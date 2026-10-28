
package in.strikes.devTrack.dto;

public class RegisterResponse {

    private Long id;
    private String username;
    private String email;
    private String message;

    public RegisterResponse(
            Long id, String username, String email, String message) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.message = message;
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getMessage() {
        return message;
    }
}
