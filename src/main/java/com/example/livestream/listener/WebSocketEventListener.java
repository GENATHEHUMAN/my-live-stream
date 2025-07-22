package com.example.livestream.listener;

import com.example.livestream.model.Broadcast;
import com.example.livestream.service.IBroadcastSessionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.Map;

@Slf4j
@Component
public class WebSocketEventListener {

    @Autowired
    private IBroadcastSessionService sessionService;
    
    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        String webSocketSessionId = event.getSessionId();
        log.info("WebSocket 연결 종료 감지: sessionId={}", webSocketSessionId);
        
        // 1. 세션 ID를 사용하여, 떠난 시청자가 보고 있던 방송 정보를 가져오고 세션에서 제거.
        Broadcast broadcast = sessionService.removeViewerBySessionId(webSocketSessionId);
        
        // 2. 만약 유효한 방송에서 나간 것이라면(null이 아니라면)
        if (broadcast != null) {
            // 3. 현재 시청자 수를 다시 가져온다.
            int currentViewers = sessionService.getViewerCount(broadcast.getId());
            log.info("방송 ID '{}'의 시청자 수 변경: {}", broadcast.getId(), currentViewers);
            
            // 4. 변경된 시청자 수를 해당 방송의 토픽으로 모든 클라이언트에게 알림.
            Map<String, Integer> viewerUpdate = Map.of("viewerCount", currentViewers);
            messagingTemplate.convertAndSend("/topic/viewers/" + broadcast.getId(), viewerUpdate);
        }
    }
}