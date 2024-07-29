package dev.thural.quietspace.authentication.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Builder
public class AuthResponse {

    private UUID id;
    private String token;
    private String userId;
    private String message;

}
