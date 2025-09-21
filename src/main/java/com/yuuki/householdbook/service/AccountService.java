package com.yuuki.householdbook.service;

import com.yuuki.householdbook.entity.Account;
import com.yuuki.householdbook.repository.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.util.Map;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AccountService {

    @Autowired
    private AccountRepository accountRepository;
     public List<Account> getAccountsByMonth(int year, int month) {
        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.withDayOfMonth(start.lengthOfMonth());
        return accountRepository.findByDateBetween(start, end);
    }
    // 一覧取得
    public List<Account> getAllAccounts() {
        return accountRepository.findAll();
    }

    // IDで取得
    public Optional<Account> getAccountById(Long id) {
        return accountRepository.findById(id);
    }

    // 新規登録
    public Account saveAccount(Account account) {
        return accountRepository.save(account);
    }

    // 更新
    public Account updateAccount(Account account) {
        return accountRepository.save(account);
    }

    // 削除
    public void deleteAccount(Long id) {
        accountRepository.deleteById(id);
    }

    // 収支タイプで絞り込み
    public List<Account> getAccountsByType(String type) {
        return accountRepository.findByType(type);
    }

    // カテゴリで絞り込み
    public List<Account> getAccountsByCategory(String category) {
        return accountRepository.findByCategory(category);
    }

    //Serviceに集計メソッドを追加
    public int getMonthlyTotal(String type, int year, int month){
    LocalDate start = LocalDate.of(year, month, 1);
    LocalDate end = start.withDayOfMonth(start.lengthOfMonth());
    List<Account> filtered = accountRepository.findByTypeAndDateBetween(type, start, end);
    return filtered.stream()
        .mapToInt(Account::getAmount)
        .sum();
}

    //カテゴリ別集計メソッドを追加
    public Map<String, Integer> getCategoryTotals(int year, int month) {
    LocalDate start = LocalDate.of(year, month, 1);
    LocalDate end = start.withDayOfMonth(start.lengthOfMonth());

    List<Account> accounts = accountRepository.findByTypeAndDateBetween("expense", start, end);

    return accounts.stream()
        .collect(Collectors.groupingBy(
            Account::getCategory,
            Collectors.summingInt(Account::getAmount)
        ));
}
}


    
