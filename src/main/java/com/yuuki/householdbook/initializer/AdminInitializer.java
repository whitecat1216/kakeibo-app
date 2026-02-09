package com.yuuki.householdbook.initializer;

import com.yuuki.householdbook.entity.AppUser;
import com.yuuki.householdbook.repository.UserRepository;
import com.yuuki.householdbook.service.CategoryService;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class AdminInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CategoryService categoryService;
    private final String adminUsername;
    private final String adminPassword;
    private final String adminEmail;

    public AdminInitializer(
            UserRepository userRepository,
            CategoryService categoryService,
            @Value("${admin.init.username:admin}") String adminUsername,
            @Value("${admin.init.password:adminpass}") String adminPassword,
            @Value("${admin.init.email:admin@example.com}") String adminEmail) {
        this.userRepository = userRepository;
        this.categoryService = categoryService;
        this.adminUsername = adminUsername;
        this.adminPassword = adminPassword;
        this.adminEmail = adminEmail;
    }

    @Override
    public void run(String... args) {
        if (userRepository.findByUsername(adminUsername) == null) {
            AppUser admin = new AppUser();
            admin.setUsername(adminUsername);
            admin.setPassword(BCrypt.hashpw(adminPassword, BCrypt.gensalt()));
            admin.setRole("ADMIN");
            admin.setEmail(adminEmail);
            userRepository.save(admin);
            categoryService.createDefaultCategories(admin);
            System.out.println("✅ 初期管理者 " + adminUsername + " を登録しました");
        }
    }
}
