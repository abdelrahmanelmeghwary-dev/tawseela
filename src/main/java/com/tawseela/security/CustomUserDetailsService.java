package com.tawseela.security;

import com.tawseela.entity.User;
import com.tawseela.repository.DriverProfileRepository;
import com.tawseela.repository.UserRepository;
import com.tawseela.util.PhoneNormalizer;
import java.util.Optional;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final DriverProfileRepository driverProfileRepository;
    private final UserDetailsFactory userDetailsFactory;

    public CustomUserDetailsService(
            UserRepository userRepository,
            DriverProfileRepository driverProfileRepository,
            UserDetailsFactory userDetailsFactory) {
        this.userRepository = userRepository;
        this.driverProfileRepository = driverProfileRepository;
        this.userDetailsFactory = userDetailsFactory;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) {
        String mobile = PhoneNormalizer.normalize(username);
        if (mobile.isEmpty()) {
            throw new UsernameNotFoundException("Invalid mobile");
        }
        User user = userRepository
                .findByMobileNumberEagerRoles(mobile)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return userDetailsFactory.build(user, driverProfileRepository.findByUser_Id(user.getId()));
    }
}
