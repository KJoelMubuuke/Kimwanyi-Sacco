package org.pahappa.kimwanyi.bean;

import org.pahappa.kimwanyi.dao.MemberDAO;
import org.pahappa.kimwanyi.model.Member;
import org.pahappa.kimwanyi.service.MemberService;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import java.io.Serializable;
import java.util.List;

@ManagedBean(name = "adminMembersBean")
@ViewScoped
public class AdminMembersBean implements Serializable {

    private final MemberService memberService = new MemberService();
    private final MemberDAO memberDAO = new MemberDAO();

    // Registration form fields
    private String nationalId;
    private String fullName;
    private String phoneNumber;
    private String email;
    private String password;

    public List<Member> getAllMembers() {
        return memberDAO.findAll();
    }

    public String registerMember() {
        try {
            memberService.register(nationalId, fullName, phoneNumber, email, password);
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Member '" + fullName + "' registered successfully.", null));
            // Clear form
            nationalId = null; fullName = null;
            phoneNumber = null; email = null; password = null;
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, e.getMessage(), null));
        }
        return null;
    }

    public String deactivate(Long memberId) {
        try {
            memberService.deactivate(memberId);
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Member account deactivated.", null));
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, e.getMessage(), null));
        }
        return null;
    }

    public String activate(Long memberId) {
        try {
            memberService.activate(memberId);
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Member account activated.", null));
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, e.getMessage(), null));
        }
        return null;
    }

    // Getters & Setters
    public String getNationalId() { return nationalId; }
    public void setNationalId(String nationalId) { this.nationalId = nationalId; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
