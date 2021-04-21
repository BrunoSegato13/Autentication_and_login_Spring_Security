package com.project.login.appuser;

import com.project.login.registration.token.ConfirmationToken;
import com.project.login.registration.token.ConfirmationTokenService;
import lombok.AllArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@AllArgsConstructor
public class AppUserService implements UserDetailsService {

    private final String USER_NOT_FOUND_MSG = "User with email %s not found";
    private final String EMAIL_EXISTS = "Email already in use";

    private final AppUserRepository userRepository;
    private final BCryptPasswordEncoder cryptPasswordEncoder;
    private final ConfirmationTokenService confirmationTokenService;



    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email)
                .orElseThrow(()-> new UsernameNotFoundException(String.format(USER_NOT_FOUND_MSG, email)));
    }

    public String signUpUser(AppUser appUser) throws IllegalAccessException {
        boolean userExists = userRepository.findByEmail(appUser.getEmail())
                .isPresent();

        if(userExists){
            throw new IllegalAccessException(String.format(EMAIL_EXISTS));
        }
        String encodedPassword = cryptPasswordEncoder.encode(appUser.getPassword());

        appUser.setPassword(encodedPassword);

        userRepository.save(appUser);


        String token = UUID.randomUUID().toString();

        ConfirmationToken confirmationToken = new ConfirmationToken(
                token,
                LocalDateTime.now(),
                LocalDateTime.now().plusMinutes(15),
                appUser

        );

        confirmationTokenService.saveConfirmationToken(confirmationToken);

        //TODO: SEND EMAIL

        return token;
    }

    public int enableAppUser(String email){
        return userRepository.enableAppUser(email);
    }
}
