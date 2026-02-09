package com.yuuki.householdbook.controller;

import com.yuuki.householdbook.entity.Account;
import com.yuuki.householdbook.entity.AppUser;
import com.yuuki.householdbook.entity.Category;
import com.yuuki.householdbook.repository.UserRepository;
import com.yuuki.householdbook.service.AccountService;
import com.yuuki.householdbook.service.CategoryService;
import com.yuuki.householdbook.service.RecurringTransactionService;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Controller
@RequestMapping("/accounts")
public class AccountController {

    @Autowired
    private AccountService accountService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private RecurringTransactionService recurringService;

    // 家計簿一覧表示
    @GetMapping
    public String listAccounts(@RequestParam(required = false) Integer year,
                               @RequestParam(required = false) Integer month,
                               @RequestParam(required = false) String type,
                               @RequestParam(required = false) LocalDate startDate,
                               @RequestParam(required = false) LocalDate endDate,
                               @RequestParam(required = false) Long categoryId,
                               @RequestParam(required = false) Integer minAmount,
                               @RequestParam(required = false) Integer maxAmount,
                               @RequestParam(required = false) String memo,
                               HttpSession session,
                               Model model) {

        AppUser user = (AppUser) session.getAttribute("loginUser");
        if (user == null) return "redirect:/login";

        LocalDate now = LocalDate.now();
        year = (year != null) ? year : now.getYear();
        month = (month != null) ? month : now.getMonthValue();

        if (startDate == null && endDate == null) {
            YearMonth ym = YearMonth.of(year, month);
            startDate = ym.atDay(1);
            endDate = ym.atEndOfMonth();
        }

        recurringService.generateForMonth(user, year, month);

        List<Account> accounts = accountService.search(
                user,
                type,
                startDate,
                endDate,
                categoryId,
                minAmount,
                maxAmount,
                memo
        );
        int income = accountService.getMonthlyTotal(user, "income", year, month);
        int expense = accountService.getMonthlyTotal(user, "expense", year, month);
        int balance = income - expense;

        List<Category> categories = categoryService.list(user);
        model.addAttribute("accounts", accounts);
        model.addAttribute("income", income);
        model.addAttribute("expense", expense);
        model.addAttribute("balance", balance);
        model.addAttribute("year", year);
        model.addAttribute("month", month);
        model.addAttribute("type", type);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("categoryId", categoryId);
        model.addAttribute("minAmount", minAmount);
        model.addAttribute("maxAmount", maxAmount);
        model.addAttribute("memo", memo);
        model.addAttribute("categories", categories);
        model.addAttribute("categoryTotals", accountService.getCategoryTotals(user, year, month));
        model.addAttribute("incomeCategoryTotals", accountService.getIncomeCategoryTotals(user, year, month));
        model.addAttribute("monthlyIncome", accountService.getMonthlyTotals(user, "income", year));
        model.addAttribute("monthlyExpense", accountService.getMonthlyTotals(user, "expense", year));

        return "account/list";
    }

    // 登録フォーム表示
    @GetMapping("/new")
    public String showForm(HttpSession session, Model model) {
        AppUser user = (AppUser) session.getAttribute("loginUser");
        if (user == null) return "redirect:/login";

        Account account = new Account();
        account.setDate(LocalDate.now());
        model.addAttribute("account", account);
        model.addAttribute("categories", categoryService.list(user));
        return "account/form";
    }

    // 登録処理
    @PostMapping("/save")
    public String saveAccount(@ModelAttribute Account account,
                              @RequestParam Long categoryId,
                              HttpSession session) {
        AppUser user = (AppUser) session.getAttribute("loginUser");
        if (user == null) return "redirect:/login";

        Category category = categoryService.findById(categoryId).orElse(null);
        if (category == null || !category.getUser().getId().equals(user.getId())) {
            return "redirect:/accounts";
        }

        account.setUser(user);
        account.setCategory(category);
        accountService.saveAccount(account);
        return "redirect:/accounts";
    }

    // 編集フォーム表示
    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, HttpSession session, Model model) {
        AppUser user = (AppUser) session.getAttribute("loginUser");
        if (user == null) return "redirect:/login";

        Account account = accountService.getAccountById(id).orElse(null);
        if (account == null || !account.getUser().getId().equals(user.getId())) {
            return "redirect:/accounts";
        }

        model.addAttribute("account", account);
        model.addAttribute("categories", categoryService.list(user));
        return "account/form";
    }

    // 更新処理
    @PostMapping("/update")
    public String updateAccount(@ModelAttribute Account account,
                                @RequestParam Long categoryId,
                                HttpSession session) {
        AppUser user = (AppUser) session.getAttribute("loginUser");
        if (user == null) return "redirect:/login";

        Account existing = accountService.getAccountById(account.getId()).orElse(null);
        if (existing == null || !existing.getUser().getId().equals(user.getId())) {
            return "redirect:/accounts";
        }

        Category category = categoryService.findById(categoryId).orElse(null);
        if (category == null || !category.getUser().getId().equals(user.getId())) {
            return "redirect:/accounts";
        }

        existing.setDate(account.getDate());
        existing.setType(account.getType());
        existing.setCategory(category);
        existing.setItem(account.getItem());
        existing.setAmount(account.getAmount());
        existing.setMemo(account.getMemo());
        accountService.saveAccount(existing);
        return "redirect:/accounts";
    }

    // 複製処理
    @GetMapping("/duplicate/{id}")
    public String duplicate(@PathVariable Long id, HttpSession session) {
        AppUser user = (AppUser) session.getAttribute("loginUser");
        if (user == null) return "redirect:/login";

        Account existing = accountService.getAccountById(id).orElse(null);
        if (existing == null || !existing.getUser().getId().equals(user.getId())) {
            return "redirect:/accounts";
        }

        Account copy = new Account();
        copy.setUser(user);
        copy.setDate(LocalDate.now());
        copy.setType(existing.getType());
        copy.setCategory(existing.getCategory());
        copy.setItem(existing.getItem());
        copy.setAmount(existing.getAmount());
        copy.setMemo(existing.getMemo());
        Account saved = accountService.saveAccount(copy);
        return "redirect:/accounts/edit/" + saved.getId();
    }

    // 削除処理
    @GetMapping("/delete/{id}")
    public String deleteAccount(@PathVariable Long id, HttpSession session) {
        AppUser user = (AppUser) session.getAttribute("loginUser");
        if (user == null) return "redirect:/login";

        accountService.deleteAccountByUser(id, user);
        return "redirect:/accounts";
    }

    // 一括削除
    @PostMapping("/bulk-delete")
    public String bulkDelete(@RequestParam(required = false) List<Long> ids, HttpSession session) {
        AppUser user = (AppUser) session.getAttribute("loginUser");
        if (user == null) return "redirect:/login";

        if (ids != null) {
            for (Long id : ids) {
                accountService.deleteAccountByUser(id, user);
            }
        }
        return "redirect:/accounts";
    }

    // CSV出力処理
    @GetMapping("/export")
    public void exportCsv(@RequestParam(required = false) Integer year,
                          @RequestParam(required = false) Integer month,
                          HttpSession session,
                          HttpServletResponse response) throws IOException {

        AppUser user = (AppUser) session.getAttribute("loginUser");
        if (user == null) {
            response.sendRedirect("/login");
            return;
        }

        List<Account> accounts = (year != null && month != null)
                ? accountService.getAccountsByMonth(user, year, month)
                : accountService.getAllAccounts(user);

        String filename = (year != null && month != null)
                ? String.format("accounts_%d_%02d.csv", year, month)
                : "accounts.csv";

        response.setContentType("text/csv; charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");

        try (PrintWriter writer = response.getWriter()) {
            writer.write('\uFEFF'); // Excel対応のBOM
            writer.println("日付,タイプ,カテゴリ,項目,金額,メモ");
            for (Account a : accounts) {
                String categoryName = a.getCategory() != null ? a.getCategory().getName() : "";
                writer.printf("%s,%s,%s,%s,%d,%s%n",
                        a.getDate(), a.getType(), categoryName, a.getItem(), a.getAmount(), a.getMemo());
            }
        }
    }
}
