package dev.thural.quietspace.websocket.model;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class UserRepresentation {

    private String username;
    private String firstname;
    private String lastname;

    @Email(message = "invalid email format")
    @NotEmpty(message = "email can not be empty")
    @NotNull(message = "email is required")
    @Size(min = 1, max = 32)
    private String email;

}
