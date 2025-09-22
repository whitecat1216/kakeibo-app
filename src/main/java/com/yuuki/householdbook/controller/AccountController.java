package com.yuuki.householdbook.controller;

import com.yuuki.householdbook.entity.Account;
import com.yuuki.householdbook.repository.AccountRepository;
import com.yuuki.householdbook.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;
import java.util.List;

@Controller
@RequestMapping("/accounts")
public class AccountController {

    @Autowired
    private AccountService accountService;

    // 一覧表示
    @GetMapping
    public String listAccounts(@RequestParam(required = false) Integer year,
                               @RequestParam(required = false) Integer month,
                               Model model) {

        if (year == null || month == null) {
            LocalDate now = LocalDate.now();
            year = now.getYear();
            month = now.getMonthValue();
        }

        List<Account> all = accountService.getAllAccounts();
        System.out.println("全登録データ一覧:");
        for (Account a : all) {
            System.out.println("日付: " + a.getDate() + ", タイプ: " + a.getType() + ", カテゴリ: " + a.getCategory() + ", 金額: " + a.getAmount());
        }

        int income = accountService.getMonthlyTotal("income", year, month);
        int expense = accountService.getMonthlyTotal("expense", year, month);
        int balance = income - expense;

        Map<String, Integer> categoryTotals = accountService.getCategoryTotals(year, month);
        System.out.println("支出カテゴリ別集計: " + categoryTotals);

        // ✅ 収入カテゴリ別集計を取得
        Map<String, Integer> incomeCategoryTotals = accountService.getIncomeCategoryTotals(year, month);
        System.out.println("収入カテゴリ別集計: " + incomeCategoryTotals);

        List<Account> accounts = accountService.getAccountsByMonth(year, month);

        model.addAttribute("income", income);
        model.addAttribute("expense", expense);
        model.addAttribute("balance", balance);
        model.addAttribute("categoryTotals", categoryTotals); // 支出カテゴリ
        model.addAttribute("incomeCategoryTotals", incomeCategoryTotals); // ✅ 収入カテゴリ
        model.addAttribute("accounts", accounts);
        model.addAttribute("year", year);
        model.addAttribute("month", month);

        return "account/list";
    }

    // 登録フォーム表示
    @GetMapping("/new")
    public String showForm(Model model) {
        model.addAttribute("account", new Account());
        return "account/form";
    }

    // 登録処理
    @PostMapping("/save")
    public String saveAccount(@ModelAttribute Account account) {
        System.out.println("登録されたデータ:");
        System.out.println("日付: " + account.getDate());
        System.out.println("タイプ: " + account.getType());
        System.out.println("カテゴリ: " + account.getCategory());
        System.out.println("金額: " + account.getAmount());

        accountService.saveAccount(account);
        return "redirect:/accounts";
    }

    // 削除処理
    @GetMapping("/delete/{id}")
    public String deleteAccount(@PathVariable Long id) {
        accountService.deleteAccount(id);
        return "redirect:/accounts";
    }
}