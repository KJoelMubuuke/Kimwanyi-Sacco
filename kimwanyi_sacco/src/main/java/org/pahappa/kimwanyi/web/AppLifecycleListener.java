package org.pahappa.kimwanyi.web;

import com.mysql.cj.jdbc.AbandonedConnectionCleanupThread;
import org.pahappa.kimwanyi.util.HibernateUtil;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Enumeration;

@WebListener
public class AppLifecycleListener implements ServletContextListener {

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        HibernateUtil.shutdown();
        deregisterJdbcDrivers();
        AbandonedConnectionCleanupThread.checkedShutdown();
    }

    private void deregisterJdbcDrivers() {
        ClassLoader webappClassLoader = Thread.currentThread().getContextClassLoader();
        Enumeration<Driver> drivers = DriverManager.getDrivers();
        while (drivers.hasMoreElements()) {
            Driver driver = drivers.nextElement();
            if (driver.getClass().getClassLoader() == webappClassLoader) {
                try {
                    DriverManager.deregisterDriver(driver);
                } catch (SQLException ignored) {
                    // Tomcat is shutting down; there is no useful recovery path here.
                }
            }
        }
    }
}
