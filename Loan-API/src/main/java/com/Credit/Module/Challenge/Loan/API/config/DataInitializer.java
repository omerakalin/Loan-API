package com.Credit.Module.Challenge.Loan.API.config;

import com.Credit.Module.Challenge.Loan.API.entity.Customer;
import com.Credit.Module.Challenge.Loan.API.entity.Loan;
import com.Credit.Module.Challenge.Loan.API.entity.LoanInstallment;
import com.Credit.Module.Challenge.Loan.API.repository.CustomerRepository;
import com.Credit.Module.Challenge.Loan.API.repository.LoanRepository;
import com.Credit.Module.Challenge.Loan.API.repository.LoanInstallmentRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner loadData(CustomerRepository customerRepository,
                                      LoanRepository loanRepository,
                                      LoanInstallmentRepository loanInstallmentRepository) {
        return args -> {
            if (customerRepository.count() == 0) {

                // Kullanıcıları oluştur
                Customer ali = new Customer("Ali", "Veli", 5000.0, 0.0);
                Customer ayse = new Customer("Ayşe", "Fatma", 10000.0, 0.0);
                Customer john = new Customer("John", "Doe", 8000.0, 0.0);
                Customer maria = new Customer("Maria", "Gomez", 15000.0, 0.0);

                customerRepository.saveAll(List.of(ali, ayse, john, maria));

                // Loans ve Installments oluştur (davranış senaryolarıyla)
                createLoanWithBehavior(ali, loanRepository, loanInstallmentRepository, customerRepository, 1200.0, 12, 6, "late");
                createLoanWithBehavior(ayse, loanRepository, loanInstallmentRepository, customerRepository, 2400.0, 12, 12, "early");
                createLoanWithBehavior(john, loanRepository, loanInstallmentRepository, customerRepository, 1800.0, 9, 9, "late");
                createLoanWithBehavior(maria, loanRepository, loanInstallmentRepository, customerRepository, 3000.0, 24, 12, "mixed");

                System.out.println("Dummy data loaded with all test cases.");
            }
        };
    }

    private void createLoanWithBehavior(Customer customer,
                                        LoanRepository loanRepository,
                                        LoanInstallmentRepository loanInstallmentRepository,
                                        CustomerRepository customerRepository,
                                        double loanAmount,
                                        int installmentCount,
                                        int paidInstallments,
                                        String behavior) {

        // Loan kaydı
        Loan loan = new Loan();
        loan.setCustomer(customer);
        loan.setLoanAmount(loanAmount);
        loan.setNumberOfInstallment(installmentCount);
        loan.setCreateDate(LocalDateTime.now());
        loan.setIsPaid(false);
        loanRepository.save(loan);

        // Installment'lar
        double installmentAmount = loanAmount / installmentCount;
        LocalDate firstDueDate = LocalDate.now().minusMonths(installmentCount - 1).withDayOfMonth(1);

        List<LoanInstallment> installments = new ArrayList<>();

        for (int i = 0; i < installmentCount; i++) {
            LoanInstallment installment = new LoanInstallment();
            installment.setLoan(loan);
            installment.setAmount(installmentAmount);
            installment.setDueDate(firstDueDate.plusMonths(i));

            if (i < paidInstallments) {
                installment.setIsPaid(true);

                if (behavior.equals("early")) {
                    installment.setPaymentDate(installment.getDueDate().minusDays(10)); // Erken ödeme
                } else if (behavior.equals("late")) {
                    installment.setPaymentDate(installment.getDueDate().plusDays(15));  // Geç ödeme
                } else if (behavior.equals("mixed")) {
                    installment.setPaymentDate(i % 2 == 0
                            ? installment.getDueDate().minusDays(5)   // çift index → erken
                            : installment.getDueDate().plusDays(10)); // tek index → geç
                }

                installment.setPaidAmount(installmentAmount);
            } else {
                installment.setIsPaid(false);
                installment.setPaidAmount(0.0);
                installment.setPaymentDate(null);
            }

            installments.add(installment);
        }

        loanInstallmentRepository.saveAll(installments);

        // Customer borcunu güncelle
        customer.setUsedCreditLimit(customer.getUsedCreditLimit() + loanAmount);
        customerRepository.save(customer);
    }
}
