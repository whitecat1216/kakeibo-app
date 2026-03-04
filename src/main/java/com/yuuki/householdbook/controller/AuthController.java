package com.yuuki.householdbook.controller;

import com.yuuki.householdbook.entity.AppUser;
import com.yuuki.householdbook.repository.UserRepository;
import com.yuuki.householdbook.service.CategoryService;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;

@Controller
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryService categoryService;

    // ログイン画面表示
    @GetMapping("/login")
    public String showLoginForm() {
        return "login";
    }

    // ログイン処理（メールアドレス認証）
   @PostMapping("/login")
public String login(@RequestParam String identifier,
                    @RequestParam String password,
                    HttpSession session,
                    Model model) {

    AppUser user = null;

    // メールアドレス形式なら email で検索、それ以外は username で検索
    if (identifier.contains("@")) {
        user = userRepository.findByEmail(identifier);
    } else {
        user = userRepository.findByUsername(identifier);
    }

    if (user != null && BCrypt.checkpw(password, user.getPassword())) {
        session.setAttribute("loginUser", user);
        return "redirect:/accounts";
    }

    model.addAttribute("error", "ログインに失敗しました");
    return "login";
}

    // ログアウト処理
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }

    // 登録画面表示
    @GetMapping("/users/register")
    public String showRegisterForm() {
        return "users/register";
    }

    // ユーザー登録処理（メールアドレス付き）
    @PostMapping("/users/register")
    public String register(@RequestParam String username,
                           @RequestParam String email,
                           @RequestParam String password,
                           Model model) {

        if (userRepository.existsByUsername(username)) {
            model.addAttribute("error", "ユーザー名は既に使用されています");
            return "users/register";
        }

        if (userRepository.existsByEmail(email)) {
            model.addAttribute("error", "メールアドレスは既に使用されています");
            return "users/register";
        }

        AppUser user = new AppUser();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(BCrypt.hashpw(password, BCrypt.gensalt()));
        user.setRole("USER"); // 初期ロール
        userRepository.save(user);
        categoryService.createDefaultCategories(user);

        return "redirect:/login?registered";
    }
}
