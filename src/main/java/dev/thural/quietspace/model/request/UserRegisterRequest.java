package dev.thural.quietspace.model.request;

import jakarta.validation.constraints.*;
import lombok.*;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRegisterRequest {

    @NotEmpty(message = "username is mandatory")
    @NotNull(message = "username is mandatory")
    @Size(min = 1, max = 32)
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
    @Size(min = 1, max = 32)
    private String email;

    @NotEmpty(message = "password is required")
    @NotNull(message = "password is required")
    @Size(min = 8, max = 32, message = "password length should be in range 8 and 32 characters")
    private String password;

    @NotNull
    @NotBlank
    @Size(min = 1, max = 16)
    private String role; // TODO: to be removed

}