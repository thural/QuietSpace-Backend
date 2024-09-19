package dev.thural.quietspace.websocket.event.message;

import com.fasterxml.jackson.annotation.JsonInclude;
import dev.thural.quietspace.enums.EventType;
import jakarta.persistence.MappedSuperclass;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@MappedSuperclass
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BaseEvent {

    String message;
    Object eventBody;

    @NotNull
    EventType type;

}
