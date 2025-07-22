package com.example.livestream.model;

import lombok.Data;

@Data
public class ChatMessage {

    // 메시지가 속한 채팅 채널 번호 (어느 방송의 채팅인지 식별)
    private String chatChannelNo;
    
    // 메시지를 보낸 사람의 닉네임
    private String senderNick;
    
    // 메시지 내용
    private String message;
    
    // 아티스트가 보낸 메시지인지 구분하기 위한 플래그
    private boolean isArtist;

}