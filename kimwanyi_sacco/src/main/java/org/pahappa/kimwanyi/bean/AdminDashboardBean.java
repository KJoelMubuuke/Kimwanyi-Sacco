package org.pahappa.kimwanyi.bean;

import org.pahappa.kimwanyi.service.DashboardService;
import org.pahappa.kimwanyi.service.DashboardSummary;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;

@ManagedBean(name = "adminDashboardBean")
@RequestScoped
public class AdminDashboardBean {

    private final DashboardService dashboardService = new DashboardService();

    public DashboardSummary getSummary() {
        return dashboardService.getSummary();
    }
}