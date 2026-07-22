package org.pahappa.kimwanyi.web;

import org.pahappa.kimwanyi.util.HibernateUtil;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@ManagedBean(name = "appStatus")
@RequestScoped
public class AppStatusBean implements Serializable {

    private static final DateTimeFormatter DISPLAY_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public String getApplicationName() {
        return "Kimwanyi SACCO";
    }

    public String getRenderedAt() {
        return LocalDateTime.now().format(DISPLAY_TIME);
    }

    public String getHibernateStatus() {
        try {
            HibernateUtil.getSessionFactory();
            return "Hibernate session factory is ready";
        } catch (Throwable ex) {
            return "Hibernate is not ready: " + rootMessage(ex);
        }
    }

    public boolean isHibernateReady() {
        try {
            HibernateUtil.getSessionFactory();
            return true;
        } catch (Throwable ex) {
            return false;
        }
    }

    private String rootMessage(Throwable throwable) {
        Throwable current = throwable;
        while (current.getCause() != null) {
            current = current.getCause();
        }
        String message = current.getMessage();
        return message == null || message.isBlank()
                ? current.getClass().getSimpleName()
                : message;
    }
}
