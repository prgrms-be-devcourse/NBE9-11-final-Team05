# NBE9-11-final-Team-05
데브코스 백엔드 9기 11회차 최종 프로젝트 5팀 오벤저스입니다.

# 🏕️ 캠핑가자 - 캠핑 예약 플랫폼

> 공공 캠핑장 데이터를 기반으로 예약·결제·리뷰·채팅을 한 곳에서 제공하는 캠핑 예약 서비스

<br/>

## 📌 프로젝트 소개

캠핑 인구가 늘어나고 있지만 캠핑장 예약은 여전히 분산된 플랫폼을 전전해야 하는 불편함이 있습니다.

**캠핑가자**는 공공 데이터 포털(고캠핑 API)에서 전국 캠핑장 정보를 자동으로 수집하고, 호스트가 구역(Site)을 직접 등록해 예약을 받을 수 있는 구조를 제공합니다. 사용자는 캠핑장을 탐색·예약·결제하고, 실시간 채팅과 SSE 알림으로 호스트와 소통할 수 있습니다. 타임딜 기능을 통해 빠른 선착순 예약도 지원합니다.

<br/>

## 💡 주요 기능

### 🔐 인증 / 회원
- 일반 회원가입 · 로그인 (JWT + Refresh Token)
- 호스트 전용 회원가입
- 이메일 · 닉네임 중복 확인
- 마이페이지 (프로필 수정, 비밀번호 변경, 회원 탈퇴)
- 프로필 이미지 업로드 (AWS S3)

### 🏕️ 캠핑장
- 고캠핑 API 연동 — 전국 캠핑장 데이터 자동 동기화 (스케줄러)
- 캠핑장 목록 / 단건 조회 (지역 · 키워드 검색)
- 호스트 캠핑장 등록 · 수정 · 삭제 · 이미지 관리
- 호스트 캠핑장 소유권 클레임(Claim)
- 구역(Site) 등록 · 수정 · 삭제

### 📅 예약
- 날짜별 구역 가용 여부 조회
- 예약 생성 · 상세 조회 · 취소
- 호스트 예약 목록 조회
- 동시성 제어 (비관적 락 기반 재고 관리)
- 체크아웃 완료 자동 처리 (스케줄러)

### 💳 결제
- 토스페이먼츠 연동 (결제 승인 API)
- 결제 내역 조회
- 결제 실패 재시도 스케줄러

### 💰 정산
- 호스트 정산 내역 조회
- 관리자 정산 생성 · 완료 처리

### ⚡ 타임딜
- 호스트 타임딜 등록 · 수정 · 취소 · 삭제
- 활성 타임딜 목록 / 단건 조회
- 선착순 구매 (비관적 락 기반 동시성 제어)
- 타임딜 상태 자동 전환 스케줄러

### 💬 채팅
- WebSocket(STOMP) 기반 실시간 채팅
- 캠핑장 오픈 채팅방 (캠핑장 승인 시 자동 생성)
- 예약 완료 시 호스트-게스트 1:1 채팅방 자동 생성
- 채팅방 목록 / 메시지 내역 조회

### 🔔 알림
- SSE(Server-Sent Events) 기반 실시간 알림
- 알림 목록 조회 · 읽음 처리 (단건 / 전체)
- 미읽음 알림 수 조회

### ⭐ 리뷰
- 완료된 예약에 대해 리뷰 작성 · 수정 · 삭제
- 캠핑장별 리뷰 목록 조회
- 내 리뷰 목록 조회

### 🛡️ 관리자
- 관리자 전용 로그인
- 회원 목록 조회 · 밴/언밴
- 캠핑장 승인 · 거절 · 일괄 승인
- 리뷰 관리 (삭제)
- 정산 생성 · 완료 처리
- 대시보드 (통계)

<br/>

## 👥 팀원 소개

| 이름 | 담당 |
|------|------|
| 이형진 | 팀장, 예약, 결제(토스페이먼츠), 정산 도메인 담당 |
| 최민규 | CI/CD, 인프라 구축, 알람(SSE), 리뷰, 관리자 도메인 담당 |
| 서준우 | 타임딜, Auth 및 JWT 인증 구현 담당 |
| 문서희 | 고캠핑 외부 API 연동, 실시간 채팅 기능 구현 담당|
| 황지윤 | 호스트 도메인 및 S3 이미지 추가 기능 구현 담당 |


<br/>

## 🛠 기술 스택

### Backend
![Java](https://img.shields.io/badge/Java_25-ED8B00?style=flat&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot_4-6DB33F?style=flat&logo=springboot&logoColor=white)
![Spring Security](https://img.shields.io/badge/Spring_Security-6DB33F?style=flat&logo=springsecurity&logoColor=white)
![Spring Data JPA](https://img.shields.io/badge/Spring_Data_JPA-6DB33F?style=flat&logo=spring&logoColor=white)
![QueryDSL](https://img.shields.io/badge/QueryDSL-0769AD?style=flat&logoColor=white)
![WebSocket](https://img.shields.io/badge/WebSocket_STOMP-010101?style=flat&logoColor=white)
![SSE](https://img.shields.io/badge/SSE-FF6B35?style=flat&logoColor=white)
![MySQL](https://img.shields.io/badge/MySQL-4479A1?style=flat&logo=mysql&logoColor=white)
![Redis](https://img.shields.io/badge/Redis-FF4438?style=flat&logo=redis&logoColor=white)
![JWT](https://img.shields.io/badge/JWT-000000?style=flat&logo=jsonwebtokens&logoColor=white)

### Frontend
![Next.js](https://img.shields.io/badge/Next.js_15-000000?style=flat&logo=nextdotjs&logoColor=white)
![React](https://img.shields.io/badge/React-61DAFB?style=flat&logo=react&logoColor=black)
![TypeScript](https://img.shields.io/badge/TypeScript-3178C6?style=flat&logo=typescript&logoColor=white)
![Tailwind CSS](https://img.shields.io/badge/Tailwind_CSS-06B6D4?style=flat&logo=tailwindcss&logoColor=white)

### Infra / DevOps
![AWS EC2](https://img.shields.io/badge/AWS_EC2-FF9900?style=flat&logo=amazonec2&logoColor=white)
![AWS RDS](https://img.shields.io/badge/AWS_RDS-527FFF?style=flat&logo=amazonrds&logoColor=white)
![AWS S3](https://img.shields.io/badge/AWS_S3-569A31?style=flat&logo=amazons3&logoColor=white)
![Nginx](https://img.shields.io/badge/Nginx-009639?style=flat&logo=nginx&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-2496ED?style=flat&logo=docker&logoColor=white)
![GitHub Actions](https://img.shields.io/badge/GitHub_Actions-2088FF?style=flat&logo=githubactions&logoColor=white)

### Monitoring
![Prometheus](https://img.shields.io/badge/Prometheus-E6522C?style=flat&logo=prometheus&logoColor=white)
![Grafana](https://img.shields.io/badge/Grafana-F46800?style=flat&logo=grafana&logoColor=white)

### External API
![고캠핑](https://img.shields.io/badge/고캠핑_API-1AAB8A?style=flat&logoColor=white)
![Toss Payments](https://img.shields.io/badge/Toss_Payments-0064FF?style=flat&logoColor=white)

<br/>

## 📁 프로젝트 구조

### 백엔드

```
backend/src/main/java/com/back/ovengers/
├── OvengersApplication.java
├── domain/
│   ├── admin/                        # 관리자 (회원 밴, 캠핑장 승인, 리뷰 관리)
│   │   ├── controller/AdminController.java
│   │   ├── service/AdminService.java
│   │   └── dto/
│   ├── auth/                         # 인증 (회원가입, 로그인, Refresh Token)
│   │   ├── controller/AuthController.java
│   │   ├── service/AuthService.java
│   │   └── dto/
│   ├── camping/                      # 캠핑장 (CRUD, 이미지, 고캠핑 API 동기화)
│   │   ├── controller/
│   │   ├── service/
│   │   ├── external/                 # 고캠핑 API 클라이언트 · 동기화 서비스
│   │   ├── scheduler/                # 데이터 동기화 스케줄러
│   │   ├── event/                    # 캠핑장 승인 이벤트 (오픈 채팅방 생성 트리거)
│   │   ├── repository/
│   │   └── entity/
│   ├── chat/                         # 채팅 (WebSocket/STOMP, 오픈·1:1 채팅)
│   │   ├── controller/
│   │   ├── service/
│   │   ├── event/                    # 결제 완료 이벤트 (1:1 채팅방 생성 트리거)
│   │   ├── repository/
│   │   └── entity/
│   ├── notification/                 # 알림 (SSE 실시간 알림, 읽음 처리)
│   │   ├── controller/
│   │   ├── service/
│   │   ├── repository/
│   │   └── entity/
│   ├── payment/                      # 결제 (토스페이먼츠 연동, 실패 재시도)
│   │   ├── controller/
│   │   ├── service/
│   │   ├── client/                   # TossPaymentClient
│   │   ├── scheduler/
│   │   └── entity/
│   ├── reservation/                  # 예약 (생성·취소, 동시성 제어, 완료 스케줄러)
│   │   ├── controller/
│   │   ├── service/
│   │   ├── scheduler/
│   │   └── entity/
│   ├── review/                       # 리뷰 (CRUD, 캠핑장별·내 리뷰 조회)
│   │   ├── controller/
│   │   ├── service/
│   │   └── entity/
│   ├── settlement/                   # 정산 (호스트 정산, 관리자 정산 처리)
│   │   ├── controller/
│   │   └── dto/
│   ├── site/                         # 구역 (호스트 구역 CRUD)
│   │   ├── controller/
│   │   ├── service/
│   │   └── entity/
│   ├── timedeal/                     # 타임딜 (선착순 구매, 비관적 락)
│   │   ├── controller/
│   │   ├── service/
│   │   ├── scheduler/
│   │   └── entity/
│   └── user/                         # 회원 (마이페이지, 프로필 이미지)
│       ├── controller/
│       ├── service/
│       └── entity/
└── global/
    ├── config/                       # Security, QueryDSL, Redis, WebSocket, S3, Swagger
    ├── security/                     # JwtProvider, JwtFilter
    ├── exception/                    # GlobalExceptionHandler, ErrorCode
    ├── response/                     # ApiResponse, PageResponse, CursorResponse
    └── s3/                           # S3Service
```

### 프론트엔드

```
frontend/src/
├── app/
│   ├── page.tsx                      # 메인 (캠핑장 목록)
│   ├── campings/
│   │   ├── page.tsx                  # 캠핑장 목록
│   │   ├── search/page.tsx           # 캠핑장 검색
│   │   └── [campingId]/
│   │       ├── page.tsx              # 캠핑장 상세
│   │       ├── reservation/page.tsx  # 예약 페이지
│   │       └── reviews/page.tsx      # 리뷰 목록
│   ├── timedeals/
│   │   ├── page.tsx                  # 타임딜 목록
│   │   └── [id]/page.tsx             # 타임딜 상세
│   ├── mypage/
│   │   ├── reservations/             # 내 예약 목록·상세
│   │   └── reviews/page.tsx          # 내 리뷰 목록
│   ├── host/                         # 호스트 전용 (캠핑장·구역·예약·타임딜 관리)
│   │   ├── campings/
│   │   ├── reservations/
│   │   └── timedeals/
│   ├── admin/                        # 관리자 전용 페이지
│   │   ├── dashboard/
│   │   ├── members/
│   │   ├── camping-approvals/
│   │   ├── reviews/
│   │   └── settlements/
│   ├── auth/                         # 로그인 · 회원가입
│   └── payment/                      # 결제 처리
├── components/
│   ├── camping/                      # 캠핑장 상세 UI 컴포넌트
│   ├── chat/                         # 채팅 UI (FAB, 채팅창, 메시지 패널)
│   ├── notification/                 # 알림 벨
│   ├── reservation/                  # 예약 폼, 취소 버튼
│   ├── review/                       # 리뷰 컴포넌트
│   ├── timedeal/                     # 타임딜 카드
│   ├── host/                         # 호스트 전용 컴포넌트
│   └── ui/                           # 공통 UI (Button, Card, Input 등)
└── store/                            # Zustand 전역 상태
```

<br/>

## 🗄 ERD

<img width="1192" height="811" alt="스크린샷 2026-07-01 오후 2 22 02" src="https://github.com/user-attachments/assets/5169ac27-62b7-4819-990b-67bee2ea2e0c" />


<br/>

## ✅ 테스트 커버리지

JaCoCo 기반 단위 테스트 · 통합 테스트를 포함한 총 **373개** 테스트 케이스 작성

| 도메인 | 커버리지 |
|--------|---------|
| auth | ✅ |
| admin | ✅ |
| camping | ✅ |
| chat | ✅ |
| notification | ✅ |
| payment | ✅ |
| reservation | ✅ |
| review | ✅ |
| settlement | ✅ |
| site | ✅ |
| timedeal | ✅ |
| user | ✅ |

> 전체 커버리지: **약 77%** (외부 API 연동 모듈 제외 시 더 높음)

**주요 테스트 전략**
- `@SpringBootTest` + `@AutoConfigureMockMvc`: 컨트롤러 통합 테스트
- `@ExtendWith(MockitoExtension.class)`: 서비스 단위 테스트
- 동시성 테스트: `ExecutorService` + `CountDownLatch` (예약·타임딜·결제)
- SSE 실시간 알림 비동기 테스트

<br/>

## 🚀 실행 방법

### Backend

**1. 레포지토리 클론**

```bash
git clone https://github.com/prgrms-be-devcourse/NBE9-11-final-Team05.git
cd NBE9-11-final-Team05/backend
```

**2. 환경변수 설정** (`src/main/resources/application-secret.yml`)

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/ovengers
    username: {DB_USERNAME}
    password: {DB_PASSWORD}

jwt:
  secret: {JWT_SECRET}

redis:
  host: localhost
  port: 6379

toss:
  secret-key: {TOSS_SECRET_KEY}

go-camping:
  value:
    service-key: {GO_CAMPING_API_KEY}

cloud:
  aws:
    s3:
      bucket: {S3_BUCKET_NAME}
    credentials:
      access-key: {AWS_ACCESS_KEY}
      secret-key: {AWS_SECRET_KEY}
    region:
      static: ap-northeast-2
```

**3. 서버 실행**

```bash
./gradlew bootRun
```

**4. API 문서 확인**

```
http://localhost:8080/swagger-ui.html
```

### Frontend

**1. 패키지 설치**

```bash
cd frontend
npm install
```

**2. 환경변수 설정** (`.env.local`)

```env
NEXT_PUBLIC_API_BASE_URL=http://localhost:8080
```

**3. 개발 서버 실행**

```bash
npm run dev
```

```
http://localhost:3000
```

<br/>

## ⚙️ CI/CD

GitHub Actions를 통해 `dev` 브랜치 푸시 시 자동 배포됩니다.

```
push to dev
  → GitHub Actions
    ① test job  : ./gradlew test (테스트 실패 시 배포 중단)

    ② build-and-deploy job (test 성공 시에만 실행)
         → Docker 이미지 빌드
         → DockerHub (artiffect/team5-backend) 에 push
         → EC2에 docker-compose.prod.yml 전송
         → EC2 접속: docker pull → docker compose up --force-recreate
         → /actuator/health 헬스체크 (최대 60초 대기)
  → EC2: Nginx Proxy Manager로 도메인 라우팅 및 SSL 처리
  → 이미지 에셋: AWS S3 + CloudFront CDN
  → Frontend: Vercel (로컬에서 직접 배포)
```

<br/>

## 📊 모니터링

Prometheus + Grafana 기반 모니터링을 **로컬 환경**에서 구성했습니다.

- Spring Boot Actuator를 통해 메트릭 수집 (`/actuator/prometheus`)
- Grafana 대시보드로 API 응답 시간, JVM 힙 사용량, DB 커넥션 풀 시각화

**k6**로 배포된 EC2 서버의 백엔드 서버를 대상으로 원격 부하 테스트 수행 — VU(가상 유저) 기반 동시 요청 시나리오로 병목 지점 파악 및 성능 검증
