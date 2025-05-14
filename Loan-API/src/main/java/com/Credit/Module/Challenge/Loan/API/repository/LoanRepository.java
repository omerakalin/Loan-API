package com.Credit.Module.Challenge.Loan.API.repository;

import com.Credit.Module.Challenge.Loan.API.entity.Customer;
import com.Credit.Module.Challenge.Loan.API.entity.Loan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LoanRepository extends JpaRepository<Loan, Long>{
      List<Loan> findByCustomer(Customer customer);
}
