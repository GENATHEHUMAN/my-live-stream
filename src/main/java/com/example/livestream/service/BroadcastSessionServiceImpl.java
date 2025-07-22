package com.example.livestream.service;

import com.example.livestream.model.Broadcast;
import com.example.livestream.model.ViewerInfo;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class BroadcastSessionServiceImpl implements IBroadcastSessionService {

    // Key: broadcast.id, Value: 방송 정보 및 실시간 시청자 목록이 담긴 Broadcast 객체
    private final Map<String, Broadcast> activeSessions = new ConcurrentHashMap<>();
    
    // Key: webSocketSessionId, Value: 해당 시청자의 정보(viewerId, broadcastId)
    private final Map<String, ViewerInfo> sessionToViewerMap = new ConcurrentHashMap<>();

    @Override
    public void startSession(Broadcast broadcast) {
        if (broadcast != null && broadcast.getId() != null) {
            activeSessions.put(broadcast.getId(), broadcast);
        }
    }

    @Override
    public Broadcast endSession(String broadcastId) {
        // 방송 종료 시, 해당 방송을 보던 모든 시청자 세션 정보도 함께 정리
        sessionToViewerMap.entrySet().removeIf(entry -> entry.getValue().getBroadcastId().equals(broadcastId));
        return activeSessions.remove(broadcastId);
    }

    @Override
    public int addViewer(String broadcastId, String viewerId, String webSocketSessionId) {
        sessionToViewerMap.put(webSocketSessionId, new ViewerInfo(viewerId, broadcastId));
        
        return Optional.ofNullable(activeSessions.get(broadcastId))
                .map(broadcast -> {
                    broadcast.getViewers().add(viewerId);
                    return broadcast.getViewers().size();
                })
                .orElse(0);
    }
    
    @Override
    public Broadcast removeViewerBySessionId(String webSocketSessionId) {
        ViewerInfo viewerInfo = sessionToViewerMap.remove(webSocketSessionId);
        if (viewerInfo == null) {
            return null;
        }
        
        Broadcast broadcast = activeSessions.get(viewerInfo.getBroadcastId());
        if (broadcast != null) {
            broadcast.getViewers().remove(viewerInfo.getViewerId());
        }
        return broadcast;
    }

    @Override
    public int getViewerCount(String broadcastId) {
        return Optional.ofNullable(activeSessions.get(broadcastId))
                       .map(b -> b.getViewers().size())
                       .orElse(0);
    }
    
    @Override
    public Broadcast getActiveSession(String broadcastId) {
        return activeSessions.get(broadcastId);
    }

    @Override
    public List<Broadcast> getActiveSessions() {
        return new ArrayList<>(activeSessions.values());
    }
}