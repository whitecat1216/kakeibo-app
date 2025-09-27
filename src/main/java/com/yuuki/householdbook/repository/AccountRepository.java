package com.yuuki.householdbook.repository;

import com.yuuki.householdbook.entity.Account;
import com.yuuki.householdbook.entity.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

    List<Account> findByUser(AppUser user);

    @Query("SELECT a FROM Account a WHERE a.user = :user AND FUNCTION('YEAR', a.date) = :year AND FUNCTION('MONTH', a.date) = :month")
    List<Account> findByUserAndMonth(@Param("user") AppUser user, @Param("year") int year, @Param("month") int month);

    @Query("SELECT a FROM Account a WHERE a.user = :user AND a.type = :type AND FUNCTION('YEAR', a.date) = :year AND FUNCTION('MONTH', a.date) = :month")
    List<Account> findByUserAndTypeAndMonth(@Param("user") AppUser user, @Param("type") String type, @Param("year") int year, @Param("month") int month);

    @Query("SELECT FUNCTION('MONTH', a.date) AS month, SUM(a.amount) FROM Account a WHERE a.user = :user AND a.type = :type AND FUNCTION('YEAR', a.date) = :year GROUP BY FUNCTION('MONTH', a.date)")
    List<Object[]> getMonthlyTotalsByUser(@Param("user") AppUser user, @Param("type") String type, @Param("year") int year);
}