package com.yuuki.householdbook.repository;

import com.yuuki.householdbook.entity.AppUser;
import com.yuuki.householdbook.entity.RecurringTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RecurringTransactionRepository extends JpaRepository<RecurringTransaction, Long> {
    List<RecurringTransaction> findByUserOrderByIdDesc(AppUser user);
}
