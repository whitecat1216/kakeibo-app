package com.yuuki.householdbook.repository;

import com.yuuki.householdbook.entity.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<AppUser, Long> {
    AppUser findByUsername(String username);
    boolean  existsByUsername(String username); // ユーザー名の重複チェック

}