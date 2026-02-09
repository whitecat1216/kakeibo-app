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

    @Query(value = "SELECT * FROM account WHERE user_id = :userId AND EXTRACT(YEAR FROM date) = :year AND EXTRACT(MONTH FROM date) = :month", nativeQuery = true)
    List<Account> findByUserAndMonth(@Param("userId") Long userId, @Param("year") int year, @Param("month") int month);

    @Query(value = "SELECT * FROM account WHERE user_id = :userId AND type = :type AND EXTRACT(YEAR FROM date) = :year AND EXTRACT(MONTH FROM date) = :month", nativeQuery = true)
    List<Account> findByUserAndTypeAndMonth(@Param("userId") Long userId, @Param("type") String type, @Param("year") int year, @Param("month") int month);

    @Query(value = "SELECT EXTRACT(MONTH FROM date) AS month, SUM(amount) FROM account WHERE user_id = :userId AND type = :type AND EXTRACT(YEAR FROM date) = :year GROUP BY EXTRACT(MONTH FROM date)", nativeQuery = true)
    List<Object[]> getMonthlyTotalsByUser(@Param("userId") Long userId, @Param("type") String type, @Param("year") int year);

    boolean existsByUserAndRecurringIdAndDate(AppUser user, Long recurringId, LocalDate date);

    long countByUserAndCategory(AppUser user, com.yuuki.householdbook.entity.Category category);

    @Query("SELECT a FROM Account a WHERE a.user = :user " +
            "AND (:type IS NULL OR a.type = :type) " +
            "AND (a.date >= COALESCE(:startDate, a.date)) " +
            "AND (a.date <= COALESCE(:endDate, a.date)) " +
            "AND (:categoryId IS NULL OR a.category.id = :categoryId) " +
            "AND (:minAmount IS NULL OR a.amount >= :minAmount) " +
            "AND (:maxAmount IS NULL OR a.amount <= :maxAmount) " +
            "AND (:memo IS NULL OR :memo = '' OR LOWER(a.memo) LIKE LOWER(CONCAT('%', :memo, '%'))) " +
            "ORDER BY a.date DESC, a.id DESC")
    List<Account> search(@Param("user") AppUser user,
                         @Param("type") String type,
                         @Param("startDate") LocalDate startDate,
                         @Param("endDate") LocalDate endDate,
                         @Param("categoryId") Long categoryId,
                         @Param("minAmount") Integer minAmount,
                         @Param("maxAmount") Integer maxAmount,
                         @Param("memo") String memo);
}
