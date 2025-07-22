package com.example.livestream.service;

import com.example.livestream.model.Broadcast;
import com.example.livestream.model.ViewerInfo; // 아래에서 만들 ViewerInfo 클래스 import

import java.util.List;

/**
 * 메모리에서 실시간 방송 세션을 관리하기 위한 서비스 인터페이스
 */
public interface IBroadcastSessionService {

    // 새로운 방송 세션을 메모리에 추가
    void startSession(Broadcast broadcast);

    // 방송 세션을 메모리에서 제거하고, 제거된 방송 정보를 반환
    Broadcast endSession(String broadcastId);

    // 특정 방송에 시청자를 추가하고, 현재 시청자 수를 반환
    int addViewer(String broadcastId, String viewerId, String webSocketSessionId);
    
    // WebSocket 세션 ID로 시청자를 제거하고, 해당 시청자가 보고 있던 방송 정보를 반환
    Broadcast removeViewerBySessionId(String webSocketSessionId);
    
    // 특정 방송의 현재 시청자 수를 반환
    int getViewerCount(String broadcastId);
    
    // ID로 특정 활성 방송 세션 정보를 가져옴
    Broadcast getActiveSession(String broadcastId);
    
    // 현재 진행중인 모든 방송 세션 목록을 반환
    List<Broadcast> getActiveSessions();
}