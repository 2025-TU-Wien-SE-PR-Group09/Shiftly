package at.ac.tuwien.sepr.groupphase.backend.endpoint.dto;

import jakarta.validation.constraints.Size;

public class MessageResponseDto {

    @Size(max = 255, min = 1, message = "Message must be between 1 and 255 characters")
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

    @Override
    public String toString() {
        return "MessageResponseDto{" +
            "message='" + message + '\'' +
            '}';
    }
}
