FROM adoptopenjdk/openjdk15:jre-15.0.1_9-alpine

WORKDIR /opt/application

COPY target/libs/* ./libs/
COPY target/game-service*.jar ./application.jar

ENTRYPOINT java -cp "/opt/application/application.jar:/opt/application/libs/*" nl.jaapcoomans.demo.mockserver.gameservice.GameServiceApplication
