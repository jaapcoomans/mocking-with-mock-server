# Mocking your microservices with MockServer

This project is the demo that belongs to my conference talk "Mocking your microservices with MockServer". 
It includes a demo of using MockServer for testing the game-service of the hypothetical application Over-Engineered-Mastermind.
The `game-service` folder contains the implementation of said service.
The `component-test` folder contains the tests that form the actual demo.

## Talk abstract

How can I test my microservices? It’s an often heard question that leads to a lot of debate. 
Deployment and interdependence with other services are the challenges we’re facing there. 
So, what if we could treat our microservices tests just like our familiar unit tests? 
What if we could isolate the microservice and mock all its dependencies? With MockServer you can do just that. 
MockServer lets you mock any http service, making you independent of other teams building those services. 
And the best part is, you can use it with the tools you already know like JUnit, Maven and Docker and do all the things you already know from your favorite Java mocking framework. 
You can run MockServer locally, as part of your Maven build or as a Docker image in your cloud environment. 
In this session I will show you exactly how you can use MockServer for your tests.

## Building and running the project

Make sure you run a Maven build (`mvn package`) before running the component tests in the `component-tests` module.
The demo is in the component-test, which uses TestContainers and JUnit 5. 
This means you can run it from your favourite IDE, or using Maven or any other tool that integrates with JUnit 5. 
TestContainers will build the Docker container for the `game-service` on start, but the Dockerfile assumes the Maven build was already performed.

## Conferences
This talk was given at the following conferences:

| Conference     | Date         | Session details | Video link |
|----------------|--------------|-----------------|------------|
| J-Fall         | 10 May 2019  | [Session details](https://jfall.nl/sessions/mocking-your-microservices-with-mock-server/) | t.b.a. |
| Devoxx Belgium | 16 May 2019  | [Session details](https://devoxx.be/talk/?id=13606) | [YouTube video](https://youtu.be/oipykrGdsk8) |
| JavaLand       | 17/18 March 2020 | [Session details](https://events.rainfocus.com/widget/oracle/oow19/catalogcodeone19?search=dev1301) | n/a |
