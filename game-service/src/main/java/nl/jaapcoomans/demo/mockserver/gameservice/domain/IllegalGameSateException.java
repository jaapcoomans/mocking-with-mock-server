package nl.jaapcoomans.demo.mockserver.gameservice.domain;

class IllegalGameSateException extends RuntimeException {
    IllegalGameSateException(String message) {
        super(message);
    }
}
