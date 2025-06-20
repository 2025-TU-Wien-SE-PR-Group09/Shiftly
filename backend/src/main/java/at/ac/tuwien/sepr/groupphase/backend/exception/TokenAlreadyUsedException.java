package at.ac.tuwien.sepr.groupphase.backend.exception;

public class TokenAlreadyUsedException extends RuntimeException {
    public TokenAlreadyUsedException(String message) {
        super(message);
    }
}
