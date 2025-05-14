package com.Credit.Module.Challenge.Loan.API.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String surname;

    private Double creditLimit;

    private Double usedCreditLimit;

    // Varsayılan yapıcı
    public Customer() {
    }

    // Parametreli yapıcı
    public Customer(String name, String surname, Double creditLimit, Double usedCreditLimit) {
        this.name = name;
        this.surname = surname;
        this.creditLimit = creditLimit;
        this.usedCreditLimit = usedCreditLimit;
    }
}
