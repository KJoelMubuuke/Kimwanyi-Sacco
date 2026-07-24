package org.pahappa.kimwanyi.service;

import org.hibernate.Session;
import org.mindrot.jbcrypt.BCrypt;
import org.pahappa.kimwanyi.dao.MemberDAO;
import org.pahappa.kimwanyi.model.Member;
import org.pahappa.kimwanyi.model.SavingsAccount;
import org.pahappa.kimwanyi.service.AuditLogService;
import org.pahappa.kimwanyi.util.HibernateUtil;

import java.math.BigDecimal;

/**
 * Member registration and account status management.
 *
 * register() creates the Member row AND its SavingsAccount row in one
 * Hibernate session/transaction - same reasoning as AccountService and
 * LoanService.approveLoan(): a member with no savings account is a
 * broken state, so both writes commit together or neither does.
 */
public class MemberService {

    private final MemberDAO memberDAO = new MemberDAO();

    /**
     * @throws IllegalArgumentException if any required field is missing,
     *         or if the national ID / membership number / email is already
     *         in use (checked here first so the caller gets a clear message
     *         instead of a raw DB unique-constraint exception)
     */
    public Member register(String nationalId, String fullName, String gender,
                           String phoneNumber, String email, String plainPassword) {

        // --- Field validation (mirrors the JSF validators as a backend safety net) ---
        if (gender == null || gender.isBlank())
            throw new IllegalArgumentException("Gender must not be null.");
        if (nationalId == null || !nationalId.matches("[A-Za-z0-9]{14}"))
            throw new IllegalArgumentException("National ID must be exactly 14 alphanumeric characters.");

        if (fullName == null || !fullName.matches("[A-Za-z ]{2,120}"))
            throw new IllegalArgumentException("Full name must be 2–120 characters, letters and spaces only.");

        if (phoneNumber != null && !phoneNumber.isBlank()
                && !phoneNumber.matches("(\\+256|0)[0-9]{9}"))
            throw new IllegalArgumentException("Phone number must be a valid Uganda number (e.g. 0771234567 or +256771234567).");

        if (email == null || !email.matches("[a-zA-Z0-9._%+\\-]+@[a-zA-Z0-9.\\-]+\\.[a-zA-Z]{2,}"))
            throw new IllegalArgumentException("Email address is not valid (e.g. name@example.com).");

        if (plainPassword == null || !plainPassword.matches("(?=.*[A-Za-z])(?=.*[0-9]).{8,}"))
            throw new IllegalArgumentException("Password must be at least 8 characters and contain both letters and numbers.");

        if (memberDAO.findByNationalId(nationalId) != null) {
            throw new IllegalArgumentException("A member with this national ID already exists");
        }

        // --- Auto-generate unique membership number (e.g., KS-2026-A1B2C) ---
        String year = String.valueOf(java.time.Year.now().getValue());
        String generatedMembershipNumber;
        while (true) {
            String randomPart = java.util.UUID.randomUUID().toString().substring(0, 5).toUpperCase();
            generatedMembershipNumber = "KS-" + year + "-" + randomPart;
            if (memberDAO.findByMembershipNumber(generatedMembershipNumber) == null) {
                break; // It's unique!
            }
        }

        org.hibernate.Transaction tx = null;
        Session session = HibernateUtil.getSessionFactory().openSession();
        try {
            tx = session.beginTransaction();

            Member member = new Member();
            member.setNationalId(nationalId);
            member.setMembershipNumber(generatedMembershipNumber);
            member.setFullName(fullName);
            member.setGender(gender);
            member.setPhoneNumber(phoneNumber);
            member.setEmail(email);
            member.setPasswordHash(BCrypt.hashpw(plainPassword, BCrypt.gensalt()));
            // Member.onCreate() does not default this - must be set explicitly
            // or the NOT NULL constraint on members.status fails.
            member.setStatus("ACTIVE");
            session.persist(member);

            SavingsAccount account = new SavingsAccount();
            account.setMember(member);
            account.setBalance(new BigDecimal("20000.00"));
            session.persist(account);

            tx.commit();
            AuditLogService.log("Member Registration", fullName, fullName, "M", "SUCCESSFUL");
            return member;

        } catch (Exception e) {
            if (tx != null) tx.rollback();
            AuditLogService.log("Member Registration", fullName + " (FAILED)", fullName, "M", "FAILED");
            throw e;
        } finally {
            session.close();
        }
    }

    /**
     * @throws IllegalStateException if the member doesn't exist
     */
    public void deactivate(Long memberId) {
        Member member = memberDAO.findById(memberId);
        if (member == null) {
            throw new IllegalStateException("Member not found: " + memberId);
        }
        member.setStatus("DEACTIVATED");
        memberDAO.update(member);
    }

    /**
     * @throws IllegalStateException if the member doesn't exist
     */
    public void activate(Long memberId) {
        Member member = memberDAO.findById(memberId);
        if (member == null) {
            throw new IllegalStateException("Member not found: " + memberId);
        }
        member.setStatus("ACTIVE");
        memberDAO.update(member);
    }

    public Member findById(Long memberId) {
        return memberDAO.findById(memberId);
    }
}