package dev.thural.quietspace.model.response;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OptionResponse {

    private UUID id;
    private UUID pollId;
    private String label;
    private String voteShare;

}