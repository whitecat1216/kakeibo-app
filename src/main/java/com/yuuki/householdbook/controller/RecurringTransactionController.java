package com.yuuki.householdbook.controller;

import com.yuuki.householdbook.entity.AppUser;
import com.yuuki.householdbook.entity.Category;
import com.yuuki.householdbook.entity.PaymentSource;
import com.yuuki.householdbook.entity.RecurringTransaction;
import com.yuuki.householdbook.service.CategoryService;
import com.yuuki.householdbook.service.PaymentSourceService;
import com.yuuki.householdbook.service.RecurringTransactionService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/recurring")
public class RecurringTransactionController {

    private final RecurringTransactionService recurringService;
    private final CategoryService categoryService;
    private final PaymentSourceService paymentSourceService;

    public RecurringTransactionController(RecurringTransactionService recurringService,
                                          CategoryService categoryService,
                                          PaymentSourceService paymentSourceService) {
        this.recurringService = recurringService;
        this.categoryService = categoryService;
        this.paymentSourceService = paymentSourceService;
    }

    @GetMapping
    public String list(HttpSession session, Model model) {
        AppUser user = (AppUser) session.getAttribute("loginUser");
        if (user == null) return "redirect:/login";

        List<RecurringTransaction> recurringList = recurringService.list(user);
        model.addAttribute("recurringList", recurringList);
        return "recurring/list";
    }

    @GetMapping("/new")
    public String newForm(HttpSession session, Model model) {
        AppUser user = (AppUser) session.getAttribute("loginUser");
        if (user == null) return "redirect:/login";

        List<Category> categories = categoryService.list(user);
        List<PaymentSource> sources = paymentSourceService.list(user);
        model.addAttribute("categories", categories);
        model.addAttribute("sources", sources);
        model.addAttribute("recurring", new RecurringTransaction());
        return "recurring/form";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute RecurringTransaction recurring,
                       @RequestParam Long categoryId,
                       @RequestParam(required = false) Long sourceId,
                       HttpSession session) {
        AppUser user = (AppUser) session.getAttribute("loginUser");
        if (user == null) return "redirect:/login";

        Category category = categoryService.findById(categoryId).orElse(null);
        if (category == null || !category.getUser().getId().equals(user.getId())) {
            return "redirect:/recurring";
        }

        PaymentSource source = null;
        if (sourceId != null) {
            source = paymentSourceService.findById(sourceId).orElse(null);
            if (source == null || !source.getUser().getId().equals(user.getId())) {
                return "redirect:/recurring";
            }
        }

        recurring.setUser(user);
        recurring.setCategory(category);
        recurring.setSource(source);
        recurringService.save(recurring);
        return "redirect:/recurring";
    }

    @GetMapping("/edit/{id}")
    public String edit(@PathVariable Long id, HttpSession session, Model model) {
        AppUser user = (AppUser) session.getAttribute("loginUser");
        if (user == null) return "redirect:/login";

        RecurringTransaction recurring = recurringService.findById(id).orElse(null);
        if (recurring == null || !recurring.getUser().getId().equals(user.getId())) {
            return "redirect:/recurring";
        }

        List<Category> categories = categoryService.list(user);
        List<PaymentSource> sources = paymentSourceService.list(user);
        model.addAttribute("categories", categories);
        model.addAttribute("sources", sources);
        model.addAttribute("recurring", recurring);
        return "recurring/form";
    }

    @PostMapping("/update")
    public String update(@ModelAttribute RecurringTransaction recurring,
                         @RequestParam Long categoryId,
                         @RequestParam(required = false) Long sourceId,
                         HttpSession session) {
        AppUser user = (AppUser) session.getAttribute("loginUser");
        if (user == null) return "redirect:/login";

        RecurringTransaction existing = recurringService.findById(recurring.getId()).orElse(null);
        if (existing == null || !existing.getUser().getId().equals(user.getId())) {
            return "redirect:/recurring";
        }

        Category category = categoryService.findById(categoryId).orElse(null);
        if (category == null || !category.getUser().getId().equals(user.getId())) {
            return "redirect:/recurring";
        }

        PaymentSource source = null;
        if (sourceId != null) {
            source = paymentSourceService.findById(sourceId).orElse(null);
            if (source == null || !source.getUser().getId().equals(user.getId())) {
                return "redirect:/recurring";
            }
        }

        existing.setType(recurring.getType());
        existing.setCategory(category);
        existing.setSource(source);
        existing.setItem(recurring.getItem());
        existing.setAmount(recurring.getAmount());
        existing.setMemo(recurring.getMemo());
        existing.setDayOfMonth(recurring.getDayOfMonth());
        existing.setStartDate(recurring.getStartDate());
        existing.setEndDate(recurring.getEndDate());
        existing.setActive(recurring.isActive());
        recurringService.save(existing);
        return "redirect:/recurring";
    }

    @PostMapping("/delete")
    public String delete(@RequestParam Long id, HttpSession session) {
        AppUser user = (AppUser) session.getAttribute("loginUser");
        if (user == null) return "redirect:/login";

        RecurringTransaction recurring = recurringService.findById(id).orElse(null);
        if (recurring == null || !recurring.getUser().getId().equals(user.getId())) {
            return "redirect:/recurring";
        }

        recurringService.delete(recurring);
        return "redirect:/recurring";
    }

    @PostMapping("/toggle")
    public String toggle(@RequestParam Long id, HttpSession session) {
        AppUser user = (AppUser) session.getAttribute("loginUser");
        if (user == null) return "redirect:/login";

        RecurringTransaction recurring = recurringService.findById(id).orElse(null);
        if (recurring == null || !recurring.getUser().getId().equals(user.getId())) {
            return "redirect:/recurring";
        }

        recurring.setActive(!recurring.isActive());
        recurringService.save(recurring);
        return "redirect:/recurring";
    }
}
