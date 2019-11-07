package nl.jaapcoomans.demo.mockserver.componenttest;

import io.restassured.RestAssured;
import io.restassured.specification.RequestSpecification;
import nl.jaapcoomans.demo.mockserver.componenttest.restapimodel.Game;
import nl.jaapcoomans.demo.mockserver.componenttest.restapimodel.GameStatus;
import nl.jaapcoomans.demo.mockserver.componenttest.restapimodel.Result;
import org.json.JSONException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockserver.client.MockServerClient;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MockServerContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.images.builder.ImageFromDockerfile;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.nio.file.Paths;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.skyscreamer.jsonassert.JSONAssert.assertEquals;

@Testcontainers
class GameServiceComponentTest {
    private static ImageFromDockerfile gameServiceImage = new ImageFromDockerfile().withFileFromPath(".", Paths.get("../game-service"));

    private static Network network = Network.newNetwork();

    @Container
    private static MockServerContainer generatorContainer =
            new MockServerContainer()
                    .withNetwork(network)
                    .withNetworkAliases("generator");

    @Container
    private static MockServerContainer checkerContainer = new MockServerContainer()
            .withNetwork(network)
            .withNetworkAliases("checker");

    @Container
    private static MockServerContainer tournamentContainer = new MockServerContainer()
            .withNetwork(network)
            .withNetworkAliases("tournament-svc");

    @Container
    private static GenericContainer gameService =
            new GenericContainer(gameServiceImage)
                    .withEnv("GENERATOR_URL", "http://generator:1080")
                    .withEnv("CHECKER_URL", "http://checker:1080")
                    .withEnv("TOURNAMENT_SVC_URL", "http://tournament-svc:1080")
                    .withNetwork(network)
                    .withExposedPorts(8080);

    private static MockServerClient generatorMock;
    private static MockServerClient checkerMock;
    private static MockServerClient tournamentMock;

    @BeforeAll
    static void init() {
        generatorMock = new MockServerClient(
                generatorContainer.getContainerIpAddress(),
                generatorContainer.getServerPort());

        checkerMock = new MockServerClient(
                checkerContainer.getContainerIpAddress(),
                checkerContainer.getServerPort());

        tournamentMock = new MockServerClient(
                tournamentContainer.getContainerIpAddress(),
                tournamentContainer.getServerPort());
    }

    @BeforeEach
    void resetMocks() {
        generatorMock.reset();
        checkerMock.reset();
        tournamentMock.reset();
    }

    @Test
    @DisplayName("When a game is started, it must be in progress.")
    void testStartGame() {
        // Given a generator service
        generatorMock
                .when(request()
                        .withMethod("GET")
                        .withHeader("Accept", "application/json")
                        .withPath("/generate"))
                .respond(response()
                        .withHeader("Content-Type", "application/json")
                        .withBody(createACode())
                        .withStatusCode(200));

        // When a new game is started
        var game = RestAssured.given()
                .header("Accept", "application/json")
                .baseUri("http://localhost:" + gameService.getFirstMappedPort())
                .when().post("/games")
                .as(Game.class);

        // Then the game must be in progress and only the generator be called.
        assertThat(game.getStatus()).isEqualTo(GameStatus.IN_PROGRESS);

        generatorMock.verify(request("/generate").withMethod("GET"));

        checkerMock.verifyZeroInteractions();
        tournamentMock.verifyZeroInteractions();
    }

    @Test
    @DisplayName("When a guess is made for a game that does not exist, a HTTP 400 is returned.")
    void testGuessNonExistingGame() {
        // Given no active games

        // When a guess is made for a game
        var response = RestAssured.given()
                .header("Accept", "application/json")
                .baseUri("http://localhost:" + gameService.getFirstMappedPort())
                .body(createACode())
                .when().post("/games/" + UUID.randomUUID().toString() + "/guess");

        // Then a 400 response
        response.then()
                .statusCode(400)
                .and()
                .body("message", containsString("Game does not exist"));

        generatorMock.verifyZeroInteractions();
        checkerMock.verifyZeroInteractions();
        tournamentMock.verifyZeroInteractions();
    }

    @Test
    @DisplayName("When the solution is requested for a game that does not exist, a HTTP 400 is returned.")
    void testGetSolutionNonExistingGame() {
        // Given no active games

        // When the solution is requested for a game
        var response = RestAssured.given()
                .header("Accept", "application/json")
                .baseUri("http://localhost:" + gameService.getFirstMappedPort())
                .body(createACode())
                .when().post("/games/" + UUID.randomUUID().toString() + "/guess");

        // Then a 400 response
        response.then()
                .statusCode(400)
                .and()
                .body("message", containsString("Game does not exist"));

        generatorMock.verifyZeroInteractions();
        checkerMock.verifyZeroInteractions();
        tournamentMock.verifyZeroInteractions();
    }

    @Test
    @DisplayName("When a guess is made for an existing game, a valid result is returned.")
    void testGuessProducesResult() {
        // Given a generator service and a newly started game
        var game = prepareAGame();
        var guess = createACode();

        checkerMock
                .when(request()
                        .withMethod("POST")
                        .withPath("/check"))
                .respond(response()
                        .withHeader("Content-Type", "application/json")
                        .withStatusCode(200)
                        .withBody(createAFailedResult())
                );

        // When
        var response = gameServiceRequest(guess)
                .post("/games/" + game.getId() + "/guess");

        // Then the response is a valid result
        response.then().statusCode(200);
        var result = response.as(Result.class);

        assertThat(result.getBlackPins()).isEqualTo(0);
        assertThat(result.getWhitePins()).isEqualTo(0);

        checkerMock.verify(request()
                .withMethod("POST")
                .withHeader("Accept", "application/json")
                .withPath("/check"));

        generatorMock.verifyZeroInteractions();
        tournamentMock.verifyZeroInteractions();
    }

    @Test
    @DisplayName("When a solution is requested for a game that has not yet ended, a HTTP 400 is returned.")
    void testGetSolutionDelegatesToGame() {
        // Given a newly started game
        var game = prepareAGame();

        // When
        var response = gameServiceRequest()
                .get("/games/" + game.getId() + "/solution");

        // Then
        response.then()
                .statusCode(400)
                .and()
                .body("message", containsString("I'm not telling the code! The game is still in progress!"));

        checkerMock.verifyZeroInteractions();
        generatorMock.verifyZeroInteractions();
        tournamentMock.verifyZeroInteractions();
    }

    @Test
    @DisplayName("When a game is won, the tournament-service is called")
    void testGameWonCallsTournamentService() throws JSONException {
        // Given
        var code = createACode();
        var game = prepareAGame(code);

        checkerMock
                .when(request()
                        .withMethod("POST")
                        .withHeader("Accept", "application/json")
                        .withPath("/check"))
                .respond(response()
                        .withHeader("Content-Type", "application/json")
                        .withStatusCode(200)
                        .withBody(createASuccessResult()));

        tournamentMock
                .when(request()
                        .withMethod("PUT")
                        .withPath("/games/" + game.getId() + "/result"))
                .respond(response()
                        .withHeader("Content-Type", "application/json")
                        .withStatusCode(201));

        // When
        var response = gameServiceRequest(code).post("/games/" + game.getId() + "/guess");

        // Then a call must be made to the tournament service
        response.then().statusCode(200);

        var actualRequests = tournamentMock.retrieveRecordedRequests(
                request()
                        .withMethod("PUT")
                        .withPath("/games/.*/result"));

        assertThat(actualRequests.length).isEqualTo(1);

        var expectedBody = expectedGameEndedBody(game.getId());
        var actualBody = actualRequests[0].getBodyAsString();

        assertEquals(expectedBody, actualBody, JSONCompareMode.LENIENT);
    }

    private Game prepareAGame() {
        String code = createACode();

        return prepareAGame(code);
    }

    private Game prepareAGame(String code) {
        generatorMock
                .when(request()
                        .withMethod("GET")
                        .withHeader("Accept", "application/json")
                        .withPath("/generate"))
                .respond(response()
                        .withHeader("Content-Type", "application/json")
                        .withBody(code)
                        .withStatusCode(200));

        var game = gameServiceRequest().post("/games").as(Game.class);

        // Reset the mock in order to verify interactions that took place after this prep.
        generatorMock.reset();

        return game;
    }

    private static RequestSpecification gameServiceRequest() {
        return RestAssured.given()
                .header("Accept", "application/json")
                .baseUri("http://localhost:" + gameService.getFirstMappedPort())
                .when();
    }

    private static RequestSpecification gameServiceRequest(String body) {
        return RestAssured.given()
                .header("Accept", "application/json")
                .baseUri("http://localhost:" + gameService.getFirstMappedPort())
                .body(body)
                .when();
    }

    private String createACode() {
        return """
            {
                "pin0": "RED",
                "pin1": "GREEN",
                "pin2": "BLUE",
                "pin3": "YELLOW"
            }
        """;
    }

    private String createAFailedResult() {
        return """
            {
                "blackPins": "0",
                "whitePins": "0"
            }
        """;
    }

    private String createASuccessResult() {
        return """
            {
                "blackPins": "4",
                "whitePins": "0"
            }
        """;
    }

    @SuppressWarnings("removal")
    private String expectedGameEndedBody(UUID gameId) {
        return """
        {
            "gameId": "%s",
            "status": "WON",
            "guesses": 1
        }
        """.formatted(gameId);
    }
}
