package org.pahappa.kimwanyi.dao;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.pahappa.kimwanyi.model.Loan;
import org.pahappa.kimwanyi.util.HibernateUtil;

import java.util.List;

public class LoanDAO {

    public void save(Loan loan) {
        Transaction tx = null;
        Session session = HibernateUtil.getSessionFactory().openSession();
        try {
            tx = session.beginTransaction();
            session.persist(loan);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw e;
        } finally {
            session.close();
        }
    }

    public Loan findById(Long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(Loan.class, id);
        }
    }

    public Loan findActiveLoanByMemberId(Long memberId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM Loan l WHERE l.member.id = :memberId AND l.status = 'ACTIVE'", Loan.class)
                    .setParameter("memberId", memberId)
                    .uniqueResult();
        }
    }

    public List<Loan> findByMemberId(Long memberId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM Loan l WHERE l.member.id = :memberId ORDER BY l.approvedAt DESC", Loan.class)
                    .setParameter("memberId", memberId)
                    .list();
        }
    }

    public List<Loan> findAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM Loan", Loan.class).list();
        }
    }

    public void update(Loan loan) {
        Transaction tx = null;
        Session session = HibernateUtil.getSessionFactory().openSession();
        try {
            tx = session.beginTransaction();
            session.merge(loan);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw e;
        } finally {
            session.close();
        }
    }
}
