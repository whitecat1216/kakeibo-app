package com.yuuki.householdbook.repository;

import com.yuuki.householdbook.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    // 収支タイプと日付範囲で絞り込み
    List<Account> findByTypeAndDateBetween(String type, LocalDate start, LocalDate end);

    // 日付範囲で絞り込み
    List<Account> findByDateBetween(LocalDate start, LocalDate end);

    // 年月と収支タイプで絞り込み（収入カテゴリグラフ用）
    @Query("SELECT a FROM Account a WHERE a.type = :type AND FUNCTION('YEAR', a.date) = :year AND FUNCTION('MONTH', a.date) = :month")
    List<Account> findByTypeAndMonth(@Param("type") String type, @Param("year") int year, @Param("month") int month);
}