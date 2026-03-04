package com.yuuki.householdbook.entity;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "payment_source")
public class PaymentSource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String type; // "cash", "bank", "card", "ewallet", "other"

    @Column(nullable = false)
    private Integer initialBalance = 0;

    private LocalDate createdAt = LocalDate.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private AppUser user;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public Integer getInitialBalance() { return initialBalance; }
    public void setInitialBalance(Integer initialBalance) { this.initialBalance = initialBalance; }

    public LocalDate getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDate createdAt) { this.createdAt = createdAt; }

    public AppUser getUser() { return user; }
    public void setUser(AppUser user) { this.user = user; }
}
