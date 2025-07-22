package com.example.livestream.service;

import java.util.List;

import com.example.livestream.model.Broadcast;

public interface ILiveService {

	/**
     * 새로운 라이브 방송을 시작하는 전체 프로세스를 처리합니다.
     * (프로필 조회, 채팅 채널 생성, 라이브 정보 생성을 모두 포함)
     * @param broadcast 방송 정보가 담긴 객체
     * @return 성공적으로 추가된 행의 수
     */
    public int insertLiveBroadcast(Broadcast broadcast);
    
    /**
     * 진행 중인 라이브 방송을 종료 상태로 변경합니다.
     * @param broadcasterId 종료할 방송의 고유 ID
     * @return 성공적으로 수정된 행의 수
     */
    public int endLiveBroadcast(String broadcasterId);

    /**
     * 현재 방송 중인 모든 라이브 목록을 조회합니다.
     * @return 방송 정보가 담긴 Broadcast 객체 리스트
     */
    public List<Broadcast> getActiveBroadcasts();
    
    
    /**
     * ID를 기준으로 방송중인 라이브 조회
     * @param id
     * @return
     */
    public Broadcast getBroadcastById(String id);

    public String findLiveBroadcastIdByArtGroupNo(int artGroupNo);
    
    public boolean isArtistLive(int artGroupNo);
    
    /**
     * 라이브 중인 방송에 시청자가 들어오면 조회수가 늘어남
     * @param broadcastId
     * @return
     */
    public int incrementLiveHit(String broadcastId);

}