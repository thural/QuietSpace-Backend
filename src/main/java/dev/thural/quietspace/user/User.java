package dev.thural.quietspace.user;
import dev.thural.quietspace.entity.Chat;
import dev.thural.quietspace.entity.Comment;
import dev.thural.quietspace.entity.Message;
import dev.thural.quietspace.entity.Post;
import dev.thural.quietspace.shared.entity.BaseEntity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import dev.thural.quietspace.shared.enums.Role;
import dev.thural.quietspace.shared.enums.StatusType;
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

import static dev.thural.quietspace.shared.enums.StatusType.ONLINE;

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
    @Builder.Default
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "user_saved_posts",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "post_id")
    )
    private List<Post> savedPosts = new ArrayList<>();

    @JsonIgnore
    @Builder.Default
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    ProfileSettings profileSettings = new ProfileSettings();

    @JsonIgnore
    @Builder.Default
    @JsonManagedReference
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Post> posts = new ArrayList<>();

    @Builder.Default
    @JsonManagedReference
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Comment> comments = new ArrayList<>();

    @Builder.Default
    @JsonManagedReference
    @ManyToMany(cascade = {CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
    @JoinTable(name = "user_chat",
            joinColumns = @JoinColumn(name = "chat_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "user_id",
                    referencedColumnName = "id"))
    private List<Chat> chats = new ArrayList<>();

    @Builder.Default
    @JsonManagedReference
    @OneToMany(mappedBy = "sender", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Message> messages = new ArrayList<>();


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

    @PreRemove
    void onRemove() {
        // TODO: remove photo associated
    }

    @PrePersist
    @PreUpdate
    void ensureProfileSettings() {
        if (profileSettings == null) {
            profileSettings = new ProfileSettings();
        }
        if (profileSettings.getUser() == null) {
            profileSettings.setUser(this);
        }
    }
}