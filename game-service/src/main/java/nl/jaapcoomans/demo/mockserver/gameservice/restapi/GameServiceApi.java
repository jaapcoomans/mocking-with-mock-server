package nl.jaapcoomans.demo.mockserver.gameservice.restapi;

import nl.jaapcoomans.demo.mockserver.gameservice.domain.GameService;

public class GameServiceApi {
    private GameService gameService;

    public GameServiceApi(GameService gameService) {
        this.gameService = gameService;
    }


}
