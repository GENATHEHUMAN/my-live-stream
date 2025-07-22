package com.example.livestream.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;

/**
 * WebRTC 시그널링과 방송 몱록 관리 등 복합적인 메시징을 처리
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer{

	public void configureMessageBroker(MessageBrokerRegistry config) {
		// 메시지 브로커가 /topic 접두사가 붙은 목적지의 클라이언트에게 메시지를 전달하도록 한다
		// '/topic/signal'을 구독하여 시그널링 메시지를 받는다
		config.enableSimpleBroker("/topic");
		
		// 클라이언트가 서버로 메시지를 보낼 때 사용할 프리픽스를 설정한다.
        // 예를 들어, 클라이언트는 '/app/broadcast/start'로 메시지를 보내 방송 시작을 알린다.
        config.setApplicationDestinationPrefixes("/app");
	}
	
	@Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // 클라이언트가 WebSocket에 연결하기 위한 엔드포인트를 등록한다.
        // '/ws' 경로로 SockJS 연결을 시도할 수 있다.
        // broadcast.html, watch.html의 JavaScript 코드와 경로가 일치해야 한다.
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*") // 모든 도메인에서의 접속을 허용한다. (개발용)
                .withSockJS(); // SockJS는 WebSocket을 지원하지 않는 브라우저를 위한 대체 옵션이다.
    }

    @Override
    public void configureWebSocketTransport(WebSocketTransportRegistration registration) {
        // 메시지 크기, 버퍼 등 전송 관련 세부 설정을 할 수 있다. (선택사항)
        registration.setMessageSizeLimit(128 * 1024)
                   .setSendBufferSizeLimit(512 * 1024)
                   .setSendTimeLimit(20000);
    }
}
