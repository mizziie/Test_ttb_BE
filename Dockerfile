FROM eclipse-temurin:17-jdk-alpine as build
WORKDIR /workspace/app

# ติดตั้ง Maven
RUN apk add --no-cache maven

# คัดลอกไฟล์โปรเจค
COPY pom.xml .
COPY src src

# สร้าง JAR
RUN mvn package -DskipTests
RUN mkdir -p target/dependency && (cd target/dependency; jar -xf ../*.jar)

FROM eclipse-temurin:17-jre-alpine
VOLUME /tmp
ARG DEPENDENCY=/workspace/app/target/dependency
COPY --from=build ${DEPENDENCY}/BOOT-INF/lib /app/lib
COPY --from=build ${DEPENDENCY}/META-INF /app/META-INF
COPY --from=build ${DEPENDENCY}/BOOT-INF/classes /app
ENTRYPOINT ["java","-cp","app:app/lib/*","com.example.surveyservice.SurveyServiceApplication"] 