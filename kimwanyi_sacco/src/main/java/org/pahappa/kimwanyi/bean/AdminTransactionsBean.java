package org.pahappa.kimwanyi.bean;

import org.pahappa.kimwanyi.dao.TransactionDAO;
import org.pahappa.kimwanyi.model.Transaction;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import java.util.List;

/**
 * Backs the Admin Transactions page — shows all financial transactions across
 * all member savings accounts.
 */
@ManagedBean(name = "adminTransactionsBean")
@RequestScoped
public class AdminTransactionsBean {

    private final TransactionDAO transactionDAO = new TransactionDAO();

    public List<Transaction> getAllTransactions() {
        return transactionDAO.findAll();
    }
}
