package org.pahappa.kimwanyi.dao;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.pahappa.kimwanyi.model.SystemLog;
import org.pahappa.kimwanyi.util.HibernateUtil;

import java.util.List;

/**
 * Data access object for system audit logs.
 */
public class SystemLogDAO {

    /** Persist a single log entry. */
    public void save(SystemLog log) {
        Transaction tx = null;
        Session session = HibernateUtil.getSessionFactory().openSession();
        try {
            tx = session.beginTransaction();
            session.persist(log);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw e;
        } finally {
            session.close();
        }
    }

    /** Fetch the most recent N log entries, newest first. */
    public List<SystemLog> findRecent(int limit) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                            "FROM SystemLog sl ORDER BY sl.createdAt DESC", SystemLog.class)
                    .setMaxResults(limit)
                    .list();
        }
    }

    /** All logs, newest first. */
    public List<SystemLog> findAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                            "FROM SystemLog sl ORDER BY sl.createdAt DESC", SystemLog.class)
                    .list();
        }
    }
}
