FROM azul/zulu-openjdk-alpine:11
ENV MONGO_URL mongodb://10.166.16.24:27017/test
EXPOSE 53000
RUN mkdir -p /app/
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring
ADD build/libs/restaurant-scoring-service-1.0-SNAPSHOT.jar /app/restaurant-scoring-service-1.0.jar
ENTRYPOINT ["java","-jar","/app/restaurant-scoring-service-1.0.jar"]