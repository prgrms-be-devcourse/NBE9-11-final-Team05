# ---- 1단계: 빌드 스테이지 ----
# JDK 전체가 필요한 빌드 작업은 여기서만 하고, 최종 이미지에는 안 들어감
FROM eclipse-temurin:25-jdk AS build
WORKDIR /app

# Gradle wrapper 관련 파일만 먼저 복사 (소스코드보다 먼저)
# → 의존성이 안 바뀌면 이 레이어가 캐싱되어 다음 빌드 때 다시 안 받아도 됨
COPY gradlew .
COPY gradle gradle
COPY build.gradle settings.gradle ./
RUN chmod +x gradlew

# 의존성만 먼저 다운로드
# || true → 이 단계에서 실패해도(예: 플러그인 설정 문제) 빌드 전체가 멈추지 않게 함
RUN ./gradlew dependencies --no-daemon || true

# 소스코드는 가장 마지막에 복사
# → 소스만 바뀌었을 때 위의 의존성 레이어는 캐시에서 그대로 재사용됨 (빌드 속도 단축)
COPY src src
RUN ./gradlew bootJar --no-daemon

# ---- 2단계: 실행 스테이지 ----
# JRE만 있는 가벼운 이미지로 교체 (빌드 도구, 소스코드는 여기 안 들어감)
FROM eclipse-temurin:25-jre AS run
WORKDIR /app
# 이 시점에 /app 디렉토리가 만들어지는데, 아직 USER 선언 전이라 소유자가 root로 찍힘

# 보안: root가 아닌 별도 유저로 컨테이너 실행
# -m: 홈 디렉토리 생성, -u 1001: UID 명시 (베이스 이미지에 기본 유저가 있든 없든 항상 동작)
# && chown appuser:appuser /app
# → WORKDIR에서 root 소유로 생성된 /app 디렉토리를 appuser 소유로 바꿔줌
# → 이걸 안 해두면, 나중에 앱이 /app 안에 로그/임시파일 등을 쓰려고 할 때
#   appuser는 그 디렉토리의 주인이 아니라서 Permission denied 에러가 날 수 있음
RUN useradd -m -u 1001 appuser && chown appuser:appuser /app
USER appuser
# 이 줄 이후의 모든 명령(COPY, ENTRYPOINT)은 appuser 권한으로 수행됨

# build.gradle에서 plain jar 생성을 막아뒀기 때문에
# build/libs/ 안에는 실행 가능한 jar 하나만 있어서 *.jar 와일드카드를 안전하게 쓸 수 있음
# --chown=appuser:appuser
# → 파일을 복사하면서 동시에 소유자를 appuser로 지정
# → /app 디렉토리뿐 아니라 그 안의 jar 파일도 appuser 소유로 일관되게 맞춰줌
COPY --chown=appuser:appuser --from=build /app/build/libs/*.jar app.jar

# 컨테이너가 8080 포트를 쓴다는 걸 명시 (문서 역할, 실제 포트 매핑은 docker-compose에서)
EXPOSE 8080

# -Xmx400m: JVM 힙 메모리를 400MB로 제한
ENTRYPOINT ["java", "-Xmx400m", "-jar", "app.jar"]