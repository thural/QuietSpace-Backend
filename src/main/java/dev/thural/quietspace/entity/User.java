package dev.thural.quietspace.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import dev.thural.quietspace.enums.Role;
import dev.thural.quietspace.enums.StatusType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.security.Principal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import static dev.thural.quietspace.enums.StatusType.ONLINE;

@Entity
@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class User extends BaseEntity implements UserDetails, Principal {

    @NotBlank
    @Column(length = 32, unique = true)
    private String username;

    @NotBlank
    @Column(length = 32, unique = true)
    private String email;

    @NotBlank
    @JsonIgnore
    private String password;

    @JsonIgnore
    private UUID photoId;

    @JsonIgnore
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "user_saved_posts",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "post_id")
    )
    private List<Post> savedPosts;

    @NotNull
    @JsonIgnore
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    ProfileSettings profileSettings;

    @JsonIgnore
    @JsonManagedReference
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Post> posts;

    @JsonManagedReference
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Comment> comments;

    @JsonManagedReference
    @ManyToMany(cascade = {CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
    @JoinTable(name = "user_chat",
            joinColumns = @JoinColumn(name = "chat_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "user_id",
                    referencedColumnName = "id"))
    private List<Chat> chats;

    @JsonManagedReference
    @OneToMany(mappedBy = "sender", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Message> messages;


    @JsonIgnore
    @ManyToMany
    @JoinTable(
            name = "user_followings",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "followings_id")
    )
    @Builder.Default
    private List<User> followings = new ArrayList<>();

    @JsonIgnore
    @Builder.Default
    @ManyToMany(mappedBy = "followings")
    private List<User> followers = new ArrayList<>();


    @JsonIgnore
    private String firstname;
    @JsonIgnore
    private String lastname;
    @JsonIgnore
    private OffsetDateTime dateOfBirth;
    @JsonIgnore
    private boolean accountLocked;
    @JsonIgnore
    private boolean enabled;
    @JsonIgnore
    private StatusType statusType;


    @NotNull
    private Role role;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return this.role.getAuthorities();
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return !accountLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    public String fullName() {
        return getFirstname() + " " + getLastname();
    }

    @Override
    public String getName() {
        return username;
    }

    public String getFullName() {
        return firstname + " " + lastname;
    }

    @PrePersist
    void initAccount() {
        setEnabled(true);
        setStatusType(ONLINE);
        setAccountLocked(false);
        setProfileSettings(new ProfileSettings(this));
    }

    @PreRemove
    void onRemove() {
        // TODO: remove photo associated
    }
}