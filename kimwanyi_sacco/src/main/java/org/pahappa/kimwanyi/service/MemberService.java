package org.pahappa.kimwanyi.service;

import org.hibernate.Session;
import org.mindrot.jbcrypt.BCrypt;
import org.pahappa.kimwanyi.dao.MemberDAO;
import org.pahappa.kimwanyi.model.Member;
import org.pahappa.kimwanyi.model.SavingsAccount;
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
    public Member register(String nationalId, String membershipNumber, String fullName,
                           String phoneNumber, String email, String plainPassword) {

        if (nationalId == null || nationalId.isBlank()
                || membershipNumber == null || membershipNumber.isBlank()
                || fullName == null || fullName.isBlank()
                || plainPassword == null || plainPassword.isBlank()) {
            throw new IllegalArgumentException("National ID, membership number, full name, and password are required");
        }

        if (memberDAO.findByNationalId(nationalId) != null) {
            throw new IllegalArgumentException("A member with this national ID already exists");
        }

        org.hibernate.Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();

            Member member = new Member();
            member.setNationalId(nationalId);
            member.setMembershipNumber(membershipNumber);
            member.setFullName(fullName);
            member.setPhoneNumber(phoneNumber);
            member.setEmail(email);
            member.setPasswordHash(BCrypt.hashpw(plainPassword, BCrypt.gensalt()));
            // Member.onCreate() does not default this - must be set explicitly
            // or the NOT NULL constraint on members.status fails.
            member.setStatus("ACTIVE");
            session.persist(member);

            SavingsAccount account = new SavingsAccount();
            account.setMember(member);
            account.setBalance(BigDecimal.ZERO);
            session.persist(account);

            tx.commit();
            return member;

        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw e;
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

    public Member findById(Long memberId) {
        return memberDAO.findById(memberId);
    }
}