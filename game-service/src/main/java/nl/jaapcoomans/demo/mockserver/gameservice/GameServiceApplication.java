package nl.jaapcoomans.demo.mockserver.gameservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.ExceptionHandler;
import nl.jaapcoomans.demo.mockserver.gameservice.domain.GameService;
import nl.jaapcoomans.demo.mockserver.gameservice.domain.IllegalGameSateException;
import nl.jaapcoomans.demo.mockserver.gameservice.domain.TournamentService;
import nl.jaapcoomans.demo.mockserver.gameservice.persist.InMemoryGameRepository;
import nl.jaapcoomans.demo.mockserver.gameservice.remote.RemoteServiceClientFactory;
import nl.jaapcoomans.demo.mockserver.gameservice.restapi.ErrorDTO;
import nl.jaapcoomans.demo.mockserver.gameservice.restapi.GameServiceApi;

import java.net.HttpURLConnection;

import static io.javalin.apibuilder.ApiBuilder.get;
import static io.javalin.apibuilder.ApiBuilder.path;
import static io.javalin.apibuilder.ApiBuilder.post;


public class GameServiceApplication {
    private static final int PORT = 8080;

    public static void main(String[] arg) {
        var objectMapper = objectMapper();

        var remoteClientFactory = new RemoteServiceClientFactory(objectMapper);

        var codeGenerator = remoteClientFactory.createCodeGeneratorClient();
        var codeChecker = remoteClientFactory.createCodeCheckerClient();
        var tournamentService = new TournamentService() {

        };

        var repository = new InMemoryGameRepository();
        var gameService = new GameService(repository, codeGenerator, codeChecker, tournamentService);
        var api = new GameServiceApi(gameService);

        Javalin.create()
                .routes(() -> {
                    path("/games", () -> {
                        post(api::createNewGame);
                        get(api::listAllGames);
                        get("/:id", api::findById);
                        post("/:id/guess", api::guessCode);
                    });
                })
                .exception(RuntimeException.class, new ExceptionMapper<>(HttpURLConnection.HTTP_BAD_REQUEST))
                .exception(IllegalGameSateException.class, new ExceptionMapper<>(HttpURLConnection.HTTP_BAD_REQUEST))
                .start(PORT);
    }

    private static ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.registerModule(new ParameterNamesModule());
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        return objectMapper;
    }

    private static class ExceptionMapper<T extends Exception> implements ExceptionHandler<T> {
        private int statusCode;

        private ExceptionMapper(int statusCode) {
            this.statusCode = statusCode;
        }

        @Override
        public void handle(T exception, Context ctx) {
            ctx.json(new ErrorDTO(statusCode, exception.getMessage())).status(statusCode);
        }
    }
}
