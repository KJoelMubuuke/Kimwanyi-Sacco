package org.pahappa.kimwanyi.dao;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.pahappa.kimwanyi.model.LoanApplication;
import org.pahappa.kimwanyi.util.HibernateUtil;

import java.util.List;

public class LoanApplicationDAO {

    public void save(LoanApplication application) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.persist(application);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw e;
        }
    }

    public LoanApplication findById(Long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(LoanApplication.class, id);
        }
    }

    public List<LoanApplication> findByMemberId(Long memberId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM LoanApplication WHERE member.id = :memberId ORDER BY appliedAt DESC", LoanApplication.class)
                    .setParameter("memberId", memberId)
                    .list();
        }
    }

    public List<LoanApplication> findPending() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM LoanApplication WHERE status = 'PENDING' ORDER BY appliedAt ASC", LoanApplication.class)
                    .list();
        }
    }

    public boolean hasPendingOrActive(Long memberId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Long count = session.createQuery("""
                            SELECT COUNT(la)
                            FROM LoanApplication la
                            LEFT JOIN la.loan loan
                            WHERE la.member.id = :memberId
                              AND (la.status = 'PENDING' OR loan.status = 'ACTIVE')
                            """, Long.class)
                    .setParameter("memberId", memberId)
                    .uniqueResult();
            return count > 0;
        }
    }

    public void update(LoanApplication application) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.merge(application);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw e;
        }
    }
}
