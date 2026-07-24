package org.pahappa.kimwanyi.bean;

import org.pahappa.kimwanyi.service.AuthResult;
import org.pahappa.kimwanyi.service.AuthService;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import java.io.Serializable;

@ManagedBean(name = "loginBean")
@SessionScoped
public class LoginBean implements Serializable {

    private String identifier;
    private String password;

    // Set on successful login, read by pages/redirect logic.
    private AuthResult currentUser;

    /**
     * Called by the login form's command button.
     * Returns a JSF navigation outcome (a page name) on success,
     * or null (stay on the same page) on failure, with an error message queued.
     */
    public String login() {
        try {
            AuthService authService = new AuthService();
            String ip = getClientIp();
            currentUser = authService.login(identifier, password, ip);
            password = null; // never keep the plaintext around

            if (currentUser.getRole() == AuthResult.Role.ADMIN) {
                return "/admin/dashboard.xhtml?faces-redirect=true";
            } else {
                return "/member/dashboard.xhtml?faces-redirect=true";
            }
        } catch (IllegalArgumentException e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, e.getMessage(), null));
            return null; // stay on login page, show the message
        }
    }

    public String logout() {
        // Record logout before session is destroyed
        if (currentUser != null) {
            try {
                String actorType = (currentUser.getRole() == AuthResult.Role.ADMIN) ? "A" : "M";
                new AuthService().recordLogout(currentUser.getDisplayName(), actorType, getClientIp());
            } catch (Exception ignored) { /* logging must not block logout */ }
        }
        FacesContext.getCurrentInstance().getExternalContext().invalidateSession();
        return "/login.xhtml?faces-redirect=true";
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }

    /** Best-effort extraction of the real client IP (handles proxies). */
    private String getClientIp() {
        try {
            HttpServletRequest req = (HttpServletRequest)
                    FacesContext.getCurrentInstance().getExternalContext().getRequest();
            String ip = req.getHeader("X-Forwarded-For");
            if (ip == null || ip.isBlank() || "unknown".equalsIgnoreCase(ip)) {
                ip = req.getRemoteAddr();
            } else {
                ip = ip.split(",")[0].trim(); // first address before any proxy hops
            }
            return ip;
        } catch (Exception e) {
            return null;
        }
    }

    // --- getters/setters ---
    public String getIdentifier() { return identifier; }
    public void setIdentifier(String identifier) { this.identifier = identifier; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public AuthResult getCurrentUser() { return currentUser; }
}