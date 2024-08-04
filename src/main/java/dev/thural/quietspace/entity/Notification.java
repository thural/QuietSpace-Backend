package dev.thural.quietspace.entity;

import dev.thural.quietspace.utils.enums.ContentType;
import dev.thural.quietspace.utils.enums.NotificationType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Entity
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Notification extends BaseEntity {

    @NotNull
    private UUID userId;

    @NotNull
    private UUID actorId;

    @NotNull
    private UUID contentId;

    @NotNull
    private Boolean isSeen;

    @Enumerated(EnumType.STRING)
    private ContentType contentType;

    @Enumerated(EnumType.STRING)
    private NotificationType notificationType;

}
