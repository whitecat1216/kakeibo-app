package com.yuuki.householdbook.initializer;

import com.yuuki.householdbook.entity.AppUser;
import com.yuuki.householdbook.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import org.mindrot.jbcrypt.BCrypt;

@Component
public class AdminInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Override
    public void run(String... args) {
        if (userRepository.findByUsername("admin") == null) {
            AppUser admin = new AppUser();
            admin.setUsername("admin");
            admin.setPassword(BCrypt.hashpw("adminpass", BCrypt.gensalt()));
            admin.setRole("ADMIN");
            admin.setEmail("admin@example.com"); // ✅ 追加
            userRepository.save(admin);
            System.out.println("✅ 初期管理者 admin を登録しました");
        }
    }
}