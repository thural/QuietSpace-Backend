package dev.thural.quietspace.model.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VoteRequest {

    @NotNull
    private UUID userId;

    @NotNull
    private UUID postId;

    @NotNull
    private String option;

}