package com.yuuki.householdbook.repository;

import com.yuuki.householdbook.entity.Account;
import com.yuuki.householdbook.entity.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

    List<Account> findByUser(AppUser user);

    @Query(value = "SELECT * FROM account WHERE user_id = :userId AND EXTRACT(YEAR FROM date) = :year AND EXTRACT(MONTH FROM date) = :month", nativeQuery = true)
    List<Account> findByUserAndMonth(@Param("userId") Long userId, @Param("year") int year, @Param("month") int month);

    @Query(value = "SELECT * FROM account WHERE user_id = :userId AND type = :type AND EXTRACT(YEAR FROM date) = :year AND EXTRACT(MONTH FROM date) = :month", nativeQuery = true)
    List<Account> findByUserAndTypeAndMonth(@Param("userId") Long userId, @Param("type") String type, @Param("year") int year, @Param("month") int month);

    @Query(value = "SELECT EXTRACT(MONTH FROM date) AS month, SUM(amount) FROM account WHERE user_id = :userId AND type = :type AND EXTRACT(YEAR FROM date) = :year GROUP BY EXTRACT(MONTH FROM date)", nativeQuery = true)
    List<Object[]> getMonthlyTotalsByUser(@Param("userId") Long userId, @Param("type") String type, @Param("year") int year);
}