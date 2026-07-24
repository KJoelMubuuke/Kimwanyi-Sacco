package org.pahappa.kimwanyi.bean;

import org.pahappa.kimwanyi.dao.SystemLogDAO;
import org.pahappa.kimwanyi.model.SystemLog;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import java.util.List;

/**
 * Backs the full System Logs admin page.
 */
@ManagedBean(name = "systemLogsBean")
@RequestScoped
public class SystemLogsBean {

    private final SystemLogDAO systemLogDAO = new SystemLogDAO();

    public List<SystemLog> getAllLogs() {
        return systemLogDAO.findAll();
    }
}
