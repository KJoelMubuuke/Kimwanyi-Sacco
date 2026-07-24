package org.pahappa.kimwanyi.dao;

import org.hibernate.Session;
import org.pahappa.kimwanyi.model.TransactionType;
import org.pahappa.kimwanyi.util.HibernateUtil;

import java.util.List;

public class TransactionTypeDAO {
    public void save(TransactionType transactionType) {
        org.hibernate.Transaction tx = null;
        Session session = HibernateUtil.getSessionFactory().openSession();
        try {
            tx = session.beginTransaction();
            session.save(transactionType);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw e;
        } finally {
            session.close();
        }
    }

    public List<TransactionType> findAll() {
        Session session = HibernateUtil.getSessionFactory().openSession();
        try {
            return session.createQuery("FROM TransactionType", TransactionType.class).list();
        } finally {
            session.close();
        }
    }

    public void delete(Long id) {
        org.hibernate.Transaction tx = null;
        Session session = HibernateUtil.getSessionFactory().openSession();
        try {
            tx = session.beginTransaction();
            TransactionType type = session.get(TransactionType.class, id);
            if (type != null) {
                session.delete(type);
            }
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw e;
        } finally {
            session.close();
        }
    }
}
