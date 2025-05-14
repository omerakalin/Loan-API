package com.Credit.Module.Challenge.Loan.API.repository;

import com.Credit.Module.Challenge.Loan.API.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long>  {
     // JpaRepository zaten findById ve save metodlarını sağlar.
}
