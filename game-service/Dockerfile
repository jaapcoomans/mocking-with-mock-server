FROM adoptopenjdk/openjdk13:jre-13_33-alpine

WORKDIR /opt/application

COPY target/libs/* ./libs/
COPY target/game-service*.jar ./application.jar

ENTRYPOINT java --enable-preview -cp "/opt/application/application.jar:/opt/application/libs/*" nl.jaapcoomans.demo.mockserver.gameservice.GameServiceApplication
