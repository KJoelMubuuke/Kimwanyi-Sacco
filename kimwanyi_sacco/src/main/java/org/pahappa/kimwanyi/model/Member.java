package org.pahappa.kimwanyi.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "members")
public class Member {
   @Id
   @GeneratedValue(strategy = GenerationType.IDENTITY)
   private Long id;

   @Column(name = "national_id", nullable = false, unique = true, length = 30)
   private String nationalId;

   @Column(name = "membership_number", nullable = false, unique = true, length = 20)
   private String membershipNumber;
   
    @Column(name = "full_name", nullable = false, length = 120)
    private String fullName;

   @Column(name = "phone_number", length = 20)
   private String phoneNumber;
   
   @Column(name = "email", length = 120)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name ="status",nullable =false)
    private String status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
 
    @OneToOne(mappedBy = "member", cascade = CascadeType.ALL, fetch = FetchType.LAZY)

    private SavingsAccount savingsAccount;
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }


    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNationalId() { return nationalId; }
    public void setNationalId(String nationalId) { this.nationalId = nationalId; }

    public String getMembershipNumber() { return membershipNumber; }
    public void setMembershipNumber(String membershipNumber) { this.membershipNumber = membershipNumber; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    public SavingsAccount getSavingsAccount() { return savingsAccount; }
    public void setSavingsAccount(SavingsAccount savingsAccount) { this.savingsAccount = savingsAccount; }
  
}
