package com.Credit.Module.Challenge.Loan.API.dto;

public class PayLoanResult {
    private int paidInstallments;
    private double totalSpent;
    private boolean loanPaid;

    public PayLoanResult(int paidInstallments, double totalSpent, boolean loanPaid) {
        this.paidInstallments = paidInstallments;
        this.totalSpent = totalSpent;
        this.loanPaid = loanPaid;
    }

    // Getters & Setters
    public int getPaidInstallments() { return paidInstallments; }
    public double getTotalSpent() { return totalSpent; }
    public boolean isLoanPaid() { return loanPaid; }
}
