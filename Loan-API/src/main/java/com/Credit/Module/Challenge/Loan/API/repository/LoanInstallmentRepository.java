package com.Credit.Module.Challenge.Loan.API.repository;

import com.Credit.Module.Challenge.Loan.API.entity.Loan;
import com.Credit.Module.Challenge.Loan.API.entity.LoanInstallment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LoanInstallmentRepository extends JpaRepository<LoanInstallment, Long>{
   List<LoanInstallment> findByLoan(Loan loan); 
}
