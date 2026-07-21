package org.pahappa.kimwanyi.util;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

public class HibernateUtil {

    private static volatile SessionFactory sessionFactory;
    private static volatile Throwable initializationError;

    private HibernateUtil() {
    }

    public static SessionFactory getSessionFactory() {
        if (sessionFactory == null) {
            synchronized (HibernateUtil.class) {
                if (sessionFactory == null) {
                    sessionFactory = buildSessionFactory();
                }
            }
        }
        return sessionFactory;
    }

    public static Throwable getInitializationError() {
        return initializationError;
    }

    private static SessionFactory buildSessionFactory() {
        try {
            initializationError = null;
            return new Configuration().configure("hibernate.cfg.xml").buildSessionFactory();
        } catch (Throwable ex) {
            initializationError = ex;
            throw new IllegalStateException("SessionFactory creation failed", ex);
        }
    }

    public static void shutdown() {
        if (sessionFactory != null) {
            sessionFactory.close();
            sessionFactory = null;
        }
    }
}
