package org.pahappa.kimwanyi.service;

import org.mindrot.jbcrypt.BCrypt;
import org.pahappa.kimwanyi.dao.AdminDAO;
import org.pahappa.kimwanyi.dao.MemberDAO;
import org.pahappa.kimwanyi.model.Admin;
import org.pahappa.kimwanyi.model.Member;

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
 */
public class AuthService {

    private final AdminDAO adminDAO = new AdminDAO();
    private final MemberDAO memberDAO = new MemberDAO();

    /**
     * @throws IllegalArgumentException if the credentials don't match any
     *         account, or the matching member account is DEACTIVATED
     */
    public AuthResult login(String identifier, String plainPassword) {
        if (identifier == null || identifier.isBlank() || plainPassword == null || plainPassword.isBlank()) {
            throw new IllegalArgumentException("Identifier and password are required");
        }

        // 1. Try admin by username
        Admin admin = adminDAO.findByUsername(identifier);
        if (admin != null && BCrypt.checkpw(plainPassword, admin.getPasswordHash())) {
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
                throw new IllegalArgumentException("This member account has been deactivated");
            }
            return new AuthResult(AuthResult.Role.MEMBER, member.getId(), member.getFullName());
        }

        throw new IllegalArgumentException("Invalid credentials. Use your National ID or email.");
    }
}