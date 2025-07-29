# 🔄 동시성 문제 실험 프로젝트

다양한 락 전략으로 동시성 문제를 해결하는 실험 프로젝트입니다.

## 📁 프로젝트 구조

```
concurrency-problem/
├── core/                    # 공통 도메인 및 인터페이스
│   └── src/main/java/
│       └── study/concurrencyproblem/core/
│           ├── domain/      # Account 엔티티
│           └── service/     # 공통 인터페이스
├── app-lock/               # 애플리케이션 레벨 락 (synchronized, ReentrantLock)
├── db-lock/                # DB 락 (낙관적, 비관적)
└── redis-lock/             # Redis 분산 락
```

## 🎯 실험 목표

### 1단계: 동시성 문제 재현 ✅
- **app-lock** 모듈에서 멀티스레드 환경에서 잔고 마이너스 문제 확인

### 2단계: 애플리케이션 락으로 해결 (예정)
- `synchronized`, `ReentrantLock`, `StampedLock` 활용

### 3단계: DB 락으로 해결 (예정)  
- JPA `@Version` (낙관적 락)
- `@Lock(PESSIMISTIC_WRITE)` (비관적 락)

### 4단계: Redis 분산 락 (예정)
- Redisson 활용한 분산 환경 대응

### 5단계: 분산 환경 테스트 (예정)
- Docker Compose + JMeter로 실제 분산 환경 검증

# concurrency-srategies-demo
