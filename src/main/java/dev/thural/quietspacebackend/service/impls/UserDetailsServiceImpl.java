package dev.thural.quietspacebackend.service.impls;

import dev.thural.quietspacebackend.entity.UserEntity;
import dev.thural.quietspacebackend.repository.UserRepository;
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
public class UserDetailsServiceImpl implements UserDetailsService {

    UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<UserEntity> optionalUser = userRepository.findUserEntityByEmail(username);
        if(optionalUser.isEmpty()) throw new UsernameNotFoundException("user not found with the email");

        UserEntity user = optionalUser.get();

        List<GrantedAuthority> authorityList = new ArrayList<>();
        return new User(user.getEmail(),user.getPassword(),authorityList);
    }
}
