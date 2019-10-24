package nl.jaapcoomans.demo.mockserver.gameservice.restapi;

public class ErrorDTO {
    private int statusCode;
    private String message;

    public ErrorDTO(int statusCode, String message) {
        this.statusCode = statusCode;
        this.message = message;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getMessage() {
        return message;
    }
}
