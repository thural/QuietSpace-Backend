package dev.thural.quietspacebackend.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TokenRequest {

    private UUID id;

    @NotNull
    @NotBlank
    private String jwtToken;

}