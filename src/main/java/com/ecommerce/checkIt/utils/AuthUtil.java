package com.ecommerce.checkIt.utils;

import com.ecommerce.checkIt.exceptions.APIException;
import com.ecommerce.checkIt.exceptions.ResourceNotFoundException;
import com.ecommerce.checkIt.model.User;
import com.ecommerce.checkIt.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@Component
public class AuthUtil {
    @Autowired
    UserRepository userRepository;

    public String loggedInEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = userRepository.findByUserName(authentication.getName()).orElseThrow(() -> new UsernameNotFoundException("User Not Found with Username: " + authentication.getName()));
        return user.getEmail();
    }

    public Long loggedInUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = userRepository.findByUserName(authentication.getName()).orElseThrow(() -> new UsernameNotFoundException("User Not Found with Username: " + authentication.getName()));
        return user.getUserId();
    }

    public User loggedInUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return userRepository.findByUserName(authentication.getName()).orElseThrow(() -> new UsernameNotFoundException("User Not Found with Username: " + authentication.getName()));
    }

}
