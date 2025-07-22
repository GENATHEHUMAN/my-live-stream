package com.example.livestream.controller;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.livestream.model.Broadcast;
import com.example.livestream.model.ChatMessage;
import com.example.livestream.model.SignalMessage;
import com.example.livestream.service.IBroadcastSessionService;
import com.example.livestream.service.ILiveService;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 */
@Slf4j
@Controller
public class BroadcastController {

    @Autowired
    private ILiveService liveService; // DB 연동 서비스

    @Autowired
    private IBroadcastSessionService sessionService; // 실시간 세션 관리 서비스

    @Autowired
    private SimpMessagingTemplate messagingTemplate; // WebSocket 메시지 전송용
    
    private final Map<String, Map<String, Object>> userSessionInfo = new ConcurrentHashMap<>();
    
    // 미디어 서버 메인 페이지 (방송 목록)
    @GetMapping("/")
    public String home(Model model) {
        List<Broadcast> activeBroadcasts = liveService.getActiveBroadcasts();
        model.addAttribute("broadcasts", activeBroadcasts);
        return "index";
    }

    // 특정 방송 시청 페이지 + 조회수 증가 (URL 경로로 ID를 직접 받는 경우)
    @GetMapping("/broadcast/{id}")
    public String watchBroadcast(@PathVariable String id, Model model) {
        log.info("ID {} 방송 시청 페이지 요청", id);
        Broadcast broadcast = liveService.getBroadcastById(id);
        if (broadcast != null) {
            liveService.incrementLiveHit(id); // 누적 조회수 1 증가
            model.addAttribute("broadcast", broadcast);
            return "watch";
        }
        return "redirect:/"; // 방송을 찾을 수 없으면 홈으로 리다이렉트
    }

    // 방송 시작 페이지
    @GetMapping("/broadcast/new")
    public String newBroadcast() {
        return "broadcast";
    }

    // [WebSocket] 방송 시작 처리
    @MessageMapping("/broadcast/start")
    public void startBroadcast(Broadcast broadcast, SimpMessageHeaderAccessor headerAccessor) {
        log.info("방송 시작 요청 받음: {}", broadcast);
        // 고유 ID 생성 및 DB 저장
        broadcast.setId(broadcast.getBroadcasterId());
        
        liveService.insertLiveBroadcast(broadcast);
        
        // 메모리에 실시간 세션 정보 등록
        sessionService.startSession(broadcast);
        
        // 방송자의 세션 ID와 사용자 정보 (임시)를 매핑
        String sessionId = headerAccessor.getSessionId();
        Map<String, Object> userInfo = new ConcurrentHashMap<>();
        
        userInfo.put("nickName", broadcast.getArtGroupNm()); 
        userInfo.put("isArtist", true);
        userSessionInfo.put(sessionId, userInfo);
        log.info("방송자 세션 정보 저장: sessionId={}, userInfo={}", sessionId, userInfo);
        
        // 모든 클라이언트에게 최신 방송 목록 브로드캐스트
        messagingTemplate.convertAndSend("/topic/broadcasts", liveService.getActiveBroadcasts());
        
        Map<String, Object> broadcastInfo = new ConcurrentHashMap<>();
        broadcastInfo.put("broadcastId", broadcast.getId());
        broadcastInfo.put("chatChannelNo", broadcast.getChatChannelNo()); 
        messagingTemplate.convertAndSend("/topic/broadcast-info/" + broadcast.getId(), broadcastInfo);
    }

    // [WebSocket] 방송 종료 처리
    @MessageMapping("/broadcast/end")
    public void endBroadcast(Map<String, String> endRequest, SimpMessageHeaderAccessor headerAccessor) {
        String broadcastId = endRequest.get("broadcasterId");
        if (broadcastId == null) {
            log.error("방송 종료 요청에 broadcastId가 없습니다.");
            return;
        }
        log.info("방송 종료 요청: {}", broadcastId);
        
        // 방송자 세션 정보 제거
        String sessionId = headerAccessor.getSessionId();
        userSessionInfo.remove(sessionId);
        log.info("방송자 세션 정보 제거: sessionId={}", sessionId);
        
        // DB와 메모리에서 방송 정보 업데이트/제거
        liveService.endLiveBroadcast(broadcastId);
        sessionService.endSession(broadcastId);

        // 최신 방송 목록과 종료 메시지를 브로드캐스트
        Map<String, Object> response = new ConcurrentHashMap<>();
        response.put("broadcastId", broadcastId);
        response.put("message", "방송이 종료되었습니다.");
        response.put("broadcasts", liveService.getActiveBroadcasts());
        
        messagingTemplate.convertAndSend("/topic/broadcast-end", response);
        messagingTemplate.convertAndSend("/topic/broadcasts", response.get("broadcasts"));
    }

    // [WebSocket] WebRTC 시그널링 처리 (viewer-join은 별도 처리)
    @MessageMapping("/signal")
    public void handleSignal(SignalMessage message, SimpMessageHeaderAccessor headerAccessor) {
        if ("viewer-join".equals(message.getType())) {
            String broadcastId = message.getReceiver();
            String viewerId = message.getSender();
            String webSocketSessionId = headerAccessor.getSessionId();

            // 세션 서비스에 시청자 추가 후, 현재 시청자 수 받기
            int viewerCount = sessionService.addViewer(broadcastId, viewerId, webSocketSessionId);
            
            // ✅ 수정된 부분: 클라이언트가 보낸 닉네임을 추출.
            String nickName = "시청자-" + webSocketSessionId.substring(0, 4); // 기본값 설정
            if (message.getData() instanceof Map) {
                // Javascript에서 보낸 data 객체를 Map으로 받는다.
                Map<String, Object> data = (Map<String, Object>) message.getData();
                if (data.get("nickName") != null) {
                    nickName = (String) data.get("nickName");
                }
            }
            
            // ✅ 수정된 부분: 추출한 실제 닉네임으로 세션 정보를 저장.
            Map<String, Object> userInfo = new ConcurrentHashMap<>();
            userInfo.put("nickName", nickName); // 추출한 닉네임으로 저장
            userInfo.put("isArtist", false);
            userSessionInfo.put(webSocketSessionId, userInfo);
            log.info("시청자 세션 정보 저장: sessionId={}, userInfo={}", webSocketSessionId, userInfo);

            // 시청자 수 변경을 모든 클라이언트에게 알림
            Map<String, Integer> viewerUpdate = Map.of("viewerCount", viewerCount);
            messagingTemplate.convertAndSend("/topic/viewers/" + broadcastId, viewerUpdate);
        }
        
        // offer, answer, ice-candidate 등 나머지 신호는 그대로 /topic/signal로 전달
        messagingTemplate.convertAndSend("/topic/signal", message);
    }
    
    // [WebSocket] 채팅 메시지 처리
    @MessageMapping("/chat/{chatChannelNo}")
    public void handleChat(@DestinationVariable String chatChannelNo, ChatMessage message, SimpMessageHeaderAccessor headerAccessor) {

    	String senderSessionId = headerAccessor.getSessionId();
        Map<String, Object> senderInfo = userSessionInfo.get(senderSessionId); // 저장된 세션 정보 조회

        if (senderInfo != null) {
            message.setSenderNick((String) senderInfo.get("nickName")); //
            message.setArtist((boolean) senderInfo.get("isArtist")); //
        } else {
            // 세션 정보가 없는 경우 (예: 새로고침 후 바로 채팅 시도 등), 기본값 설정
            log.warn("채팅 메시지 전송자의 세션 정보를 찾을 수 없습니다. 기본값으로 처리합니다. SessionId: {}", senderSessionId);
            message.setSenderNick("알 수 없는 사용자");
            message.setArtist(false);
        }

        log.info("채팅 메시지 수신 - 채널: {}, 닉네임: {}, 아티스트여부: {}, 메시지: {}",
                 chatChannelNo, message.getSenderNick(), message.isArtist(), message.getMessage());
    	
        messagingTemplate.convertAndSend("/topic/chat/" + chatChannelNo, message);
    }
    
	    // 메인 서버에서 요청하는 watchLiveByArtist (경로 변수 + 쿼리 파라미터)
	    @GetMapping("/broadcast/live/{artGroupNo}") // 메인서버 apt-community.js에서 호출하는 URL
	    public String watchLiveByArtist(
			    @PathVariable int artGroupNo, // 경로 변수로 artGroupNo를 받음
			    @RequestParam(required = false) String broadcastId, // 쿼리 파라미터로 broadcastId를 받음
			    @RequestParam(required = false) String viewerNick, // 1. 메인서버에서 보낸 닉네임을 받기 위한 파라미터 추가
			    RedirectAttributes ra
				) {
    	log.info("메인서버로부터 watchLive 요청. artGroupNo: {}, broadcastId: {}, viewerNick: {}", artGroupNo, broadcastId, viewerNick);

		if (liveService.getBroadcastById(broadcastId) != null) {
		            
            // ✅ 리다이렉트 시 'viewerNick' 파라미터를 그대로 전달
            if (viewerNick != null && !viewerNick.isEmpty()) {
                ra.addAttribute("viewerNick", viewerNick);
            }
            
            return "redirect:/broadcast/" + broadcastId;

        } else {
            // 해당 broadcastId의 방송이 없는 경우
            return "redirect:/";
        }
	}
    
}