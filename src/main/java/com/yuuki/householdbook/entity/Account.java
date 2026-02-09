package com.yuuki.householdbook.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "account")
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate date;

    private String type; // "income" or "expense"

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    private String item;

    private Integer amount;

    private String memo;

    private LocalDate createdAt = LocalDate.now();

    @Column(name = "recurring_id")
    private Long recurringId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_id")
    private PaymentSource source;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private AppUser user;

    // --- Getter & Setter ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }

    public String getItem() { return item; }
    public void setItem(String item) { this.item = item; }

    public Integer getAmount() { return amount; }
    public void setAmount(Integer amount) { this.amount = amount; }

    public String getMemo() { return memo; }
    public void setMemo(String memo) { this.memo = memo; }

    public LocalDate getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDate createdAt) { this.createdAt = createdAt; }

    public Long getRecurringId() { return recurringId; }
    public void setRecurringId(Long recurringId) { this.recurringId = recurringId; }

    public PaymentSource getSource() { return source; }
    public void setSource(PaymentSource source) { this.source = source; }

    public AppUser getUser() { return user; }
    public void setUser(AppUser user) { this.user = user; }
}
