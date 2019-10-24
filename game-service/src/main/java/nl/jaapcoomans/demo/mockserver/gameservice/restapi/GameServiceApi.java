package nl.jaapcoomans.demo.mockserver.gameservice.restapi;

import io.javalin.http.Context;
import io.javalin.http.NotFoundResponse;
import nl.jaapcoomans.demo.mockserver.gameservice.domain.Code;
import nl.jaapcoomans.demo.mockserver.gameservice.domain.Game;
import nl.jaapcoomans.demo.mockserver.gameservice.domain.GameService;
import org.eclipse.jetty.http.HttpStatus;

import java.util.List;
import java.util.UUID;

public class GameServiceApi {
    private GameService gameService;

    public GameServiceApi(GameService gameService) {
        this.gameService = gameService;
    }

    public void listAllGames(Context requestContext) {
        List<Game> allGames = this.gameService.findAll();
        requestContext.json(allGames);
    }

    public void findById(Context requestContext) {
        var gameId = UUID.fromString(requestContext.pathParam("id"));
        var game = this.gameService.findById(gameId)
                .map(GameDTO::fromGame)
                .orElseThrow(NotFoundResponse::new);

        requestContext.json(game).status(HttpStatus.OK_200);
    }

    public void createNewGame(Context requestContext) {
        Game newGame = this.gameService.startNewGame();
        requestContext.json(GameDTO.fromGame(newGame));
    }

    public void guessCode(Context requestContext) {
        var gameId = UUID.fromString(requestContext.pathParam("id"));
        var guess = requestContext.bodyAsClass(Code.class);

        var result = this.gameService.guessCode(gameId, guess);

        requestContext.json(result);
    }
}
