package com.yuuki.householdbook.controller;

import com.yuuki.householdbook.entity.Account;
import com.yuuki.householdbook.entity.AppUser;
import com.yuuki.householdbook.repository.UserRepository;
import com.yuuki.householdbook.service.AccountService;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/accounts")
public class AccountController {

    @Autowired
    private AccountService accountService;

    @Autowired
    private UserRepository userRepository;

    // 一覧表示
    @GetMapping
    public String listAccounts(@RequestParam(required = false) Integer year,
                               @RequestParam(required = false) Integer month,
                               Authentication auth,
                               Model model) {

        AppUser user = userRepository.findByUsername(auth.getName());

        if (year == null || month == null) {
            LocalDate now = LocalDate.now();
            year = now.getYear();
            month = now.getMonthValue();
        }

        List<Account> accounts = accountService.getAccountsByMonth(user, year, month);
        int income = accountService.getMonthlyTotal(user, "income", year, month);
        int expense = accountService.getMonthlyTotal(user, "expense", year, month);
        int balance = income - expense;

        Map<String, Integer> categoryTotals = accountService.getCategoryTotals(user, year, month);
        Map<String, Integer> incomeCategoryTotals = accountService.getIncomeCategoryTotals(user, year, month);
        Map<Integer, Integer> monthlyIncome = accountService.getMonthlyTotals(user, "income", year);
        Map<Integer, Integer> monthlyExpense = accountService.getMonthlyTotals(user, "expense", year);

        model.addAttribute("income", income);
        model.addAttribute("expense", expense);
        model.addAttribute("balance", balance);
        model.addAttribute("categoryTotals", categoryTotals);
        model.addAttribute("incomeCategoryTotals", incomeCategoryTotals);
        model.addAttribute("accounts", accounts);
        model.addAttribute("year", year);
        model.addAttribute("month", month);
        model.addAttribute("monthlyIncome", monthlyIncome);
        model.addAttribute("monthlyExpense", monthlyExpense);

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
    public String saveAccount(@ModelAttribute Account account, Authentication auth) {
        AppUser user = userRepository.findByUsername(auth.getName());
        account.setUser(user);
        accountService.saveAccount(account);
        return "redirect:/accounts";
    }

    // 削除処理
    @GetMapping("/delete/{id}")
    public String deleteAccount(@PathVariable Long id) {
        accountService.deleteAccount(id);
        return "redirect:/accounts";
    }

    // CSV出力処理
    @GetMapping("/export")
    public void exportCsv(@RequestParam(required = false) Integer year,
                          @RequestParam(required = false) Integer month,
                          Authentication auth,
                          HttpServletResponse response) throws IOException {

        AppUser user = userRepository.findByUsername(auth.getName());

        response.setContentType("text/csv; charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=\"accounts.csv\"");

        List<Account> accounts = (year != null && month != null)
                ? accountService.getAccountsByMonth(user, year, month)
                : accountService.getAllAccounts(user);

        try (PrintWriter writer = response.getWriter()) {
            writer.write('\uFEFF'); // Excel対応のBOM
            writer.println("日付,タイプ,カテゴリ,項目,金額,メモ");
            for (Account a : accounts) {
                writer.printf("%s,%s,%s,%s,%d,%s%n",
                        a.getDate(), a.getType(), a.getCategory(), a.getItem(), a.getAmount(), a.getMemo());
            }
        }
    }
}