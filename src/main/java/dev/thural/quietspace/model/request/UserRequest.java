package dev.thural.quietspace.model.request;

import jakarta.validation.constraints.*;
import lombok.*;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRequest {

    @NotNull
    @NotBlank
    @Size(min = 1, max = 16)
    private String role;

    @NotNull
    @NotBlank
    @Size(min = 1, max = 32)
    private String username;

    @Email
    @NotNull
    @NotBlank
    @Size(min = 1, max = 32)
    private String email;

    @NotEmpty(message = "password is required")
    @NotNull(message = "password is required")
    @Size(min = 8, max = 32, message = "password length should be in range 8 and 32 characters")
    private String password;

}