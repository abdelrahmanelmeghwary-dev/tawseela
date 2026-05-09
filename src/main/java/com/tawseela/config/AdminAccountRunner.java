package com.tawseela.config;

import com.tawseela.entity.RoleEntity;
import com.tawseela.entity.SystemRole;
import com.tawseela.entity.User;
import com.tawseela.repository.RoleRepository;
import com.tawseela.repository.UserRepository;
import com.tawseela.util.PhoneNormalizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@Order(1)
public class AdminAccountRunner implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminAccountRunner.class);

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final TawseelaProperties props;

    public AdminAccountRunner(
            UserRepository userRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder,
            TawseelaProperties props) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.props = props;
    }

    @Override
    public void run(String... args) {
        String mobile = PhoneNormalizer.normalize(props.getAdminBootstrap().getMobile());
        if (!StringUtils.hasText(mobile)) {
            mobile = "admin";
        }
        if (userRepository.existsByMobileNumber(mobile)) {
            return;
        }
        String envPwd = System.getenv("TAWSEELA_ADMIN_DEFAULT_PASSWORD");
        String rawPassword =
                StringUtils.hasText(envPwd) ? envPwd.trim() : props.getAdminBootstrap().getDefaultPassword();
        if (!StringUtils.hasText(rawPassword)) {
            rawPassword = "ChangeMe1!Strong";
        }
        User admin = new User();
        admin.setFirstName("System");
        admin.setLastName("Admin");
        admin.setMobileNumber(mobile);
        admin.setPasswordHash(passwordEncoder.encode(rawPassword));
        admin.setEnabled(true);
        admin.setPhoneVerified(true);
        RoleEntity adminRole = roleRepository
                .findByName(SystemRole.ADMIN)
                .orElseThrow(() -> new IllegalStateException("ADMIN role missing — check Flyway seed"));
        admin.getRoles().add(adminRole);
        userRepository.save(admin);
        log.warn(
                "Default admin created with mobile [{}]. Change password immediately in production.",
                mobile);
    }
}
