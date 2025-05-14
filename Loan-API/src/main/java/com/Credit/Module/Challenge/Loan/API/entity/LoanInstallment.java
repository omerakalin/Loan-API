package com.Credit.Module.Challenge.Loan.API.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Entity
@Data

public class LoanInstallment {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "loan_id", nullable = false)
    private Loan loan;

    private Double amount;

    private Double paidAmount;

    private LocalDate dueDate;

    private LocalDate paymentDate;

    private Boolean isPaid;

}
