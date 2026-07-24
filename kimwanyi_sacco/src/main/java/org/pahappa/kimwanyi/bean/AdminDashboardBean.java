package org.pahappa.kimwanyi.bean;

import org.pahappa.kimwanyi.dao.SystemLogDAO;
import org.pahappa.kimwanyi.model.SystemLog;
import org.pahappa.kimwanyi.service.DashboardService;
import org.pahappa.kimwanyi.service.DashboardSummary;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import java.util.List;

@ManagedBean(name = "adminDashboardBean")
@RequestScoped
public class AdminDashboardBean {

    private final DashboardService dashboardService = new DashboardService();
    private final SystemLogDAO systemLogDAO = new SystemLogDAO();

    public DashboardSummary getSummary() {
        return dashboardService.getSummary();
    }

    /** Returns the 10 most recent audit log entries for the dashboard panel. */
    public List<SystemLog> getRecentLogs() {
        return systemLogDAO.findRecent(10);
    }
}