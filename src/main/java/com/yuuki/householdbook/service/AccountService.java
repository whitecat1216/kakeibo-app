package com.yuuki.householdbook.service;

import com.yuuki.householdbook.entity.Account;
import com.yuuki.householdbook.entity.AppUser;
import com.yuuki.householdbook.repository.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class AccountService {

    @Autowired
    private AccountRepository accountRepository;

    // 月別データ取得
    public List<Account> getAccountsByMonth(AppUser user, int year, int month) {
        return accountRepository.findByUserAndMonth(user.getId(), year, month);
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

    // 所有者チェック付きの削除処理
    public void deleteAccountByUser(Long id, AppUser user) {
        Optional<Account> optionalAccount = accountRepository.findById(id);
        if (optionalAccount.isPresent()) {
            Account account = optionalAccount.get();
            if (account.getUser().getId().equals(user.getId())) {
                accountRepository.delete(account);
            }
        }
    }

    // 月別合計（収入 or 支出）
    public int getMonthlyTotal(AppUser user, String type, int year, int month) {
        List<Account> filtered = accountRepository.findByUserAndTypeAndMonth(user.getId(), type, year, month);
        return filtered.stream().mapToInt(Account::getAmount).sum();
    }

    // 支出カテゴリ別集計
    public Map<String, Integer> getCategoryTotals(AppUser user, int year, int month) {
        List<Account> accounts = accountRepository.findByUserAndTypeAndMonth(user.getId(), "expense", year, month);
        return accounts.stream()
                .collect(Collectors.groupingBy(
                        Account::getCategory,
                        Collectors.summingInt(Account::getAmount)
                ));
    }

    // 収入カテゴリ別集計
    public Map<String, Integer> getIncomeCategoryTotals(AppUser user, int year, int month) {
        List<Account> accounts = accountRepository.findByUserAndTypeAndMonth(user.getId(), "income", year, month);
        Map<String, Integer> totals = new HashMap<>();
        for (Account a : accounts) {
            totals.merge(a.getCategory(), a.getAmount(), Integer::sum);
        }
        return totals;
    }

    // 月別推移グラフ用集計
    public Map<Integer, Integer> getMonthlyTotals(AppUser user, String type, int year) {
        List<Object[]> results = accountRepository.getMonthlyTotalsByUser(user.getId(), type, year);
        Map<Integer, Integer> totals = new LinkedHashMap<>();
        for (Object[] row : results) {
            Integer month = ((Number) row[0]).intValue();
            Integer sum = ((Number) row[1]).intValue();
            totals.put(month, sum);
        }
        return totals;
    }
}