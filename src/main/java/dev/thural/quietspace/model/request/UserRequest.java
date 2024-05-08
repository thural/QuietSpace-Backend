package dev.thural.quietspace.model.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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

    @NotNull
    @NotBlank
    @Size(min = 8, max = 32)
    private String password;

}