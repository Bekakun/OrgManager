package kz.masku.orgmanager.model.dto;

/** JWT token response returned on successful authentication. */
public record LoginResponse(

        String token,
        String tokenType,
        String role,
        String fullName
) {
    public LoginResponse(String token, String role, String fullName) {
        this(token, "Bearer", role, fullName);
    }
}
