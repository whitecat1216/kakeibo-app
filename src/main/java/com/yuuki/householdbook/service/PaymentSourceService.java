package com.yuuki.householdbook.service;

import com.yuuki.householdbook.entity.AppUser;
import com.yuuki.householdbook.entity.PaymentSource;
import com.yuuki.householdbook.repository.PaymentSourceRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PaymentSourceService {

    private final PaymentSourceRepository paymentSourceRepository;

    public PaymentSourceService(PaymentSourceRepository paymentSourceRepository) {
        this.paymentSourceRepository = paymentSourceRepository;
    }

    public List<PaymentSource> list(AppUser user) {
        return paymentSourceRepository.findByUserOrderByIdAsc(user);
    }

    public PaymentSource save(PaymentSource source) {
        return paymentSourceRepository.save(source);
    }

    public Optional<PaymentSource> findById(Long id) {
        return paymentSourceRepository.findById(id);
    }

    public void delete(PaymentSource source) {
        paymentSourceRepository.delete(source);
    }

    public PaymentSource findByName(AppUser user, String name) {
        if (name == null || name.isBlank()) return null;
        return paymentSourceRepository.findByUserAndNameIgnoreCase(user, name.trim());
    }

    public PaymentSource findOrCreateByName(AppUser user, String name) {
        if (name == null || name.isBlank()) return null;
        PaymentSource existing = paymentSourceRepository.findByUserAndNameIgnoreCase(user, name.trim());
        if (existing != null) return existing;

        PaymentSource source = new PaymentSource();
        source.setUser(user);
        source.setName(name.trim());
        source.setType("other");
        source.setInitialBalance(0);
        return paymentSourceRepository.save(source);
    }
}
