package com.yuuki.householdbook.repository;

import com.yuuki.householdbook.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

    // 日付で絞り込み
    List<Account> findByDate(LocalDate date);

    // 収支タイプで絞り込み（income / expense）
    List<Account> findByType(String type);

    // カテゴリで絞り込み
    List<Account> findByCategory(String category);

    // 金額の昇順で取得
    List<Account> findAllByOrderByAmountAsc();

    List<Account> findByTypeAndDateBetween(String type, LocalDate start, LocalDate end);

    List<Account> findByDateBetween(LocalDate start, LocalDate end);

}