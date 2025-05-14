package com.Credit.Module.Challenge.Loan.API.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data

public class Loan {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    private Double loanAmount;

    private Integer numberOfInstallment;

    private LocalDateTime createDate;

    private Boolean isPaid;

}
