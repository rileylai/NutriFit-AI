package au.edu.sydney.elec5619.prac03.group8.nutrifit_app.dto.Common;

import java.time.LocalDateTime;

/**
 * Generic error response DTO
 */
public class ErrorResponseDTO {
    private String error;
    private String message;
    private String timestamp;
    
    public ErrorResponseDTO() {
        this.timestamp = LocalDateTime.now().toString();
    }
    
    public ErrorResponseDTO(String error, String message) {
        this.error = error;
        this.message = message;
        this.timestamp = LocalDateTime.now().toString();
    }
    
    // Getters and Setters
    public String getError() { return error; }
    public void setError(String error) { this.error = error; }
    
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    
    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
}