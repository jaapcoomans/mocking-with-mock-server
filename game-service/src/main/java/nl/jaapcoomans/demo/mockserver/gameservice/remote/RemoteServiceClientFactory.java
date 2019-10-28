package nl.jaapcoomans.demo.mockserver.gameservice.remote;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Body;
import feign.Feign;
import feign.Headers;
import feign.Logger;
import feign.Param;
import feign.RequestLine;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import feign.slf4j.Slf4jLogger;
import nl.jaapcoomans.demo.mockserver.gameservice.domain.Code;
import nl.jaapcoomans.demo.mockserver.gameservice.domain.CodeChecker;
import nl.jaapcoomans.demo.mockserver.gameservice.domain.CodeGenerator;
import nl.jaapcoomans.demo.mockserver.gameservice.domain.GameStatus;
import nl.jaapcoomans.demo.mockserver.gameservice.domain.TournamentService;

import java.util.UUID;

public class RemoteServiceClientFactory {
    private static final String ENV_GENERATOR_URL = "GENERATOR_URL";
    private static final String ENV_CHECKER_URL = "CHECKER_URL";
    private static final String ENV_TOURNAMENT_SVC_URL = "TOURNAMENT_SVC_URL";

    private static final String DEFAULT_GENERATOR_URL = "http://localhost:8081";
    private static final String DEFAULT_CHECKER_URL = "http://localhost:8082";
    private static final String DEFAULT_TOURNAMENT_SVC_URL = "http://localhost:8083";

    private ObjectMapper objectMapper;

    private final String codeGeneratorUrl;
    private final String codeCheckerUrl;
    private final String tournamentServiceUrl;

    public RemoteServiceClientFactory(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;

        this.codeGeneratorUrl = getEnvOrDefault(ENV_GENERATOR_URL, DEFAULT_GENERATOR_URL);
        this.codeCheckerUrl = getEnvOrDefault(ENV_CHECKER_URL, DEFAULT_CHECKER_URL);
        this.tournamentServiceUrl = getEnvOrDefault(ENV_TOURNAMENT_SVC_URL, DEFAULT_TOURNAMENT_SVC_URL);
    }

    private static String getEnvOrDefault(String variable, String defaultValue) {
        var value = System.getenv(variable);
        if (value != null) {
            return value;
        } else {
            return defaultValue;
        }
    }

    public CodeGenerator createCodeGeneratorClient() {
        var remote = Feign.builder()
                .encoder(new JacksonEncoder(objectMapper))
                .decoder(new JacksonDecoder(objectMapper))
                .decode404()
                .logger(new Slf4jLogger())
                .logLevel(Logger.Level.FULL)
                .target(RemoteCodeGenerator.class, this.codeGeneratorUrl);

        return remote::generateCode;
    }

    public CodeChecker createCodeCheckerClient() {
        var remote = Feign.builder()
                .encoder(new JacksonEncoder(objectMapper))
                .decoder(new JacksonDecoder(objectMapper))
                .decode404()
                .logger(new Slf4jLogger())
                .logLevel(Logger.Level.FULL)
                .target(RemoteCodeChecker.class, this.codeCheckerUrl);

        return (code, guess) -> remote.checkCode(new CodeGuessBody(code, guess)).toResult();
    }

    public TournamentService createTournamentServiceClient() {
        var remote = Feign.builder()
                .encoder(new JacksonEncoder(objectMapper))
                .decoder(new JacksonDecoder(objectMapper))
                .decode404()
                .logger(new Slf4jLogger())
                .logLevel(Logger.Level.FULL)
                .target(RemoteTournamentService.class, this.tournamentServiceUrl);

        return remote::gameEnded;
    }

    interface RemoteCodeGenerator {
        @RequestLine("GET /generate")
        @Headers({"Content-Type: application/json", "Accept: application/json"})
        Code generateCode();
    }

    interface RemoteCodeChecker {
        @RequestLine("POST /check")
        @Headers({"Content-Type: application/json", "Accept: application/json"})
        ResultResponse checkCode(CodeGuessBody body);
    }

    interface RemoteTournamentService {
        String BODY_TEMPLATE = """
            %7B
                "gameId": "{gameId}",
                "result": "{result}",
                "guesses": {guesses}
            %7D
            """;

        @RequestLine("PUT /games/{gameId}/result")
        @Headers({"Content-Type: application/json", "Accept: application/json"})
        @Body(BODY_TEMPLATE)
        void gameEnded(@Param("gameId") UUID gameId, @Param("result") GameStatus result, @Param("guesses") int guesses);
    }
}
