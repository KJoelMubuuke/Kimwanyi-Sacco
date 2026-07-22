package org.pahappa.kimwanyi.service;

import org.hibernate.Session;
import org.pahappa.kimwanyi.util.HibernateUtil;

import java.math.BigDecimal;

/**
 * Admin dashboard - the brief's "Admin dashboard showing summary counts
 * and totals." Every number here is a single read-only aggregate query
 * (COUNT/SUM), not a Java loop over loaded entities - so this stays fast
 * even as the number of members/loans grows, since the database does
 * the counting, not this code.
 *
 * This solves one of the brief's actual complaints directly: "It takes us
 * almost a week after month-end to know how much money the SACCO actually
 * holds" - this method answers that in one query, instantly.
 */
public class DashboardService {

    public DashboardSummary getSummary() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {

            Long totalActiveMembers = session.createQuery(
                            "SELECT COUNT(m) FROM Member m WHERE m.status = 'ACTIVE'", Long.class)
                    .uniqueResult();

            BigDecimal totalSavingsHeld = session.createQuery(
                            "SELECT COALESCE(SUM(a.balance), 0) FROM SavingsAccount a", BigDecimal.class)
                    .uniqueResult();

            Long activeLoansCount = session.createQuery(
                            "SELECT COUNT(l) FROM Loan l WHERE l.status = 'ACTIVE'", Long.class)
                    .uniqueResult();

            BigDecimal totalOutstandingLoanBalance = session.createQuery(
                            "SELECT COALESCE(SUM(l.remainingBalance), 0) FROM Loan l WHERE l.status = 'ACTIVE'", BigDecimal.class)
                    .uniqueResult();

            Long pendingApplicationsCount = session.createQuery(
                            "SELECT COUNT(la) FROM LoanApplication la WHERE la.status = 'PENDING'", Long.class)
                    .uniqueResult();

            return new DashboardSummary(
                    totalActiveMembers != null ? totalActiveMembers : 0,
                    totalSavingsHeld != null ? totalSavingsHeld : BigDecimal.ZERO,
                    activeLoansCount != null ? activeLoansCount : 0,
                    totalOutstandingLoanBalance != null ? totalOutstandingLoanBalance : BigDecimal.ZERO,
                    pendingApplicationsCount != null ? pendingApplicationsCount : 0
            );
        }
    }
}