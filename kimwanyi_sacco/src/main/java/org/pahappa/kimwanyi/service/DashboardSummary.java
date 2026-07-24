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
    private final long todayTransactionsCount;
    private final long totalMembers;
    private final long maleMembersCount;
    private final long femaleMembersCount;

    public DashboardSummary(long totalActiveMembers, BigDecimal totalSavingsHeld,
                            long activeLoansCount, BigDecimal totalOutstandingLoanBalance,
                            long pendingApplicationsCount, long todayTransactionsCount,
                            long totalMembers, long maleMembersCount, long femaleMembersCount) {
        this.totalActiveMembers = totalActiveMembers;
        this.totalSavingsHeld = totalSavingsHeld;
        this.activeLoansCount = activeLoansCount;
        this.totalOutstandingLoanBalance = totalOutstandingLoanBalance;
        this.pendingApplicationsCount = pendingApplicationsCount;
        this.todayTransactionsCount = todayTransactionsCount;
        this.totalMembers = totalMembers;
        this.maleMembersCount = maleMembersCount;
        this.femaleMembersCount = femaleMembersCount;
    }

    public long getTotalActiveMembers() { return totalActiveMembers; }
    public BigDecimal getTotalSavingsHeld() { return totalSavingsHeld; }
    public long getActiveLoansCount() { return activeLoansCount; }
    public BigDecimal getTotalOutstandingLoanBalance() { return totalOutstandingLoanBalance; }
    public long getPendingApplicationsCount() { return pendingApplicationsCount; }
    public long getTodayTransactionsCount() { return todayTransactionsCount; }
    public long getTotalMembers() { return totalMembers; }
    public long getMaleMembersCount() { return maleMembersCount; }
    public long getFemaleMembersCount() { return femaleMembersCount; }
}