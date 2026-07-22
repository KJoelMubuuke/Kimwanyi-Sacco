package org.pahappa.kimwanyi.service;

import java.math.BigDecimal;

/**
 * Plain data carrier for the admin dashboard - the brief's
 * "Admin dashboard showing summary counts and totals."
 */
public class DashboardSummary {

    private final long totalActiveMembers;
    private final BigDecimal totalSavingsHeld;
    private final long activeLoansCount;
    private final BigDecimal totalOutstandingLoanBalance;
    private final long pendingApplicationsCount;

    public DashboardSummary(long totalActiveMembers, BigDecimal totalSavingsHeld,
                            long activeLoansCount, BigDecimal totalOutstandingLoanBalance,
                            long pendingApplicationsCount) {
        this.totalActiveMembers = totalActiveMembers;
        this.totalSavingsHeld = totalSavingsHeld;
        this.activeLoansCount = activeLoansCount;
        this.totalOutstandingLoanBalance = totalOutstandingLoanBalance;
        this.pendingApplicationsCount = pendingApplicationsCount;
    }

    public long getTotalActiveMembers() { return totalActiveMembers; }
    public BigDecimal getTotalSavingsHeld() { return totalSavingsHeld; }
    public long getActiveLoansCount() { return activeLoansCount; }
    public BigDecimal getTotalOutstandingLoanBalance() { return totalOutstandingLoanBalance; }
    public long getPendingApplicationsCount() { return pendingApplicationsCount; }
}