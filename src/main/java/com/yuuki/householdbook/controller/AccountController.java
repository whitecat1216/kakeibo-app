package com.yuuki.householdbook.controller;

import com.yuuki.householdbook.entity.Account;
import com.yuuki.householdbook.entity.AppUser;
import com.yuuki.householdbook.entity.Category;
import com.yuuki.householdbook.entity.PaymentSource;
import com.yuuki.householdbook.service.AccountService;
import com.yuuki.householdbook.service.CategoryService;
import com.yuuki.householdbook.service.CsvImportService;
import com.yuuki.householdbook.service.PaymentSourceService;
import com.yuuki.householdbook.service.RecurringTransactionService;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/accounts")
public class AccountController {

    @Autowired
    private AccountService accountService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private RecurringTransactionService recurringService;

    @Autowired
    private PaymentSourceService paymentSourceService;

    @Autowired
    private CsvImportService csvImportService;

    @GetMapping
    public String dashboard(@RequestParam(required = false) Integer year,
                            @RequestParam(required = false) Integer month,
                            HttpSession session,
                            Model model) {
        AppUser user = (AppUser) session.getAttribute("loginUser");
        if (user == null) return "redirect:/login";

        LocalDate now = LocalDate.now();
        year = (year != null) ? year : now.getYear();
        month = (month != null) ? month : now.getMonthValue();

        recurringService.generateForMonth(user, year, month);
        int income = accountService.getMonthlyTotal(user, "income", year, month);
        int expense = accountService.getMonthlyTotal(user, "expense", year, month);
        int balance = income - expense;

        model.addAttribute("year", year);
        model.addAttribute("month", month);
        model.addAttribute("income", income);
        model.addAttribute("expense", expense);
        model.addAttribute("balance", balance);
        model.addAttribute("entryCount", accountService.getAccountsByMonth(user, year, month).size());
        model.addAttribute("sourceCount", paymentSourceService.list(user).size());
        model.addAttribute("categoryCount", categoryService.list(user).size());
        return "account/dashboard";
    }

    @GetMapping("/list")
    public String listAccounts(@RequestParam(required = false) Integer year,
                               @RequestParam(required = false) Integer month,
                               @RequestParam(required = false) String type,
                               @RequestParam(required = false) LocalDate startDate,
                               @RequestParam(required = false) LocalDate endDate,
                               @RequestParam(required = false) Long categoryId,
                               @RequestParam(required = false) Long sourceId,
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
        List<Account> accounts = accountService.search(user, type, startDate, endDate, categoryId, sourceId, minAmount, maxAmount, memo);

        model.addAttribute("accounts", accounts);
        model.addAttribute("income", accountService.getMonthlyTotal(user, "income", year, month));
        model.addAttribute("expense", accountService.getMonthlyTotal(user, "expense", year, month));
        model.addAttribute("balance", accountService.getMonthlyTotal(user, "income", year, month) - accountService.getMonthlyTotal(user, "expense", year, month));
        model.addAttribute("year", year);
        model.addAttribute("month", month);
        model.addAttribute("type", type);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("categoryId", categoryId);
        model.addAttribute("sourceId", sourceId);
        model.addAttribute("minAmount", minAmount);
        model.addAttribute("maxAmount", maxAmount);
        model.addAttribute("memo", memo);
        model.addAttribute("categories", categoryService.list(user));
        model.addAttribute("sources", paymentSourceService.list(user));
        return "account/list";
    }

    @GetMapping("/report")
    public String report(@RequestParam(required = false) Integer year,
                         @RequestParam(required = false) Integer month,
                         HttpSession session,
                         Model model) {
        AppUser user = (AppUser) session.getAttribute("loginUser");
        if (user == null) return "redirect:/login";

        LocalDate now = LocalDate.now();
        year = (year != null) ? year : now.getYear();
        month = (month != null) ? month : now.getMonthValue();

        recurringService.generateForMonth(user, year, month);
        int expense = accountService.getMonthlyTotal(user, "expense", year, month);
        int income = accountService.getMonthlyTotal(user, "income", year, month);

        YearMonth currentYm = YearMonth.of(year, month);
        YearMonth prevYm = currentYm.minusMonths(1);
        YearMonth prevYearYm = currentYm.minusYears(1);
        int prevMonthExpense = accountService.getMonthlyTotal(user, "expense", prevYm.getYear(), prevYm.getMonthValue());
        int prevMonthDiff = expense - prevMonthExpense;
        double prevMonthRate = calcRate(prevMonthExpense, prevMonthDiff);
        int prevYearExpense = accountService.getMonthlyTotal(user, "expense", prevYearYm.getYear(), prevYearYm.getMonthValue());
        int prevYearDiff = expense - prevYearExpense;
        double prevYearRate = calcRate(prevYearExpense, prevYearDiff);

        List<PaymentSource> sources = paymentSourceService.list(user);
        Map<Long, Integer> sourceBalances = new LinkedHashMap<>();
        int totalInitial = 0;
        for (PaymentSource s : sources) {
            int net = accountService.getNetTotalBySource(user, s.getId());
            sourceBalances.put(s.getId(), s.getInitialBalance() + net);
            totalInitial += s.getInitialBalance();
        }
        Map<Integer, Integer> monthlyNet = accountService.getMonthlyNetTotals(user, year);
        Map<Integer, Integer> monthlyBalance = new LinkedHashMap<>();
        int running = totalInitial;
        for (int m = 1; m <= 12; m++) {
            running += monthlyNet.getOrDefault(m, 0);
            monthlyBalance.put(m, running);
        }

        Map<String, Integer> currentCategoryTotals = accountService.getCategoryTotals(user, year, month);
        Map<String, Integer> prevCategoryTotals = accountService.getCategoryTotals(user, prevYm.getYear(), prevYm.getMonthValue());
        List<Map.Entry<String, Integer>> topExpenseCategories = currentCategoryTotals.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(5)
                .toList();
        List<AnomalyCategory> anomalyCategories = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : currentCategoryTotals.entrySet()) {
            int previous = prevCategoryTotals.getOrDefault(entry.getKey(), 0);
            int diff = entry.getValue() - previous;
            if (diff <= 0) continue;
            if (previous == 0 && entry.getValue() < 5000) continue;
            double rate = previous == 0 ? 1.0 : (double) diff / previous;
            if (rate >= 0.2 || diff >= 5000) {
                anomalyCategories.add(new AnomalyCategory(entry.getKey(), previous, entry.getValue(), diff, rate));
            }
        }
        anomalyCategories.sort(Comparator.comparingInt(AnomalyCategory::getDiff).reversed());
        if (anomalyCategories.size() > 5) anomalyCategories = anomalyCategories.subList(0, 5);

        model.addAttribute("year", year);
        model.addAttribute("month", month);
        model.addAttribute("income", income);
        model.addAttribute("expense", expense);
        model.addAttribute("prevMonthExpense", prevMonthExpense);
        model.addAttribute("prevMonthDiff", prevMonthDiff);
        model.addAttribute("prevMonthRate", prevMonthRate);
        model.addAttribute("prevYearExpense", prevYearExpense);
        model.addAttribute("prevYearDiff", prevYearDiff);
        model.addAttribute("prevYearRate", prevYearRate);
        model.addAttribute("sources", sources);
        model.addAttribute("sourceBalances", sourceBalances);
        model.addAttribute("categoryTotals", currentCategoryTotals);
        model.addAttribute("incomeCategoryTotals", accountService.getIncomeCategoryTotals(user, year, month));
        model.addAttribute("monthlyIncome", accountService.getMonthlyTotals(user, "income", year));
        model.addAttribute("monthlyExpense", accountService.getMonthlyTotals(user, "expense", year));
        model.addAttribute("monthlyBalance", monthlyBalance);
        model.addAttribute("topExpenseCategories", topExpenseCategories);
        model.addAttribute("anomalyCategories", anomalyCategories);
        return "account/report";
    }

    @GetMapping("/yearly")
    public String yearly(@RequestParam(required = false) Integer year,
                         HttpSession session,
                         Model model) {
        AppUser user = (AppUser) session.getAttribute("loginUser");
        if (user == null) return "redirect:/login";

        int targetYear = (year != null) ? year : LocalDate.now().getYear();
        Map<Integer, Integer> monthlyIncomeRaw = accountService.getMonthlyTotals(user, "income", targetYear);
        Map<Integer, Integer> monthlyExpenseRaw = accountService.getMonthlyTotals(user, "expense", targetYear);

        Map<Integer, Integer> monthlyIncome = new LinkedHashMap<>();
        Map<Integer, Integer> monthlyExpense = new LinkedHashMap<>();
        Map<Integer, Integer> monthlyBalance = new LinkedHashMap<>();
        List<MonthlyYearSummary> monthlyRows = new ArrayList<>();
        int annualIncome = 0;
        int annualExpense = 0;
        int cumulative = 0;

        for (int m = 1; m <= 12; m++) {
            int income = monthlyIncomeRaw.getOrDefault(m, 0);
            int expense = monthlyExpenseRaw.getOrDefault(m, 0);
            int balance = income - expense;
            cumulative += balance;

            monthlyIncome.put(m, income);
            monthlyExpense.put(m, expense);
            monthlyBalance.put(m, cumulative);
            monthlyRows.add(new MonthlyYearSummary(m, income, expense, balance, cumulative));

            annualIncome += income;
            annualExpense += expense;
        }

        model.addAttribute("year", targetYear);
        model.addAttribute("annualIncome", annualIncome);
        model.addAttribute("annualExpense", annualExpense);
        model.addAttribute("annualBalance", annualIncome - annualExpense);
        model.addAttribute("monthlyRows", monthlyRows);
        model.addAttribute("monthlyIncome", monthlyIncome);
        model.addAttribute("monthlyExpense", monthlyExpense);
        model.addAttribute("monthlyBalance", monthlyBalance);
        return "account/yearly";
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
        model.addAttribute("sources", paymentSourceService.list(user));
        return "account/form";
    }

    // 登録処理
    @PostMapping("/save")
    public String saveAccount(@ModelAttribute Account account,
                              @RequestParam Long categoryId,
                              @RequestParam(required = false) Long sourceId,
                              HttpSession session) {
        AppUser user = (AppUser) session.getAttribute("loginUser");
        if (user == null) return "redirect:/login";

        Category category = categoryService.findById(categoryId).orElse(null);
        if (category == null || !category.getUser().getId().equals(user.getId())) {
            return "redirect:/accounts/list";
        }

        PaymentSource source = null;
        if (sourceId != null) {
            source = paymentSourceService.findById(sourceId).orElse(null);
            if (source == null || !source.getUser().getId().equals(user.getId())) {
                return "redirect:/accounts/list";
            }
        }

        account.setUser(user);
        account.setCategory(category);
        account.setSource(source);
        accountService.saveAccount(account);
        return "redirect:/accounts/list";
    }

    // 編集フォーム表示
    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, HttpSession session, Model model) {
        AppUser user = (AppUser) session.getAttribute("loginUser");
        if (user == null) return "redirect:/login";

        Account account = accountService.getAccountById(id).orElse(null);
        if (account == null || !account.getUser().getId().equals(user.getId())) {
            return "redirect:/accounts/list";
        }

        model.addAttribute("account", account);
        model.addAttribute("categories", categoryService.list(user));
        model.addAttribute("sources", paymentSourceService.list(user));
        return "account/form";
    }

    // 更新処理
    @PostMapping("/update")
    public String updateAccount(@ModelAttribute Account account,
                                @RequestParam Long categoryId,
                                @RequestParam(required = false) Long sourceId,
                                HttpSession session) {
        AppUser user = (AppUser) session.getAttribute("loginUser");
        if (user == null) return "redirect:/login";

        Account existing = accountService.getAccountById(account.getId()).orElse(null);
        if (existing == null || !existing.getUser().getId().equals(user.getId())) {
            return "redirect:/accounts/list";
        }

        Category category = categoryService.findById(categoryId).orElse(null);
        if (category == null || !category.getUser().getId().equals(user.getId())) {
            return "redirect:/accounts/list";
        }

        PaymentSource source = null;
        if (sourceId != null) {
            source = paymentSourceService.findById(sourceId).orElse(null);
            if (source == null || !source.getUser().getId().equals(user.getId())) {
                return "redirect:/accounts/list";
            }
        }

        existing.setDate(account.getDate());
        existing.setType(account.getType());
        existing.setCategory(category);
        existing.setSource(source);
        existing.setItem(account.getItem());
        existing.setAmount(account.getAmount());
        existing.setMemo(account.getMemo());
        accountService.saveAccount(existing);
        return "redirect:/accounts/list";
    }

    // 複製処理
    @GetMapping("/duplicate/{id}")
    public String duplicate(@PathVariable Long id, HttpSession session) {
        AppUser user = (AppUser) session.getAttribute("loginUser");
        if (user == null) return "redirect:/login";

        Account existing = accountService.getAccountById(id).orElse(null);
        if (existing == null || !existing.getUser().getId().equals(user.getId())) {
            return "redirect:/accounts/list";
        }

        Account copy = new Account();
        copy.setUser(user);
        copy.setDate(LocalDate.now());
        copy.setType(existing.getType());
        copy.setCategory(existing.getCategory());
        copy.setSource(existing.getSource());
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
        return "redirect:/accounts/list";
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
        return "redirect:/accounts/list";
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
            writer.println("日付,タイプ,カテゴリ,支払方法,項目,金額,メモ");
            for (Account a : accounts) {
                String categoryName = a.getCategory() != null ? a.getCategory().getName() : "";
                String sourceName = a.getSource() != null ? a.getSource().getName() : "";
                writer.printf("%s,%s,%s,%s,%s,%d,%s%n",
                        a.getDate(), a.getType(), categoryName, sourceName, a.getItem(), a.getAmount(), a.getMemo());
            }
        }
    }

    @GetMapping("/import")
    public String showImportForm(HttpSession session) {
        AppUser user = (AppUser) session.getAttribute("loginUser");
        if (user == null) return "redirect:/login";
        return "account/import";
    }

    @PostMapping("/import")
    public String importCsv(@RequestParam("file") MultipartFile file,
                            @RequestParam(defaultValue = "true") boolean skipDuplicates,
                            HttpSession session,
                            Model model) {
        AppUser user = (AppUser) session.getAttribute("loginUser");
        if (user == null) return "redirect:/login";

        try {
            CsvImportService.CsvImportResult result = csvImportService.importCsv(user, file, skipDuplicates);
            model.addAttribute("result", result);
        } catch (Exception e) {
            model.addAttribute("importError", e.getMessage());
        }
        return "account/import";
    }

    private double calcRate(int base, int diff) {
        if (base <= 0) {
            return diff > 0 ? 1.0 : 0.0;
        }
        return (double) diff / base;
    }

    public static class AnomalyCategory {
        private final String name;
        private final int previous;
        private final int current;
        private final int diff;
        private final double rate;

        public AnomalyCategory(String name, int previous, int current, int diff, double rate) {
            this.name = name;
            this.previous = previous;
            this.current = current;
            this.diff = diff;
            this.rate = rate;
        }

        public String getName() { return name; }
        public int getPrevious() { return previous; }
        public int getCurrent() { return current; }
        public int getDiff() { return diff; }
        public double getRate() { return rate; }
    }

    public static class MonthlyYearSummary {
        private final int month;
        private final int income;
        private final int expense;
        private final int balance;
        private final int cumulative;

        public MonthlyYearSummary(int month, int income, int expense, int balance, int cumulative) {
            this.month = month;
            this.income = income;
            this.expense = expense;
            this.balance = balance;
            this.cumulative = cumulative;
        }

        public int getMonth() { return month; }
        public int getIncome() { return income; }
        public int getExpense() { return expense; }
        public int getBalance() { return balance; }
        public int getCumulative() { return cumulative; }
    }
}
