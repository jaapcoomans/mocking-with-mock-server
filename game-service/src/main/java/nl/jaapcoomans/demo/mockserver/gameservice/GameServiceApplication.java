package nl.jaapcoomans.demo.mockserver.gameservice;

import io.javalin.Javalin;


public class GameServiceApplication {
    private static final int PORT = 8080;

    public static void main(String[] arg) {
        Javalin application = createApplication();

        application.start(PORT);
    }

    private static Javalin createApplication() {
        return Javalin.create();
    }
}
