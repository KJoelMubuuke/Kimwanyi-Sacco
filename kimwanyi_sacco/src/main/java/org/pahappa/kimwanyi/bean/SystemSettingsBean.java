package org.pahappa.kimwanyi.bean;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Backs the System Settings page.
 *
 * In a production system these values would be persisted in a
 * system_settings / configuration table. For now they are held in a
 * singleton-style in-memory store so the admin can view and edit them
 * within the session without requiring a DB schema change.
 *
 * Adding a settings table later only requires wiring save()/load() to
 * a DAO — the bean API exposed to the XHTML does not change.
 */
@ManagedBean(name = "systemSettingsBean")
@ViewScoped
public class SystemSettingsBean implements Serializable {

    // ---- Member account basic settings ----
    private BigDecimal individualMinimumAccountBalance = new BigDecimal("20000");
    private BigDecimal monthlyPaymentAmount             = new BigDecimal("5000");
    private BigDecimal minimumAccountBalance            = new BigDecimal("20000");
    private String     accountNumberPrefix              = "KS";
    private BigDecimal registrationFeeAmount            = new BigDecimal("10000");
    private int        monthlyPaymentDateDay            = 1; // day of the month

    // ---- Transaction message settings ----
    private String messageApiEndpoint = "";
    private String messageApiKey      = "";
    private String messageApiSenderId = "KimwanyiSACCO";

    // ---- Save action ----
    public String saveSettings() {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO,
                        "Settings saved successfully.", null));
        return null; // stay on same page
    }

    // ---- Getters / Setters ----

    public BigDecimal getIndividualMinimumAccountBalance() { return individualMinimumAccountBalance; }
    public void setIndividualMinimumAccountBalance(BigDecimal v) { this.individualMinimumAccountBalance = v; }

    public BigDecimal getMonthlyPaymentAmount() { return monthlyPaymentAmount; }
    public void setMonthlyPaymentAmount(BigDecimal v) { this.monthlyPaymentAmount = v; }

    public BigDecimal getMinimumAccountBalance() { return minimumAccountBalance; }
    public void setMinimumAccountBalance(BigDecimal v) { this.minimumAccountBalance = v; }

    public String getAccountNumberPrefix() { return accountNumberPrefix; }
    public void setAccountNumberPrefix(String v) { this.accountNumberPrefix = v; }

    public BigDecimal getRegistrationFeeAmount() { return registrationFeeAmount; }
    public void setRegistrationFeeAmount(BigDecimal v) { this.registrationFeeAmount = v; }

    public int getMonthlyPaymentDateDay() { return monthlyPaymentDateDay; }
    public void setMonthlyPaymentDateDay(int v) { this.monthlyPaymentDateDay = v; }

    public String getMessageApiEndpoint() { return messageApiEndpoint; }
    public void setMessageApiEndpoint(String v) { this.messageApiEndpoint = v; }

    public String getMessageApiKey() { return messageApiKey; }
    public void setMessageApiKey(String v) { this.messageApiKey = v; }

    public String getMessageApiSenderId() { return messageApiSenderId; }
    public void setMessageApiSenderId(String v) { this.messageApiSenderId = v; }
}
