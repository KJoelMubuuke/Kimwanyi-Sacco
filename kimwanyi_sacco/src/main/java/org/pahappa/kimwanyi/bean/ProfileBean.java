package org.pahappa.kimwanyi.bean;

import org.mindrot.jbcrypt.BCrypt;
import org.pahappa.kimwanyi.dao.MemberDAO;
import org.pahappa.kimwanyi.model.Member;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import java.io.Serializable;

@ManagedBean(name = "profileBean")
@ViewScoped
public class ProfileBean implements Serializable {

    @ManagedProperty(value = "#{loginBean}")
    private LoginBean loginBean;

    private final MemberDAO memberDAO = new MemberDAO();

    // Profile fields — null means "not yet loaded"
    private String fullName;
    private String phoneNumber;
    private String email;
    private boolean loaded = false;

    // Password change fields
    private String currentPassword;
    private String newPassword;
    private String confirmNewPassword;

    /** Lazy load from DB on first access. */
    private void ensureLoaded() {
        if (!loaded) {
            Member member = memberDAO.findById(loginBean.getCurrentUser().getUserId());
            if (member != null) {
                fullName    = member.getFullName();
                phoneNumber = member.getPhoneNumber();
                email       = member.getEmail();
            }
            loaded = true;
        }
    }

    /** Save name, phone, email. */
    public String saveProfile() {
        if (fullName == null || fullName.isBlank()) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Full name is required.", null));
            return null;
        }
        try {
            Member member = memberDAO.findById(loginBean.getCurrentUser().getUserId());
            member.setFullName(fullName.trim());
            member.setPhoneNumber(phoneNumber);
            member.setEmail(email);
            memberDAO.update(member);
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Profile updated successfully.", null));
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Update failed: " + e.getMessage(), null));
        }
        return null;
    }

    /** Change password — requires correct current password. */
    public String changePassword() {
        if (newPassword == null || newPassword.length() < 6) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "New password must be at least 6 characters.", null));
            return null;
        }
        if (!newPassword.equals(confirmNewPassword)) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "New passwords do not match.", null));
            return null;
        }
        try {
            Member member = memberDAO.findById(loginBean.getCurrentUser().getUserId());
            if (!BCrypt.checkpw(currentPassword, member.getPasswordHash())) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Current password is incorrect.", null));
                return null;
            }
            member.setPasswordHash(BCrypt.hashpw(newPassword, BCrypt.gensalt()));
            memberDAO.update(member);
            // Clear fields
            currentPassword = null; newPassword = null; confirmNewPassword = null;
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Password changed successfully.", null));
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Password change failed: " + e.getMessage(), null));
        }
        return null;
    }

    // Read-only info (from DB, not editable)
    public String getMembershipNumber() {
        Member m = memberDAO.findById(loginBean.getCurrentUser().getUserId());
        return m != null ? m.getMembershipNumber() : "";
    }
    public String getNationalId() {
        Member m = memberDAO.findById(loginBean.getCurrentUser().getUserId());
        return m != null ? m.getNationalId() : "";
    }
    public String getStatus() {
        Member m = memberDAO.findById(loginBean.getCurrentUser().getUserId());
        return m != null ? m.getStatus() : "";
    }

    // Getters & Setters
    public String getFullName()        { ensureLoaded(); return fullName; }
    public void setFullName(String v)  { this.fullName = v; }

    public String getPhoneNumber()        { ensureLoaded(); return phoneNumber; }
    public void setPhoneNumber(String v)  { this.phoneNumber = v; }

    public String getEmail()        { ensureLoaded(); return email; }
    public void setEmail(String v)  { this.email = v; }

    public String getCurrentPassword()        { return currentPassword; }
    public void setCurrentPassword(String v)  { this.currentPassword = v; }

    public String getNewPassword()        { return newPassword; }
    public void setNewPassword(String v)  { this.newPassword = v; }

    public String getConfirmNewPassword()        { return confirmNewPassword; }
    public void setConfirmNewPassword(String v)  { this.confirmNewPassword = v; }

    public void setLoginBean(LoginBean loginBean) { this.loginBean = loginBean; }
}
