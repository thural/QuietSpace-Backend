package dev.thural.quietspace.authentication.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class RegistrationRequest {

    @NotEmpty(message = "username is mandatory")
    @NotNull(message = "username is mandatory")
    private String username;

    @NotEmpty(message = "firstname is mandatory")
    @NotNull(message = "firstname is mandatory")
    private String firstname;

    @NotEmpty(message = "lastname is mandatory")
    @NotNull(message = "lastname is mandatory")
    private String lastname;

    @Email(message = "invalid email format")
    @NotEmpty(message = "email is required")
    @NotNull(message = "email is required")
    private String email;

    @NotEmpty(message = "password is mandatory")
    @NotNull(message = "password is mandatory")
    @Size(min = 8, message = "password should be 8 characters long minimum")
    private String password;

}
