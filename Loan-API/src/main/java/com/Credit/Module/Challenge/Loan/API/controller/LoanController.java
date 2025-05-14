package com.Credit.Module.Challenge.Loan.API.controller;

import com.Credit.Module.Challenge.Loan.API.entity.Loan;
import com.Credit.Module.Challenge.Loan.API.entity.LoanInstallment;
import com.Credit.Module.Challenge.Loan.API.service.LoanService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.Credit.Module.Challenge.Loan.API.dto.PayLoanResult;
import java.util.List;


@RestController
@RequestMapping("/loans")
public class LoanController {

    private final LoanService loanService;

    public LoanController(LoanService loanService) {
        this.loanService = loanService;
    }

    @PostMapping("/create")
    public ResponseEntity<Loan> createLoan(@RequestParam Long customerId,
                                           @RequestParam Double amount,
                                           @RequestParam Double interestRate,
                                           @RequestParam Integer numberOfInstallments) {
        Loan loan = loanService.createLoan(customerId, amount, interestRate, numberOfInstallments);
        return ResponseEntity.ok(loan);
    }

    @GetMapping("/list")
    public ResponseEntity<List<Loan>> listLoans(@RequestParam Long customerId) {
            List<Loan> loans = loanService.getLoansByCustomer(customerId);
        return ResponseEntity.ok(loans);
    }

    @GetMapping("/installments")
    public ResponseEntity<List<LoanInstallment>> listInstallments(@RequestParam Long loanId) {
            List<LoanInstallment> installments = loanService.getInstallmentsByLoan(loanId);
        return ResponseEntity.ok(installments);
    }

    @PostMapping("/pay")
    public ResponseEntity<PayLoanResult> payLoan(@RequestParam Long loanId,
                                             @RequestParam Double amount) {
        PayLoanResult result = loanService.payLoan(loanId, amount);
    return ResponseEntity.ok(result);
    }

}
