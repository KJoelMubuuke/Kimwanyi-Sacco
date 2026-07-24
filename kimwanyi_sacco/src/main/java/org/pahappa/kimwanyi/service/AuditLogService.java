package org.pahappa.kimwanyi.service;

import org.pahappa.kimwanyi.dao.SystemLogDAO;
import org.pahappa.kimwanyi.model.SystemLog;

/**
 * Central, fire-and-forget audit logging helper.
 *
 * Every method swallows its own exceptions so a logging failure
 * can NEVER propagate up and break a business operation.
 * Call from any service or bean.
 */
public class AuditLogService {

    private static final SystemLogDAO logDAO = new SystemLogDAO();

    /**
     * Write a log entry. All parameters are nullable — missing values are silently ignored.
     *
     * @param action    Short description of what happened, e.g. "Deposit", "Loan Approved"
     * @param detail    Optional extra detail, e.g. member name, amount
     * @param actorName Full name of the person who triggered the action
     * @param actorType "A" for Admin, "M" for Member
     * @param status    "SUCCESSFUL", "FAILED", or "INFO"
     */
    public static void log(String action, String detail, String actorName,
                           String actorType, String status) {
        log(action, detail, actorName, actorType, status, null);
    }

    /**
     * Overload that also captures the IP address.
     */
    public static void log(String action, String detail, String actorName,
                           String actorType, String status, String ipAddress) {
        try {
            SystemLog entry = new SystemLog();
            String fullAction = (detail != null && !detail.isBlank())
                    ? action + ": " + detail
                    : action;
            entry.setAction(fullAction);
            entry.setActorName(actorName);
            entry.setActorType(actorType != null ? actorType : "M");
            entry.setStatus(status != null ? status : "INFO");
            entry.setIpAddress(ipAddress);
            logDAO.save(entry);
        } catch (Exception ignored) {
            // Logging must never crash business logic
        }
    }
}
