package dev.thural.quietspace.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.Column;
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
    @JsonBackReference
    private Chat chat;

    @NotNull
    @ManyToOne
    @JsonBackReference
    private User sender;

    @NotNull
    @ManyToOne
    private User recipient;

    @NotBlank
    @Column(length = 999)
    private String text;

    @NotNull
    private Boolean isSeen;

    @PrePersist
    void initFields() {
        setIsSeen(false);
    }

}