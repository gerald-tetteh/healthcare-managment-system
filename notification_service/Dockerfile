FROM amazoncorretto:17-alpine3.21
RUN mkdir /src
COPY target/*.jar /src/app.jar
WORKDIR /src
EXPOSE 8086
ENTRYPOINT ["java", "-jar", "app.jar"]