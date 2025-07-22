package com.example.livestream.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import com.example.livestream.model.Broadcast;

@Mapper
public interface LiveMapper {

    /**
     * 방송하는 아티스트의 커뮤니티 프로필 번호를 조회.
     * @param broadcast artGroupNo와 userId가 담긴 객체
     * @return 커뮤니티 프로필 번호
     */
    public Integer getComuProfileNo(Broadcast broadcast);

    /**
     * 라이브 방송을 위한 새로운 채팅 채널을 생성.
     * @param broadcast artGroupNo와 comuProfileNo가 담긴 객체
     * @return 성공적으로 추가된 행의 수
     */
    public int createChatChannel(Broadcast broadcast);

    /**
     * 새로운 라이브 방송 정보를 DB에 INSERT.
     * @param broadcast 방송 정보가 담긴 객체
     * @return 성공적으로 추가된 행의 수
     */
    public int insertLiveBroadcast(Broadcast broadcast);
    
    /**
     * 진행 중인 라이브 방송을 종료 상태로 UPDATE.
     * @param broadcasterId 종료할 방송을 식별하는 ID (LIVE_URL에 저장된 값)
     * @return 성공적으로 수정된 행의 수
     */
    public int endLiveBroadcast(String broadcasterId);
    
    /**
     * 현재 방송 중인 모든 라이브 목록을 조회.
     * @return 방송 정보가 담긴 Broadcast 객체 리스트
     */
    public List<Broadcast> getActiveBroadcasts();
    
    /**
     * ID를 기준으로 방송중인 라이브 찾기
     * @param id
     * @return
     */
    public Broadcast getBroadcastById(String id);
    
    public String findLiveBroadcastIdByArtGroupNo(int artGroupNo);
    
    public int isArtistLive(int artGroupNo);
    
    public int incrementLiveHit(String broadcastId);
}