package org.pahappa.kimwanyi.service;

/**
 * Carries the outcome of a successful login. Exactly one of member/admin
 * is non-null, matching your two-table design - there is no shared
 * "users" table, so the caller (a JSF login bean) needs to know which
 * kind of account just authenticated.
 */
public class AuthResult {

    public enum Role { MEMBER, ADMIN }

    private final Role role;
    private final Long userId;
    private final String displayName;

    public AuthResult(Role role, Long userId, String displayName) {
        this.role = role;
        this.userId = userId;
        this.displayName = displayName;
    }

    public Role getRole() { return role; }
    public Long getUserId() { return userId; }
    public String getDisplayName() { return displayName; }
}