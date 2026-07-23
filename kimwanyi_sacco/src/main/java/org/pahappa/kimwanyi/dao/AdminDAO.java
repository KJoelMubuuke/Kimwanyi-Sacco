package org.pahappa.kimwanyi.dao;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.pahappa.kimwanyi.model.Admin;
import org.pahappa.kimwanyi.util.HibernateUtil;

import java.util.List;

public class AdminDAO {

    public void save(Admin admin) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.persist(admin);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw e;
        }
    }

    public Admin findById(Long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(Admin.class, id);
        }
    }

    public Admin findByUsername(String username) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM Admin a WHERE a.username = :username", Admin.class)
                    .setParameter("username", username)
                    .uniqueResult();
        }
    }

    public List<Admin> findAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM Admin", Admin.class).list();
        }
    }

    public void update(Admin admin) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.merge(admin);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw e;
        }
    }
}