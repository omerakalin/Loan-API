package com.Credit.Module.Challenge.Loan.API.service;

import com.Credit.Module.Challenge.Loan.API.entity.Customer;
import com.Credit.Module.Challenge.Loan.API.entity.Loan;
import com.Credit.Module.Challenge.Loan.API.entity.LoanInstallment;
import com.Credit.Module.Challenge.Loan.API.repository.CustomerRepository;
import com.Credit.Module.Challenge.Loan.API.repository.LoanInstallmentRepository;
import com.Credit.Module.Challenge.Loan.API.repository.LoanRepository;
import com.Credit.Module.Challenge.Loan.API.dto.PayLoanResult;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class LoanService {

    private final CustomerRepository customerRepository;
    private final LoanRepository loanRepository;
    private final LoanInstallmentRepository loanInstallmentRepository;

    public LoanService(CustomerRepository customerRepository, LoanRepository loanRepository, LoanInstallmentRepository loanInstallmentRepository) {
        this.customerRepository = customerRepository;
        this.loanRepository = loanRepository;
        this.loanInstallmentRepository = loanInstallmentRepository;
    }

    public Loan createLoan(Long customerId, Double amount, Double interestRate, Integer numberOfInstallments) {
        // 1. Müşteri kontrolü
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        // 2. Limit kontrolü
        double totalLoanAmount = amount * (1 + interestRate);
        if (customer.getCreditLimit() - customer.getUsedCreditLimit() < totalLoanAmount) {
            throw new RuntimeException("Customer does not have enough credit limit");
        }

        // 3. Installment sayısı kontrolü
        if (!(numberOfInstallments == 6 || numberOfInstallments == 9 || numberOfInstallments == 12 || numberOfInstallments == 24)) {
            throw new RuntimeException("Invalid number of installments");
        }

        // 4. Interest Rate kontrolü
        if (interestRate < 0.1 || interestRate > 0.5) {
            throw new RuntimeException("Invalid interest rate");
        }

        // 5. Loan kaydı oluştur
        Loan loan = new Loan();
        loan.setCustomer(customer);
        loan.setLoanAmount(totalLoanAmount);
        loan.setNumberOfInstallment(numberOfInstallments);
        loan.setCreateDate(LocalDateTime.now());
        loan.setIsPaid(false);
        Loan savedLoan = loanRepository.save(loan);

        // 6. Installments oluştur
        List<LoanInstallment> installments = new ArrayList<>();
        double installmentAmount = totalLoanAmount / numberOfInstallments;
        LocalDate firstDueDate = LocalDate.now().plusMonths(1).withDayOfMonth(1);

        for (int i = 0; i < numberOfInstallments; i++) {
            LoanInstallment installment = new LoanInstallment();
            installment.setLoan(savedLoan);
            installment.setAmount(installmentAmount);
            installment.setPaidAmount(0.0);
            installment.setDueDate(firstDueDate.plusMonths(i));
            installment.setIsPaid(false);
            installments.add(installment);
        }

        loanInstallmentRepository.saveAll(installments);

        // 7. Customer usedCreditLimit güncelle
        customer.setUsedCreditLimit(customer.getUsedCreditLimit() + totalLoanAmount);
        customerRepository.save(customer);

        return savedLoan;
    }

    public List<Loan> getLoansByCustomer(Long customerId) {
        Customer customer = customerRepository.findById(customerId)
            .orElseThrow(() -> new RuntimeException("Customer not found"));

        return loanRepository.findByCustomer(customer);
    }

    public List<LoanInstallment> getInstallmentsByLoan(Long loanId) {
        Loan loan = loanRepository.findById(loanId)
            .orElseThrow(() -> new RuntimeException("Loan not found"));

        return loanInstallmentRepository.findByLoan(loan);
    }

    public PayLoanResult payLoan(Long loanId, Double paymentAmount) {
        Loan loan = loanRepository.findById(loanId)
            .orElseThrow(() -> new RuntimeException("Loan not found"));

    // Ödenmemiş, due date ilk 3 ay içinde olan taksitleri çekiyoruz
    List<LoanInstallment> installments = loanInstallmentRepository.findByLoan(loan).stream()
            .filter(i -> !i.getIsPaid())
            .filter(i -> i.getDueDate().isBefore(LocalDate.now().plusMonths(3).withDayOfMonth(1)))
            .sorted((i1, i2) -> i1.getDueDate().compareTo(i2.getDueDate()))
            .toList();

    int paidInstallments = 0;
    double totalSpent = 0.0;

    for (LoanInstallment installment : installments) {
        if (paymentAmount >= installment.getAmount()) {
            double baseAmount = installment.getAmount();
            LocalDate paymentDate = LocalDate.now();
            long daysDiff = java.time.temporal.ChronoUnit.DAYS.between(paymentDate, installment.getDueDate());

            double adjustedPaidAmount = baseAmount;

            if (daysDiff > 0) { // Erken ödeme indirimi
                double discount = baseAmount * 0.001 * daysDiff;
                adjustedPaidAmount -= discount;
            } else if (daysDiff < 0) { // Geç ödeme cezası
                double penalty = baseAmount * 0.001 * Math.abs(daysDiff);
                adjustedPaidAmount += penalty;
            }

            installment.setPaidAmount(adjustedPaidAmount);
            installment.setPaymentDate(paymentDate);
            installment.setIsPaid(true);

            paymentAmount -= baseAmount;  // sabit taksit bazlı azaltılır
            totalSpent += adjustedPaidAmount;  // indirimli/cezalı gerçek ödeme artar
            paidInstallments++;
        } else {
            break;
        }
    }

    if (paidInstallments > 0) {
        loanInstallmentRepository.saveAll(installments);
    }

    // Eğer tüm taksitler ödendiyse loan isPaid = true yap
    boolean loanPaid = loanInstallmentRepository.findByLoan(loan).stream().allMatch(LoanInstallment::getIsPaid);
    if (loanPaid) {
        loan.setIsPaid(true);
        loanRepository.save(loan);
    }

    // Customer usedCreditLimit azalt (toplam harcanan gerçek tutar kadar)
    Customer customer = loan.getCustomer();
    customer.setUsedCreditLimit(customer.getUsedCreditLimit() - totalSpent);
    customerRepository.save(customer);

    // Sonuç DTO döndür
    return new PayLoanResult(paidInstallments, totalSpent, loanPaid);
    }

}
