package org.pahappa.kimwanyi.service;

import org.mindrot.jbcrypt.BCrypt;
import org.pahappa.kimwanyi.dao.AdminDAO;
import org.pahappa.kimwanyi.dao.MemberDAO;
import org.pahappa.kimwanyi.model.Admin;
import org.pahappa.kimwanyi.model.Member;

/**
 * Login for both roles through one entry point, since there's no shared
 * users table. Admins log in with username, members log in with email -
 * the single "identifier" field is checked against admins first, then
 * members, exactly as discussed: two separate lookups, not one shared
 * login table.
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

        Admin admin = adminDAO.findByUsername(identifier);
        if (admin != null && BCrypt.checkpw(plainPassword, admin.getPasswordHash())) {
            return new AuthResult(AuthResult.Role.ADMIN, admin.getId(), admin.getFullName());
        }

        Member member = memberDAO.findByEmail(identifier);
        if (member != null && BCrypt.checkpw(plainPassword, member.getPasswordHash())) {
            if (!"ACTIVE".equals(member.getStatus())) {
                throw new IllegalArgumentException("This member account has been deactivated");
            }
            return new AuthResult(AuthResult.Role.MEMBER, member.getId(), member.getFullName());
        }

        throw new IllegalArgumentException("Invalid credentials");
    }
}