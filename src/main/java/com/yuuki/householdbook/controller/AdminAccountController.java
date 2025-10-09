package com.yuuki.householdbook.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.yuuki.householdbook.entity.Account;
import com.yuuki.householdbook.entity.AppUser;
import com.yuuki.householdbook.repository.UserRepository;
import com.yuuki.householdbook.service.AccountService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/admin/accounts")
public class AdminAccountController {

    @Autowired
    private AccountService accountService;

    @Autowired
    private UserRepository userRepository;

    @GetMapping
    public String viewUserAccounts(@RequestParam Long userId,
                                   @RequestParam(required = false) Integer year,
                                   @RequestParam(required = false) Integer month,
                                   HttpSession session,
                                   Model model) {

        // 管理者チェック
        AppUser loginUser = (AppUser) session.getAttribute("loginUser");
        if (loginUser == null || !"ADMIN".equals(loginUser.getRole())) {
            return "redirect:/access-denied";
        }

        // 対象ユーザー取得
        AppUser targetUser = userRepository.findById(userId).orElse(null);
        if (targetUser == null) {
            model.addAttribute("error", "指定されたユーザーが存在しません");
            return "admin/account-list";
        }

        // 年月の初期化
        LocalDate now = LocalDate.now();
        year = (year != null) ? year : now.getYear();
        month = (month != null) ? month : now.getMonthValue();

        // 家計簿データ取得
        List<Account> accounts = accountService.getAccountsByMonth(targetUser, year, month);
        int income = accountService.getMonthlyTotal(targetUser, "income", year, month);
        int expense = accountService.getMonthlyTotal(targetUser, "expense", year, month);
        int balance = income - expense;

        // モデルにデータを渡す
        model.addAttribute("accounts", accounts);
        model.addAttribute("income", income);
        model.addAttribute("expense", expense);
        model.addAttribute("balance", balance);
        model.addAttribute("year", year);
        model.addAttribute("month", month);
        model.addAttribute("targetUser", targetUser);

        return "admin/account-list";
    }
}