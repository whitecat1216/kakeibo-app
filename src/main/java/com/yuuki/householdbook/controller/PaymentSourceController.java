package com.yuuki.householdbook.controller;

import com.yuuki.householdbook.entity.AppUser;
import com.yuuki.householdbook.entity.PaymentSource;
import com.yuuki.householdbook.repository.AccountRepository;
import com.yuuki.householdbook.service.PaymentSourceService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/sources")
public class PaymentSourceController {

    private final PaymentSourceService paymentSourceService;
    private final AccountRepository accountRepository;

    public PaymentSourceController(PaymentSourceService paymentSourceService,
                                   AccountRepository accountRepository) {
        this.paymentSourceService = paymentSourceService;
        this.accountRepository = accountRepository;
    }

    @GetMapping
    public String list(HttpSession session, Model model) {
        AppUser user = (AppUser) session.getAttribute("loginUser");
        if (user == null) return "redirect:/login";

        List<PaymentSource> sources = paymentSourceService.list(user);
        model.addAttribute("sources", sources);
        model.addAttribute("source", new PaymentSource());
        return "sources/list";
    }

    @PostMapping
    public String create(@ModelAttribute PaymentSource source, HttpSession session) {
        AppUser user = (AppUser) session.getAttribute("loginUser");
        if (user == null) return "redirect:/login";

        source.setUser(user);
        paymentSourceService.save(source);
        return "redirect:/sources";
    }

    @GetMapping("/edit/{id}")
    public String edit(@PathVariable Long id, HttpSession session, Model model) {
        AppUser user = (AppUser) session.getAttribute("loginUser");
        if (user == null) return "redirect:/login";

        PaymentSource source = paymentSourceService.findById(id).orElse(null);
        if (source == null || !source.getUser().getId().equals(user.getId())) {
            return "redirect:/sources";
        }

        model.addAttribute("source", source);
        return "sources/form";
    }

    @PostMapping("/update")
    public String update(@ModelAttribute PaymentSource source, HttpSession session) {
        AppUser user = (AppUser) session.getAttribute("loginUser");
        if (user == null) return "redirect:/login";

        PaymentSource existing = paymentSourceService.findById(source.getId()).orElse(null);
        if (existing == null || !existing.getUser().getId().equals(user.getId())) {
            return "redirect:/sources";
        }

        existing.setName(source.getName());
        existing.setType(source.getType());
        existing.setInitialBalance(source.getInitialBalance());
        paymentSourceService.save(existing);
        return "redirect:/sources";
    }

    @PostMapping("/delete")
    public String delete(@RequestParam Long id, HttpSession session, Model model) {
        AppUser user = (AppUser) session.getAttribute("loginUser");
        if (user == null) return "redirect:/login";

        PaymentSource source = paymentSourceService.findById(id).orElse(null);
        if (source == null || !source.getUser().getId().equals(user.getId())) {
            return "redirect:/sources";
        }

        long usedCount = accountRepository.countByUserAndSource(user, source);
        if (usedCount > 0) {
            model.addAttribute("deleteError", "この支払方法は既に使われています。先に明細の支払方法を変更してください。");
            model.addAttribute("sources", paymentSourceService.list(user));
            model.addAttribute("source", new PaymentSource());
            return "sources/list";
        }

        paymentSourceService.delete(source);
        return "redirect:/sources";
    }
}
