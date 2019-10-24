package nl.jaapcoomans.demo.mockserver.gameservice.domain;

public class IllegalGameSateException extends RuntimeException {
    IllegalGameSateException(String message) {
        super(message);
    }
}
