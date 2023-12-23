package dev.thural.quietspacebackend.service.impls;

import dev.thural.quietspacebackend.entity.UserEntity;
import dev.thural.quietspacebackend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<UserEntity> optionalUser = userRepository.findUserEntityByEmail(username);

        optionalUser.orElseThrow(() -> new UsernameNotFoundException("user not found with the email"));

        UserEntity user = optionalUser.get();

        List<GrantedAuthority> authorityList = new ArrayList<>();
        return new User(user.getEmail(),user.getPassword(),authorityList);
    }
}
