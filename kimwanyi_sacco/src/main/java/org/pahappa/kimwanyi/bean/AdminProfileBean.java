package org.pahappa.kimwanyi.bean;

import org.mindrot.jbcrypt.BCrypt;
import org.pahappa.kimwanyi.dao.AdminDAO;
import org.pahappa.kimwanyi.model.Admin;
import org.pahappa.kimwanyi.service.AuditLogService;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import java.io.Serializable;

/**
 * Backs the Admin "My Profile" page.
 * Lets admins view their account info and change their password.
 */
@ManagedBean(name = "adminProfileBean")
@ViewScoped
public class AdminProfileBean implements Serializable {

    @ManagedProperty(value = "#{loginBean}")
    private LoginBean loginBean;

    private final AdminDAO adminDAO = new AdminDAO();

    // Password change fields
    private String currentPassword;
    private String newPassword;
    private String confirmNewPassword;

    /** Read-only — admin's display name from session. */
    public String getDisplayName() {
        return loginBean.getCurrentUser() != null
                ? loginBean.getCurrentUser().getDisplayName()
                : "";
    }

    /** Fetch admin from DB for username display. */
    public String getUsername() {
        Admin admin = adminDAO.findById(loginBean.getCurrentUser().getUserId());
        return admin != null ? admin.getUsername() : "";
    }

    /** Change the admin's password — requires the correct current password. */
    public String changePassword() {
        if (newPassword == null || newPassword.length() < 8) {
            addError("New password must be at least 8 characters.");
            return null;
        }
        if (!newPassword.equals(confirmNewPassword)) {
            addError("New passwords do not match.");
            return null;
        }
        try {
            Admin admin = adminDAO.findById(loginBean.getCurrentUser().getUserId());
            if (admin == null) {
                addError("Admin account not found.");
                return null;
            }
            if (!BCrypt.checkpw(currentPassword, admin.getPasswordHash())) {
                addError("Current password is incorrect.");
                return null;
            }
            admin.setPasswordHash(BCrypt.hashpw(newPassword, BCrypt.gensalt()));
            adminDAO.update(admin);
            AuditLogService.log("Admin Password Changed", admin.getFullName(),
                    admin.getFullName(), "A", "SUCCESSFUL");
            currentPassword = null;
            newPassword = null;
            confirmNewPassword = null;
            addInfo("Password changed successfully.");
        } catch (Exception e) {
            addError("Password change failed: " + e.getMessage());
        }
        return null;
    }

    private void addError(String msg) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, msg, null));
    }
    private void addInfo(String msg) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, msg, null));
    }

    // Getters & Setters
    public String getCurrentPassword()       { return currentPassword; }
    public void setCurrentPassword(String v) { this.currentPassword = v; }

    public String getNewPassword()       { return newPassword; }
    public void setNewPassword(String v) { this.newPassword = v; }

    public String getConfirmNewPassword()       { return confirmNewPassword; }
    public void setConfirmNewPassword(String v) { this.confirmNewPassword = v; }

    public void setLoginBean(LoginBean loginBean) { this.loginBean = loginBean; }
}
