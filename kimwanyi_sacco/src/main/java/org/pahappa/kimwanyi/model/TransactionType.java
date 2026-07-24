package org.pahappa.kimwanyi.model;

import javax.persistence.*;

@Entity
@Table(name = "transaction_types")
public class TransactionType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, unique = true, length = 100)
    private String name;

    @Column(name = "account_effect", nullable = false, length = 20)
    private String accountEffect; // e.g. "Addition", "Reduction"

    @Column(name = "account_type", length = 50)
    private String accountType; // e.g. "Savings Account"

    @Column(name = "linked_bank", length = 100)
    private String linkedBank; // e.g. "Centenary Bank (3100401542)"

    @Column(name = "bank_effect", length = 20)
    private String bankEffect; // e.g. "Addition", "Reduction"

    @Column(name = "is_bank_active", nullable = false)
    private boolean bankActive = false;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getAccountEffect() { return accountEffect; }
    public void setAccountEffect(String accountEffect) { this.accountEffect = accountEffect; }

    public String getAccountType() { return accountType; }
    public void setAccountType(String accountType) { this.accountType = accountType; }

    public String getLinkedBank() { return linkedBank; }
    public void setLinkedBank(String linkedBank) { this.linkedBank = linkedBank; }

    public String getBankEffect() { return bankEffect; }
    public void setBankEffect(String bankEffect) { this.bankEffect = bankEffect; }

    public boolean isBankActive() { return bankActive; }
    public void setBankActive(boolean bankActive) { this.bankActive = bankActive; }
}
