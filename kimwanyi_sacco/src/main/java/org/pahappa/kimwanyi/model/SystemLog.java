package org.pahappa.kimwanyi.model;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Audit log entry — captures every login, logout, and key admin action.
 * Maps to the system_logs table.
 */
@Entity
@Table(name = "system_logs")
public class SystemLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** IP address of the request, e.g. "41.210.147.131" */
    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    /** Free-text description, e.g. "Logged In", "Add Transaction type: N/A" */
    @Column(name = "action", nullable = false, columnDefinition = "text")
    private String action;

    /** Display name of the user who performed the action (member or admin name) */
    @Column(name = "actor_name", length = 120)
    private String actorName;

    /** SUCCESSFUL | FAILED | INFO */
    @Column(name = "status", nullable = false, length = 20)
    private String status;

    /** M = Member, A = Admin; stored as a hint for the badge displayed in the UI */
    @Column(name = "actor_type", length = 1)
    private String actorType;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    // ---- getters / setters ----

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getActorName() { return actorName; }
    public void setActorName(String actorName) { this.actorName = actorName; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getActorType() { return actorType; }
    public void setActorType(String actorType) { this.actorType = actorType; }

    public LocalDateTime getCreatedAt() { return createdAt; }

    private static final DateTimeFormatter DISPLAY = DateTimeFormatter.ofPattern("yyyy-MM-dd (HH:mm:ss)");
    public String getCreatedAtDisplay() {
        return createdAt != null ? createdAt.format(DISPLAY) : "";
    }
}
