package com.yuuki.householdbook.service;

import com.yuuki.householdbook.entity.Account;
import com.yuuki.householdbook.entity.AppUser;
import com.yuuki.householdbook.entity.RecurringTransaction;
import com.yuuki.householdbook.repository.AccountRepository;
import com.yuuki.householdbook.repository.RecurringTransactionRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

@Service
public class RecurringTransactionService {

    private final RecurringTransactionRepository recurringTransactionRepository;
    private final AccountRepository accountRepository;

    public RecurringTransactionService(RecurringTransactionRepository recurringTransactionRepository,
                                       AccountRepository accountRepository) {
        this.recurringTransactionRepository = recurringTransactionRepository;
        this.accountRepository = accountRepository;
    }

    public List<RecurringTransaction> list(AppUser user) {
        return recurringTransactionRepository.findByUserOrderByIdDesc(user);
    }

    public RecurringTransaction save(RecurringTransaction recurring) {
        return recurringTransactionRepository.save(recurring);
    }

    public Optional<RecurringTransaction> findById(Long id) {
        return recurringTransactionRepository.findById(id);
    }

    public void delete(RecurringTransaction recurring) {
        recurringTransactionRepository.delete(recurring);
    }

    public void generateForMonth(AppUser user, int year, int month) {
        YearMonth ym = YearMonth.of(year, month);
        List<RecurringTransaction> recurringList = list(user);
        for (RecurringTransaction r : recurringList) {
            if (!r.isActive()) continue;

            int day = Math.min(r.getDayOfMonth(), ym.lengthOfMonth());
            LocalDate targetDate = LocalDate.of(year, month, day);

            if (r.getStartDate() != null && targetDate.isBefore(r.getStartDate())) continue;
            if (r.getEndDate() != null && targetDate.isAfter(r.getEndDate())) continue;

            if (accountRepository.existsByUserAndRecurringIdAndDate(user, r.getId(), targetDate)) {
                continue;
            }

            Account account = new Account();
            account.setUser(user);
            account.setDate(targetDate);
            account.setType(r.getType());
            account.setCategory(r.getCategory());
            account.setItem(r.getItem());
            account.setAmount(r.getAmount());
            account.setMemo(r.getMemo());
            account.setRecurringId(r.getId());
            accountRepository.save(account);
        }
    }
}
