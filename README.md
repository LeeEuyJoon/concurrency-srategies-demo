## 동시성 문제를 해결하는 다양한 락 전략 실험

다양한 락 전략의 동시성 제어 성능을 비교 분석하는 프로젝트입니다.


### 프로젝트 목적
JVM/MySQL/Redis 기반의 다양한 락 전략 적용<br>
전략별 메트릭 계측<br>
쓰기 작업 및 조회/쓰기 혼합 작업 부하 상황에서의 성능 차이 관찰<br>

<br>

### 아키텍처 구성
```
                      [ Client (JMeter) ]
                               |
                               v
                      +-----------------+
                      |   Nginx (LB)    |  :8081
                      +-----------------+
                         |     |     |
                         v     v     v
+-----------------+    +-----------------+    +-----------------+
| Spring Boot App |    | Spring Boot App |    | Spring Boot App |
|     app-1       |    |     app-2       |    |     app-3       |
|   Micrometer    |    |   Micrometer    |    |   Micrometer    |
|   :8080         |    |   :8080         |    |   :8080         |
+-----------------+    +-----------------+    +-----------------+
      |                     |                     |
      +----------+----------+----------+----------+
                 |                     |
                 v                     v
           +-----------+         +-----------+
           |   MySQL   |         |   Redis   |
           |   :3306   |         |   :6379   |
           +-----------+         +-----------+

[Observability]

+-----------------+         +-----------------+
| Prometheus 9090 |   - >   |  Grafana 3000   |
+-----------------+         +-----------------+


```

<br>

### 기술 스택
**Backend**: Spring Boot 3.5.4, Java 17<br>
**Database, Lock**: MySQL 8.0<br>
**Lock**: Redis 7-alpine<br>
**Monitoring**: Micrometer + Prometheus + Grafana<br>
**Testing**: Apache JMeter<br>
**Containerization**: Docker Compose

<br>

### 구현 전략

**1. JVM 기반 락**

`ReentrantLock`: 기본적인 배타 락 <br>
`ReentrantReadWriteLock`: 읽기/쓰기 분리 락<br>
`StampedLock`: 낙관적 읽기 지원 락<br>
`Synchronized`: JVM 내장 동기화<br>

**2. 데이터베이스 락 (MySQL)**

`Pessimistic Lock`: JPA 비관적 락 (SELECT FOR UPDATE)<br>
`Optimistic Lock`: JPA 낙관적 락 (버전 기반)<br>
`Named Lock`: MySQL 사용자 정의 락 (GET_LOCK/RELEASE_LOCK)<br>

**3. 분산 락**

`Redis Lettuce`: Spring Data Redis 기반 분산 락<br>
`Redis Redisson`: Redisson 라이브러리 기반 분산 락<br>

<br>

### 측정 지표

성능: TPS, 응답시간 분포 (P50/P90/P95/P99)<br>
락 효율성: 평균 대기시간, 재시도 횟수<br>
안정성: 에러율, 데이터 정합성 검증<br>
리소스: 트랜잭션 지속시간<br>

<br>

### 실험 시나리오

**회차별 부하 프로필** <br>
| 회차           | 스케일 | Threads | Loops | 총 요청 수 | Ramp-up |
|----------------|--------|---------|-------|------------|---------|
| 1회차 (저부하) | app=3  | 10      | 200   | 2,000      | 3s      |
| 2회차 (중부하) | app=3  | 50      | 200   | 10,000     | 10s     |
| 3회차 (고부하) | app=3  | 200     | 200   | 40,000     | 20s     |


상세 내용 아래 노션 페이지에 기재함

**only withdraw** : https://noon-blizzard-1ca.notion.site/write-only-25d25c4dfa9f80bbba98de835a0eeaac?source=copy_link

**read and withdraw (7:3)** : https://noon-blizzard-1ca.notion.site/read-and-write-26125c4dfa9f80a1a885ff3948a1142c?source=copy_link

<br>

### 실험 결과 요약

**주요 결과**

| 전략                 | Write Only (TPS/에러율/P95) | Mixed 7:3 (TPS/에러율/P95) | 특징 요약                            |
| ------------------ | ------------------------ | ----------------------- | -------------------------------- |
| **No Lock**        | 118 / **82.9%** / 1.3s   | 327 / 17.1% / 1.1s      | 정합성 붕괴, baseline                 |
| **Synchronized**   | 220 / 62% / 220ms        | 349 / 12% / 52ms        | JVM 단위에서는 동작 OK, 분산환경 정합성 불가     |
| **ReentrantLock**  | 292 / 62% / 145ms        | 464 / 10% / 27ms        | 동일                               |
| **RRWLock**        | 232 / 62% / 165ms        | 393 / 11% / 35ms        | 읽기 비중↑ 시 개선                      |
| **StampedLock**    | 142 / 63% / 218ms        | 409 / 11% / 40ms        | 낙관적 읽기 특성 반영                     |
| **MySQL Pess.**    | 291 / **0%** / 295ms     | 441 / **0%** / 360ms    | 정합성 최상, TPS 중간, 안정적 tail latency |
| **MySQL Optim.**   | 70 / 6% / **19s**        | 136 / 4% / **8.5s**     | 저부하 OK, 고부하 tail latency 폭발      |
| **Named Lock**     | 41 / 75.8% / 3.0s        | 11.5 / 26% / 3.0s       | 성능 저조, 사용 부적합                    |
| **Redis Lettuce**  | 166 / 4.2% / 1.4s        | 251 / 1.1% / 916ms      | 안정적이나 재시도 횟수 높음                  |
| **Redis Redisson** | 233 / 3.6% / 1.2s        | 528 / 0.6% / 600ms      | TPS 최상, 에러율 낮음, 운영 적합            |

<br>

### 관련 작성 글

- [전략 패턴 적용기 + 스프링에서 List로 빈 주입하기](https://luti-dev.tistory.com/16)  
- [@Transactional 메서드에서는 Synchronized 동기화가 통하지 않는다?!?!?](https://luti-dev.tistory.com/17)  


<br>
