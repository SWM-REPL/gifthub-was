# GiftHub - 모바일 상품권 통합 관리 서버

**GiftHub**는 사용자들의 휴대폰에 분산된 모바일 상품권(기프티콘)을 통합 관리할 수 있도록 지원하는 백엔드 서버입니다.  
상품권 자동 등록, 유효기간 알림, 공유 기능 등 실사용자의 관리 편의성과 기프티콘 소멸 방지를 목표로 설계되었습니다.

[앱 다운로드 바로가기 ▶](https://play.google.com/store/apps/details?id=org.swmaestro.repl.GiftHub)

---

## ✨ 주요 기능

- **상품권 이미지 자동 등록**  
  이미지 인식 → 정보 추출 → 유사도 검색 → 자동 등록까지 전체 파이프라인 자동화

- **유효기간 임박 알림**  
  Spring Batch + FCM 기반의 정기 알림 발송 배치 시스템 구현

- **상품권 공유 및 사용 상태 관리**  
  상품권의 공유/회수 상태, 유효성 정보 관리

- **OAuth2 기반 로그인 및 사용자 인증**  
  Kakao, Google, Naver, Apple 계정을 통한 로그인 지원 + JWT 인증

- **Presigned URL 기반 이미지 업로드**  
  S3 기반 비동기 이미지 업로드 처리로 서버 부하 최소화

---

## 🛠 기술 스택

| 분야            | 기술 |
|----------------|------|
| Language       | Java 17 |
| Framework      | Spring Boot, Spring Security, Spring Data JPA |
| Database       | MySQL, Redis |
| Infra & DevOps | AWS (S3, CloudFront, EC2, RDS, Route53), GitHub Actions |
| Search         | Elasticsearch |
| Notification   | Firebase Cloud Messaging (FCM) |

---

## 📁 프로젝트 구조

```plaintext
gifthub-was/
├── domain/ # 도메인 계층 (Voucher, Member 등)
├── application/ # 서비스 로직
├── infrastructure/ # 외부 연동 (FCM, OAuth, S3 등)
├── global/ # 예외 처리, 응답 포맷, 설정 파일
└── docs/ # API 문서 및 ERD
```

---

## 📷 시스템 구성도

### 🔹 VPC 네트워크 구성

![vpc1 drawio](https://github.com/SWM-REPL/gifthub-was/assets/62206617/b78bcec8-ebfe-4c29-bedf-6b87c9a54759)

### 🔹 CI/CD 파이프라인

GitHub Actions 기반의 테스트, 빌드, 배포 자동화  
Slack, Sentry 연동을 통한 실시간 알림 및 장애 대응

<img width="812" alt="CI/CD 구성도" src="https://github.com/SWM-REPL/gifthub-was/assets/62206617/13ec679d-6d55-4954-a8fb-22f8afa13a0e">

### 🔹 ERD (Entity Relationship Diagram)

<img width="1579" alt="ERD" src="https://github.com/SWM-REPL/gifthub-was/assets/62206617/1d06ff2f-1c15-48f7-9235-21412537169d">

---

## 🚀 실행 방법

```bash
# 1. 환경 변수 설정
cp .env.example .env
# 환경 변수 값을 채워주세요

# 2. 빌드 및 실행
./gradlew build
java -jar build/libs/gifthub-was.jar
```

## 🧑‍💻 기여자

| 이름                                                | 역할                                      |
| ------------------------------------------------- | --------------------------------------- |
| 이진우 ([jinlee1703](https://github.com/jinlee1703)) | 백엔드 개발, 배치 시스템, OAuth 인증, 인프라 구성, CI/CD |
| 정인희 ([inh2613](https://github.com/inh2613))                                            | 백엔드 개발, ElasticSearch 연동, 인프라 구성                        |

---

## 📎 관련 링크

- [📱 앱 다운로드](https://play.google.com/store/apps/details?id=org.swmaestro.repl.GiftHub)
- [🎨 Figma 디자인](https://www.figma.com/design/g48ChGf3YK8U09umrL4ahC/230806_UI_%E1%84%8E%E1%85%AC%E1%84%8C%E1%85%A9%E1%86%BC%E1%84%89%E1%85%AE%E1%84%8C%E1%85%A5%E1%86%BC)
- [📊 발표 자료](https://www.slideshare.net/slideshow/sw-14-repl-gifthub-pptx/275371709)
- [🗂 Confluence 기반 문서](https://gifthub-confluence.vercel.app/)

---

> 궁금한 사항이나 버그 제보는 [Issues](https://github.com/SWM-REPL/gifthub-was/issues)를 통해 남겨주세요!
