# 실시간 라이브 스트리밍 서비스 (Spring Boot + WebRTC)

이 프로젝트는 Spring Boot와 WebRTC, WebSocket(STOMP/SockJS)을 활용하여 아티스트가 실시간 방송을 송출하고 팬이 시청할 수 있는 라이브 스트리밍 웹 서비스를 구현한 예제입니다.

---

## 주요 기술 스택
- **Spring Boot**: 백엔드 서버, WebSocket 시그널링 처리
- **WebRTC**: 실시간 미디어(영상/음성) 송수신
- **STOMP/SockJS**: WebSocket 기반 시그널링 메시지 교환
- **Thymeleaf**: 템플릿 기반 프론트엔드 렌더링
- **Bootstrap**: UI 스타일링

---

## 폴더 구조
```
my-live-stream/
├── src/main/java/com/example/livestream/
│   ├── config/         # WebSocket 등 서버 설정
│   ├── controller/     # 방송/시그널링 관련 컨트롤러
│   ├── model/          # 방송 정보 등 데이터 모델
│   └── LiveStreamApplication.java  # 메인 클래스
├── src/main/resources/
│   ├── templates/
│   │   ├── broadcast.html  # 방송 송출(호스트) 페이지
│   │   ├── watch.html      # 시청자(팬) 페이지
│   │   └── index.html      # 방송 목록/메인 페이지
│   └── application.properties  # 서버 네트워크 설정
├── pom.xml            # Maven 의존성
└── README.md          # 설명서(본 파일)
```

---

## 실행 전 준비
1. **Java 11+** 설치
2. **Maven** 설치
3. (서버 PC 기준) 방화벽에서 8080 포트 허용
4. `src/main/resources/application.properties`에서
   ```
   server.address=0.0.0.0
   server.port=8080
   ```
   외부 접속 허용 및 포트 지정

---

## 빌드 및 실행
```bash
# 프로젝트 루트(my-live-stream)에서
mvn clean package
java -jar target/*.jar
```

- 서버가 실행되면, 같은 네트워크의 다른 PC/모바일에서
  `http://서버IP:8080` (예: `http://192.168.35.96:8080`)로 접속

---

## 주요 동작 흐름

### 1. 방송 송출(호스트)
- `/broadcast.html` 접속 → 방송 제목/설명 입력 후 방송 시작
- 브라우저에서 카메라/마이크 권한 요청
- WebRTC로 미디어 스트림 생성, WebSocket으로 시그널링
- 시청자가 접속하면 PeerConnection 생성 및 offer/answer/ICE 교환
- 방송 종료 시 모든 연결 종료 및 알림 전송

### 2. 방송 시청(팬)
- `/watch.html` 접속 (방송 목록에서 선택)
- WebSocket으로 방송자에게 연결 요청(viewer-join)
- 방송자의 offer 수신 → answer 생성 및 전송
- 미디어 스트림 수신 후 비디오에 표시
- 방송 종료 시 알림 후 메인 페이지로 이동

---

## 코드 주요 포인트

### 방송자(broadcast.html)
- `getUserMedia`로 해상도/프레임레이트 지정 가능
- 방송 시작/종료, 시그널링, PeerConnection 관리 등 상세 주석 참고

### 시청자(watch.html)
- 방송자의 미디어 스트림을 WebRTC로 수신
- 방송 종료 알림 및 자동 리다이렉트 처리

---

## 네트워크 환경 팁
- 서버는 반드시 0.0.0.0 바인딩 필요
- 클라이언트는 서버의 실제 IP로 접속해야 함
- 방화벽/공유기 포트포워딩 등 네트워크 환경에 따라 추가 설정 필요

---

## 참고/확장
- TURN 서버 추가 시 외부 네트워크에서도 안정적 연결 가능
- 방송 녹화, 채팅, 다중 방송 등 기능 확장 가능

---

## 문의
- 코드/구현 관련 문의는 주석과 README를 참고하세요.
- 추가 질문은 언제든 환영합니다! 