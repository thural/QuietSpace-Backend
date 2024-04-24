package dev.thural.quietspacebackend.model.response;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TokenResponse {

    private UUID id;

    @NotNull
    @NotBlank
    private String jwtToken;

}