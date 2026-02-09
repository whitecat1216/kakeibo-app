package com.yuuki.householdbook.controller;

import com.yuuki.householdbook.entity.AppUser;
import com.yuuki.householdbook.entity.Category;
import com.yuuki.householdbook.repository.AccountRepository;
import com.yuuki.householdbook.repository.RecurringTransactionRepository;
import com.yuuki.householdbook.service.CategoryService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/categories")
public class CategoryController {

    private final CategoryService categoryService;
    private final AccountRepository accountRepository;
    private final RecurringTransactionRepository recurringTransactionRepository;

    public CategoryController(CategoryService categoryService,
                              AccountRepository accountRepository,
                              RecurringTransactionRepository recurringTransactionRepository) {
        this.categoryService = categoryService;
        this.accountRepository = accountRepository;
        this.recurringTransactionRepository = recurringTransactionRepository;
    }

    @GetMapping
    public String list(HttpSession session, Model model) {
        AppUser user = (AppUser) session.getAttribute("loginUser");
        if (user == null) return "redirect:/login";

        List<Category> categories = categoryService.list(user);
        model.addAttribute("categories", categories);
        model.addAttribute("category", new Category());
        return "categories/list";
    }

    @PostMapping
    public String create(@ModelAttribute Category category, HttpSession session) {
        AppUser user = (AppUser) session.getAttribute("loginUser");
        if (user == null) return "redirect:/login";

        category.setUser(user);
        categoryService.save(category);
        return "redirect:/categories";
    }

    @GetMapping("/edit/{id}")
    public String edit(@PathVariable Long id, HttpSession session, Model model) {
        AppUser user = (AppUser) session.getAttribute("loginUser");
        if (user == null) return "redirect:/login";

        Category category = categoryService.findById(id).orElse(null);
        if (category == null || !category.getUser().getId().equals(user.getId())) {
            return "redirect:/categories";
        }

        model.addAttribute("category", category);
        return "categories/form";
    }

    @PostMapping("/update")
    public String update(@ModelAttribute Category category, HttpSession session) {
        AppUser user = (AppUser) session.getAttribute("loginUser");
        if (user == null) return "redirect:/login";

        Category existing = categoryService.findById(category.getId()).orElse(null);
        if (existing == null || !existing.getUser().getId().equals(user.getId())) {
            return "redirect:/categories";
        }

        existing.setName(category.getName());
        existing.setColor(category.getColor());
        existing.setSortOrder(category.getSortOrder());
        existing.setType(category.getType());
        categoryService.save(existing);
        return "redirect:/categories";
    }

    @PostMapping("/delete")
    public String delete(@RequestParam Long id, HttpSession session, Model model) {
        AppUser user = (AppUser) session.getAttribute("loginUser");
        if (user == null) return "redirect:/login";

        Category category = categoryService.findById(id).orElse(null);
        if (category == null || !category.getUser().getId().equals(user.getId())) {
            return "redirect:/categories";
        }

        long usedCount = accountRepository.countByUserAndCategory(user, category);
        long recurringUsed = recurringTransactionRepository.countByUserAndCategory(user, category);
        if (usedCount > 0 || recurringUsed > 0) {
            model.addAttribute("deleteError", "このカテゴリは既に使われています。明細または定期収支のカテゴリを変更してください。");
            model.addAttribute("categories", categoryService.list(user));
            model.addAttribute("category", new Category());
            return "categories/list";
        }

        categoryService.delete(category);
        return "redirect:/categories";
    }

    @PostMapping("/move")
    public String move(@RequestParam Long id, @RequestParam String direction, HttpSession session) {
        AppUser user = (AppUser) session.getAttribute("loginUser");
        if (user == null) return "redirect:/login";

        categoryService.move(user, id, direction);
        return "redirect:/categories";
    }
}
