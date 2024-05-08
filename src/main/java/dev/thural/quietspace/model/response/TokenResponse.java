package dev.thural.quietspace.model.response;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TokenResponse {

    private UUID id;
    private String jwtToken;

}