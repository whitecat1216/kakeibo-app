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
}
