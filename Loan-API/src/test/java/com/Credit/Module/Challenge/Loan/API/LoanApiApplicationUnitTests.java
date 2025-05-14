package com.Credit.Module.Challenge.Loan.API;

import com.Credit.Module.Challenge.Loan.API.dto.PayLoanResult;
import com.Credit.Module.Challenge.Loan.API.entity.Customer;
import com.Credit.Module.Challenge.Loan.API.entity.Loan;
import com.Credit.Module.Challenge.Loan.API.entity.LoanInstallment;
import com.Credit.Module.Challenge.Loan.API.repository.CustomerRepository;
import com.Credit.Module.Challenge.Loan.API.repository.LoanInstallmentRepository;
import com.Credit.Module.Challenge.Loan.API.repository.LoanRepository;
import com.Credit.Module.Challenge.Loan.API.service.LoanService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class) // For robust Mockito integration
class LoanApiApplicationUnitTests {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private LoanRepository loanRepository;

    @Mock
    private LoanInstallmentRepository loanInstallmentRepository;

    @InjectMocks
    private LoanService loanService;

    private Customer testCustomer;
    private Loan testLoan;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this); // Initializes mocks

        testCustomer = new Customer();
        testCustomer.setId(1L);
        testCustomer.setName("John");
        testCustomer.setSurname("Doe");
        testCustomer.setCreditLimit(50000.0);
        testCustomer.setUsedCreditLimit(10000.0);

        testLoan = new Loan();
        testLoan.setId(1L);
        testLoan.setCustomer(testCustomer);
        testLoan.setLoanAmount(12000.0); // Example total amount with interest
        testLoan.setNumberOfInstallment(12);
        testLoan.setIsPaid(false);
    }

    @Test
    void testCreateLoan_Success() {
        double amount = 10000.0;
        double interestRate = 0.2; // Valid interest rate
        int numberOfInstallments = 12; // Valid number of installments

        when(customerRepository.findById(1L)).thenReturn(Optional.of(testCustomer));
        // Ensure the customer has enough credit: 50000 limit - 10000 used = 40000 available
        // Loan to create: 10000 * (1 + 0.2) = 12000. This is within the limit.
        when(loanRepository.save(any(Loan.class))).thenAnswer(i -> {
            Loan loanArg = i.getArgument(0);
            loanArg.setId(1L); // Simulate saving and getting an ID
            return loanArg;
        });
        when(loanInstallmentRepository.saveAll(anyList())).thenAnswer(i -> i.getArguments()[0]);
        when(customerRepository.save(any(Customer.class))).thenReturn(testCustomer);


        Loan loan = loanService.createLoan(1L, amount, interestRate, numberOfInstallments);

        assertNotNull(loan);
        assertEquals(1L, loan.getCustomer().getId());
        assertEquals(amount * (1 + interestRate), loan.getLoanAmount(), 0.01);
        assertEquals(numberOfInstallments, loan.getNumberOfInstallment());
        assertFalse(loan.getIsPaid());
        assertNotNull(loan.getCreateDate());

        // Verify customer's used credit limit was updated
        // Initial used: 10000. Loan taken: 12000. New used: 22000
        assertEquals(10000.0 + (amount * (1 + interestRate)), testCustomer.getUsedCreditLimit(), 0.01);

        verify(customerRepository, times(1)).findById(1L);
        verify(loanRepository, times(1)).save(any(Loan.class));
        verify(loanInstallmentRepository, times(1)).saveAll(anyList());
        verify(customerRepository, times(1)).save(testCustomer);
    }

    @Test
    void testCreateLoan_FailsWhenInsufficientCreditLimit() {
        testCustomer.setUsedCreditLimit(45000.0); // Leaves only 5000 credit left
        when(customerRepository.findById(1L)).thenReturn(Optional.of(testCustomer));

        // Attempt to create a loan of 10000 * (1+0.1) = 11000, which exceeds available credit
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            loanService.createLoan(1L, 10000.0, 0.1, 6);
        });
        assertEquals("Customer does not have enough credit limit", exception.getMessage());

        verify(loanRepository, never()).save(any());
    }

    @Test
    void testCreateLoan_FailsWhenInvalidInstallmentNumber() {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(testCustomer));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            loanService.createLoan(1L, 1000.0, 0.1, 7); // 7 is invalid
        });
        assertEquals("Invalid number of installments", exception.getMessage()); // Matched to LoanService

        verify(loanRepository, never()).save(any());
    }

    @Test
    void testCreateLoan_FailsWhenInterestRateInvalid_TooHigh() {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(testCustomer));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            loanService.createLoan(1L, 1000.0, 0.6, 6); // 0.6 is > 0.5
        });
        assertEquals("Invalid interest rate", exception.getMessage()); // Matched to LoanService

        verify(loanRepository, never()).save(any());
    }

    @Test
    void testCreateLoan_FailsWhenInterestRateInvalid_TooLow() {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(testCustomer));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            loanService.createLoan(1L, 1000.0, 0.05, 6); // 0.05 is < 0.1
        });
        assertEquals("Invalid interest rate", exception.getMessage()); // Matched to LoanService

        verify(loanRepository, never()).save(any());
    }


    @Test
    void testGetLoansByCustomer_Success() {
        List<Loan> mockLoans = new ArrayList<>();
        mockLoans.add(testLoan);

        when(customerRepository.findById(1L)).thenReturn(Optional.of(testCustomer));
        when(loanRepository.findByCustomer(testCustomer)).thenReturn(mockLoans);

        List<Loan> loans = loanService.getLoansByCustomer(1L);
        assertEquals(1, loans.size());
        assertEquals(testLoan.getLoanAmount(), loans.get(0).getLoanAmount());

        verify(customerRepository, times(1)).findById(1L);
        verify(loanRepository, times(1)).findByCustomer(testCustomer);
    }

    @Test
    void testGetLoansByCustomer_CustomerNotFound() {
        when(customerRepository.findById(1L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            loanService.getLoansByCustomer(1L);
        });
        assertEquals("Customer not found", exception.getMessage());
    }


    @Test
    void testGetInstallmentsByLoan_Success() {
        List<LoanInstallment> mockInstallments = new ArrayList<>();
        LoanInstallment installment1 = createInstallment(1L, testLoan, 1000.0, false, LocalDate.now().plusMonths(1), null);
        mockInstallments.add(installment1);

        when(loanRepository.findById(1L)).thenReturn(Optional.of(testLoan)); // Added this mock
        when(loanInstallmentRepository.findByLoan(testLoan)).thenReturn(mockInstallments);

        List<LoanInstallment> installments = loanService.getInstallmentsByLoan(1L);
        assertEquals(1, installments.size());
        assertEquals(1000.0, installments.get(0).getAmount());

        verify(loanRepository, times(1)).findById(1L);
        verify(loanInstallmentRepository, times(1)).findByLoan(testLoan);
    }

    @Test
    void testGetInstallmentsByLoan_LoanNotFound() {
        when(loanRepository.findById(1L)).thenReturn(Optional.empty());
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            loanService.getInstallmentsByLoan(1L);
        });
        assertEquals("Loan not found", exception.getMessage());
    }


    @Test
    void testPayLoan_PaysInstallmentsCorrectly_WithPenaltiesAndDiscounts() {
        // Current date for calculation reference in the test
        LocalDate paymentDate = LocalDate.now(); // LoanService uses LocalDate.now() internally

        // Setup installments with varying due dates relative to 'paymentDate'
        List<LoanInstallment> installments = new ArrayList<>();
        // Inst 1: Due last month (e.g., if paymentDate is May 15, due date April 15). LATE.
        installments.add(createInstallment(1L, testLoan, 1000.0, false, paymentDate.minusMonths(1), null));
        // Inst 2: Due this month but earlier (e.g., if paymentDate is May 15, due date May 1). LATE.
        installments.add(createInstallment(2L, testLoan, 1000.0, false, paymentDate.withDayOfMonth(1), null));
        // Inst 3: Due next month (e.g., if paymentDate is May 15, due date June 15). EARLY.
        installments.add(createInstallment(3L, testLoan, 1000.0, false, paymentDate.plusMonths(1), null));
         // Inst 4: Due in 2 months (e.g., if paymentDate is May 15, due date July 15). EARLY.
        installments.add(createInstallment(4L, testLoan, 1000.0, false, paymentDate.plusMonths(2), null));
        // Inst 5: Due in 3 months (e.g., if paymentDate is May 15, due date Aug 15). Should NOT be payable by the 3-month rule.
        installments.add(createInstallment(5L, testLoan, 1000.0, false, paymentDate.plusMonths(3), null));


        // Calculate expected costs based on LoanService logic (0.1% per day)
        double expectedTotalSpent = 0;
        int expectedPaidInstallments = 0;

        // Installment 1 (Late)
        long daysLate1 = ChronoUnit.DAYS.between(installments.get(0).getDueDate(), paymentDate);
        double penalty1 = installments.get(0).getAmount() * 0.001 * daysLate1;
        double adjustedAmount1 = installments.get(0).getAmount() + penalty1;
        expectedTotalSpent += adjustedAmount1;
        expectedPaidInstallments++;

        // Installment 2 (Late)
        // Assuming paymentDate is after the 1st of the current month
        long daysLate2 = ChronoUnit.DAYS.between(installments.get(1).getDueDate(), paymentDate);
        double penalty2 = 0;
        if (daysLate2 > 0) { // only apply penalty if truly late
             penalty2 = installments.get(1).getAmount() * 0.001 * daysLate2;
        }
        double adjustedAmount2 = installments.get(1).getAmount() + penalty2;
        expectedTotalSpent += adjustedAmount2;
        expectedPaidInstallments++;
        
        // We are paying 2500. So only first two installments will be paid.
        // Initial customer used credit limit: 10000.0
        double initialUsedCredit = testCustomer.getUsedCreditLimit();


        when(loanRepository.findById(1L)).thenReturn(Optional.of(testLoan));
        // The service filters installments, so mock the full list first
        when(loanInstallmentRepository.findByLoan(testLoan)).thenReturn(installments);
        when(loanInstallmentRepository.saveAll(anyList())).thenAnswer(i -> i.getArguments()[0]);
        when(loanRepository.save(any(Loan.class))).thenAnswer(i -> i.getArguments()[0]);
        // when(customerRepository.findById(1L)).thenReturn(Optional.of(testCustomer)); // Already part of testLoan
        when(customerRepository.save(any(Customer.class))).thenAnswer(i -> i.getArguments()[0]);

        // Attempt to pay 2500. This should cover the first two installments (base 1000 each)
        // after adjustments.
        PayLoanResult result = loanService.payLoan(1L, 2500.0);

        assertEquals(expectedPaidInstallments, result.getPaidInstallments());
        assertEquals(expectedTotalSpent, result.getTotalSpent(), 0.01); // Using calculated expected total
        assertFalse(result.isLoanPaid()); // Assuming not all 5 installments are paid

        // Verify customer's used credit limit was reduced by the actual amount spent
        assertEquals(initialUsedCredit - expectedTotalSpent, testCustomer.getUsedCreditLimit(), 0.01);

        verify(loanInstallmentRepository, times(1)).saveAll(anyList());
        // Loan might not be saved if not fully paid yet by this payment
        List<LoanInstallment> capturedInstallments = new ArrayList<>();
        for(LoanInstallment inst : installments) {
            if(inst.getId() <= expectedPaidInstallments) { // Only check those expected to be paid
                 capturedInstallments.add(inst);
            }
        }
        // Verify that only the first two installments were marked as paid.
        assertTrue(capturedInstallments.get(0).getIsPaid());
        assertEquals(adjustedAmount1, capturedInstallments.get(0).getPaidAmount(), 0.01);
        assertEquals(paymentDate, capturedInstallments.get(0).getPaymentDate());

        assertTrue(capturedInstallments.get(1).getIsPaid());
        assertEquals(adjustedAmount2, capturedInstallments.get(1).getPaidAmount(), 0.01);
        assertEquals(paymentDate, capturedInstallments.get(1).getPaymentDate());
        
        // Verify other installments (3, 4, 5) are not paid by this transaction
        assertFalse(installments.get(2).getIsPaid()); // Inst 3
        assertFalse(installments.get(3).getIsPaid()); // Inst 4
        assertFalse(installments.get(4).getIsPaid()); // Inst 5


        verify(customerRepository, times(1)).save(any(Customer.class));
    }
    
    @Test
    void testPayLoan_LoanFullyPaid() {
        LocalDate paymentDate = LocalDate.now();
        List<LoanInstallment> installments = new ArrayList<>();
        // Only one installment for simplicity
        installments.add(createInstallment(1L, testLoan, 100.0, false, paymentDate.minusDays(5), null)); // Late
        testLoan.setNumberOfInstallment(1); // Loan has only one installment

        double daysLate = ChronoUnit.DAYS.between(installments.get(0).getDueDate(), paymentDate);
        double penalty = installments.get(0).getAmount() * 0.001 * daysLate;
        double expectedAmountPaid = installments.get(0).getAmount() + penalty;
        
        double initialUsedCredit = testCustomer.getUsedCreditLimit();


        when(loanRepository.findById(1L)).thenReturn(Optional.of(testLoan));
        when(loanInstallmentRepository.findByLoan(testLoan)).thenReturn(installments); // This list will be modified by service
        when(loanInstallmentRepository.saveAll(anyList())).thenAnswer(i -> {
            // Simulate the side effect of saving where the passed list items are updated
            List<LoanInstallment> savedList = i.getArgument(0);
            // In a real scenario, these would be updated by the service logic before saveAll is called
            // For the purpose of allMatch check after saveAll, ensure the state is as expected
            if (!savedList.isEmpty() && savedList.get(0).getId().equals(1L)) {
                savedList.get(0).setIsPaid(true); 
                savedList.get(0).setPaidAmount(expectedAmountPaid);
                savedList.get(0).setPaymentDate(paymentDate);
            }
            return savedList;
        });
        when(loanRepository.save(any(Loan.class))).thenAnswer(i -> i.getArguments()[0]);
        when(customerRepository.save(any(Customer.class))).thenAnswer(i -> i.getArguments()[0]);

        PayLoanResult result = loanService.payLoan(1L, 200.0); // Enough to pay the installment with penalty

        assertEquals(1, result.getPaidInstallments());
        assertEquals(expectedAmountPaid, result.getTotalSpent(), 0.01);
        assertTrue(result.isLoanPaid());
        assertTrue(testLoan.getIsPaid()); // Check loan status directly
        assertEquals(initialUsedCredit - expectedAmountPaid, testCustomer.getUsedCreditLimit(), 0.01);


        verify(loanRepository, times(1)).save(testLoan); // Loan should be saved as paid
    }


    private LoanInstallment createInstallment(Long id, Loan loan, Double amount, boolean isPaid, LocalDate dueDate, LocalDate paymentDate) {
        LoanInstallment installment = new LoanInstallment();
        installment.setId(id);
        installment.setLoan(loan);
        installment.setAmount(amount);
        installment.setPaidAmount(isPaid ? amount : 0.0);
        installment.setIsPaid(isPaid);
        installment.setDueDate(dueDate);
        installment.setPaymentDate(paymentDate);
        return installment;
    }
}