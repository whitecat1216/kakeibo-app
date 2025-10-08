package com.yuuki.householdbook.controller;

import com.yuuki.householdbook.entity.AppUser;
import com.yuuki.householdbook.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private UserRepository userRepository;

    // ユーザー一覧表示
    @GetMapping("/users")
    public String userList(HttpSession session, Model model) {
        AppUser loginUser = (AppUser) session.getAttribute("loginUser");
        if (loginUser == null || !"ADMIN".equals(loginUser.getRole())) {
            return "redirect:/access-denied";
        }

        List<AppUser> users = userRepository.findAll();
        model.addAttribute("users", users);
        return "admin/user-list";
    }

    // 管理者昇格処理
    @PostMapping("/promote")
    public String promoteUser(@RequestParam Long userId, HttpSession session) {
        AppUser loginUser = (AppUser) session.getAttribute("loginUser");
        if (loginUser == null || !"ADMIN".equals(loginUser.getRole())) {
            return "redirect:/access-denied";
        }

        AppUser targetUser = userRepository.findById(userId).orElse(null);
        if (targetUser != null && !"ADMIN".equals(targetUser.getRole())) {
            targetUser.setRole("ADMIN");
            userRepository.save(targetUser);
        }

        return "redirect:/admin/users";
    }
}