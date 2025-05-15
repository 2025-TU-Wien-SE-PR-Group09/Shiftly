package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto;

public class MessageResponseDto {

    String message;

    public MessageResponseDto(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
