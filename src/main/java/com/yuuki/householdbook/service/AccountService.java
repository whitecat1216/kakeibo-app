package com.yuuki.householdbook.service;

import com.yuuki.householdbook.entity.Account;
import com.yuuki.householdbook.entity.AppUser;
import com.yuuki.householdbook.repository.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AccountService {

    @Autowired
    private AccountRepository accountRepository;

    // 月別データ取得
    public List<Account> getAccountsByMonth(AppUser user, int year, int month) {
        return accountRepository.findByUserAndMonth(user, year, month);
    }

    // 全データ取得（ログインユーザーのみ）
    public List<Account> getAllAccounts(AppUser user) {
        return accountRepository.findByUser(user);
    }

    // 登録・更新
    public Account saveAccount(Account account) {
        return accountRepository.save(account);
    }

    // ID取得
    public Optional<Account> getAccountById(Long id) {
        return accountRepository.findById(id);
    }

    // 削除
    public void deleteAccount(Long id) {
        accountRepository.deleteById(id);
    }

    // 月別合計（収入 or 支出）
    public int getMonthlyTotal(AppUser user, String type, int year, int month) {
        List<Account> filtered = accountRepository.findByUserAndTypeAndMonth(user, type, year, month);
        return filtered.stream().mapToInt(Account::getAmount).sum();
    }

    // 支出カテゴリ別集計
    public Map<String, Integer> getCategoryTotals(AppUser user, int year, int month) {
        List<Account> accounts = accountRepository.findByUserAndTypeAndMonth(user, "expense", year, month);
        return accounts.stream()
                .collect(Collectors.groupingBy(
                        Account::getCategory,
                        Collectors.summingInt(Account::getAmount)
                ));
    }

    // 収入カテゴリ別集計
    public Map<String, Integer> getIncomeCategoryTotals(AppUser user, int year, int month) {
        List<Account> accounts = accountRepository.findByUserAndTypeAndMonth(user, "income", year, month);
        Map<String, Integer> totals = new HashMap<>();
        for (Account a : accounts) {
            totals.merge(a.getCategory(), a.getAmount(), Integer::sum);
        }
        return totals;
    }

    // 月別推移グラフ用集計
    public Map<Integer, Integer> getMonthlyTotals(AppUser user, String type, int year) {
        List<Object[]> results = accountRepository.getMonthlyTotalsByUser(user, type, year);
        Map<Integer, Integer> totals = new LinkedHashMap<>();
        for (Object[] row : results) {
            Integer month = (Integer) row[0];
            Integer sum = ((Number) row[1]).intValue();
            totals.put(month, sum);
        }
        return totals;
    }
}