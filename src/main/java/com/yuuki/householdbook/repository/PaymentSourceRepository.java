package com.yuuki.householdbook.repository;

import com.yuuki.householdbook.entity.AppUser;
import com.yuuki.householdbook.entity.PaymentSource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentSourceRepository extends JpaRepository<PaymentSource, Long> {
    List<PaymentSource> findByUserOrderByIdAsc(AppUser user);

    PaymentSource findByUserAndNameIgnoreCase(AppUser user, String name);
}
