package sdk.operator.exception;

public class InternalServerError extends RuntimeException {
    private String message;
    public InternalServerError(String message) {
        this.message = message;
    }
}
