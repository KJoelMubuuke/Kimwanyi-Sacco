package org.pahappa.kimwanyi.service;

import org.mindrot.jbcrypt.BCrypt;
import org.pahappa.kimwanyi.dao.AdminDAO;
import org.pahappa.kimwanyi.dao.MemberDAO;
import org.pahappa.kimwanyi.dao.SystemLogDAO;
import org.pahappa.kimwanyi.model.Admin;
import org.pahappa.kimwanyi.model.Member;
import org.pahappa.kimwanyi.model.SystemLog;

/**
 * Login for both roles through one entry point.
 *
 * Lookup order:
 *   1. Admins    → by username
 *   2. Members   → by membership number (primary identifier per spec)
 *   3. Members   → by email (fallback, for convenience)
 *
 * A member can sign in with either their membership number
 * or their email address — whichever they remember.
 *
 * Every login attempt (success or failure) is written to the system_logs
 * table so the dashboard can show "Recent System Logs".
 */
public class AuthService {

    private final AdminDAO adminDAO = new AdminDAO();
    private final MemberDAO memberDAO = new MemberDAO();
    private final SystemLogDAO systemLogDAO = new SystemLogDAO();

    /**
     * @throws IllegalArgumentException if the credentials don't match any
     *         account, or the matching member account is DEACTIVATED
     */
    public AuthResult login(String identifier, String plainPassword) {
        return login(identifier, plainPassword, null);
    }

    /**
     * Login overload that also captures the caller's IP address for the
     * system log.  Preferred call site.
     *
     * @param ipAddress HTTP client IP, or null if unavailable
     * @throws IllegalArgumentException if the credentials don't match any
     *         account, or the matching member account is DEACTIVATED
     */
    public AuthResult login(String identifier, String plainPassword, String ipAddress) {
        if (identifier == null || identifier.isBlank() || plainPassword == null || plainPassword.isBlank()) {
            throw new IllegalArgumentException("Identifier and password are required");
        }

        // 1. Try admin by username
        Admin admin = adminDAO.findByUsername(identifier);
        if (admin != null && BCrypt.checkpw(plainPassword, admin.getPasswordHash())) {
            writeLog("Logged In", admin.getFullName(), "A", "SUCCESSFUL", ipAddress);
            return new AuthResult(AuthResult.Role.ADMIN, admin.getId(), admin.getFullName());
        }

        // 2. Try member by National ID (NIN - primary identifier per spec)
        Member member = memberDAO.findByNationalId(identifier);

        // 3. Fallback: try member by email
        if (member == null) {
            member = memberDAO.findByEmail(identifier);
        }

        if (member != null && BCrypt.checkpw(plainPassword, member.getPasswordHash())) {
            if (!"ACTIVE".equals(member.getStatus())) {
                writeLog("Logged In", member.getFullName(), "M", "FAILED", ipAddress);
                throw new IllegalArgumentException("This member account has been deactivated");
            }
            writeLog("Logged In", member.getFullName(), "M", "SUCCESSFUL", ipAddress);
            return new AuthResult(AuthResult.Role.MEMBER, member.getId(), member.getFullName());
        }

        // Unknown identifier or wrong password — log a failed attempt
        writeLog("Logged In", identifier, "M", "FAILED", ipAddress);
        throw new IllegalArgumentException("Invalid credentials. Use your National ID or email.");
    }

    /** Records a logout for the given display name. */
    public void recordLogout(String displayName, String actorType, String ipAddress) {
        writeLog("Logged Out", displayName, actorType, "SUCCESSFUL", ipAddress);
    }

    // ---- private helpers ------------------

    private void writeLog(String action, String actorName, String actorType,
                          String status, String ipAddress) {
        try {
            SystemLog log = new SystemLog();
            log.setAction(action);
            log.setActorName(actorName);
            log.setActorType(actorType);
            log.setStatus(status);
            log.setIpAddress(ipAddress);
            systemLogDAO.save(log);
        } catch (Exception ignored) {
            // Logging must never crash the business flow
        }
    }
}