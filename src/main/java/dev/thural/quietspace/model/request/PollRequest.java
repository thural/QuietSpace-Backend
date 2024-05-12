package dev.thural.quietspace.model.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PollRequest {

    private OffsetDateTime dueDate;

    @NotNull
    private List<String> options;

}