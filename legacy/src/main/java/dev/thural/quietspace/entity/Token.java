package dev.thural.quietspace.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.OffsetDateTime;

@Entity
@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class Token extends BaseEntity {

    @NotBlank
    @Column(length = 600, unique = true)
    private String token;

    @Email
    @NotBlank
    private String email;

    @ManyToOne
    @JoinColumn(name = "userId", nullable = false)
    private User user;

    private OffsetDateTime expireDate;
    private OffsetDateTime validateDate;

}