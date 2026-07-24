package org.pahappa.kimwanyi.dao;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.pahappa.kimwanyi.model.Member;
import org.pahappa.kimwanyi.util.HibernateUtil;

import java.util.List;

public class MemberDAO {

    public void save(Member member) {
        Transaction tx = null;
        Session session = HibernateUtil.getSessionFactory().openSession();
        try {
            tx = session.beginTransaction();
            session.persist(member);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw e;
        } finally {
            session.close();
        }
    }

    public Member findById(Long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(Member.class, id);
        }
    }

    public Member findByEmail(String email) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM Member m WHERE m.email = :email", Member.class)
                    .setParameter("email", email)
                    .uniqueResult();
        }
    }

    public Member findByNationalId(String nationalId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM Member m WHERE m.nationalId = :nid", Member.class)
                    .setParameter("nid", nationalId)
                    .uniqueResult();
        }
    }

    public Member findByMembershipNumber(String membershipNumber) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM Member m WHERE m.membershipNumber = :mno", Member.class)
                    .setParameter("mno", membershipNumber)
                    .uniqueResult();
        }
    }

    public List<Member> findAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM Member", Member.class).list();
        }
    }

    public List<Member> searchByName(String nameQuery) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM Member m WHERE lower(m.fullName) LIKE lower(:nameQuery)", Member.class)
                    .setParameter("nameQuery", "%" + nameQuery + "%")
                    .list();
        }
    }

    public void update(Member member) {
        Transaction tx = null;
        Session session = HibernateUtil.getSessionFactory().openSession();
        try {
            tx = session.beginTransaction();
            session.merge(member);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw e;
        } finally {
            session.close();
        }
    }
}