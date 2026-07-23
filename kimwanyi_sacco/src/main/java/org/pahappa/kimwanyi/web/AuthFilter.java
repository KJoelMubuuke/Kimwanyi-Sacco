package org.pahappa.kimwanyi.web;

import org.pahappa.kimwanyi.service.AuthResult;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * Prevents unauthenticated access to member and admin pages.
 * Also enforces role-based routing: members cannot access /admin/* and vice versa.
 */
@WebFilter(urlPatterns = {"/member/*", "/admin/*"})
public class AuthFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        HttpSession session = req.getSession(false);
        AuthResult currentUser = null;
        if (session != null) {
            // LoginBean is @SessionScoped; retrieve the stored user from the session map
            Object loginBean = session.getAttribute("loginBean");
            if (loginBean instanceof org.pahappa.kimwanyi.bean.LoginBean) {
                currentUser = ((org.pahappa.kimwanyi.bean.LoginBean) loginBean).getCurrentUser();
            }
        }

        String requestURI = req.getRequestURI();
        String contextPath = req.getContextPath();

        if (currentUser == null) {
            // Not logged in - redirect to login
            res.sendRedirect(contextPath + "/login.xhtml");
            return;
        }

        boolean isAdminPage = requestURI.startsWith(contextPath + "/admin/");
        boolean isMemberPage = requestURI.startsWith(contextPath + "/member/");

        if (isAdminPage && currentUser.getRole() != AuthResult.Role.ADMIN) {
            res.sendRedirect(contextPath + "/member/dashboard.xhtml");
            return;
        }
        if (isMemberPage && currentUser.getRole() != AuthResult.Role.MEMBER) {
            res.sendRedirect(contextPath + "/admin/dashboard.xhtml");
            return;
        }

        chain.doFilter(request, response);
    }
}
