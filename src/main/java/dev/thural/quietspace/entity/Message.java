package dev.thural.quietspace.entity;

import dev.thural.quietspace.utils.enums.StatusType;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class Message extends BaseEntity {

    @ManyToOne
    private Chat chat;

    @NotNull
    @ManyToOne
    private User sender;

    @NotNull
    @ManyToOne
    private User recipient;

    @NotNull
    @NotBlank
    private String text;

    @NotNull
    private Boolean seen;

    @PrePersist
    void onCreate() {
        setSeen(false);
    }

}