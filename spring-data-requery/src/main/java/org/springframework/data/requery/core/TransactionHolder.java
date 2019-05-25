package org.springframework.data.requery.core;

import io.requery.Transaction;
import lombok.Getter;
import org.springframework.transaction.support.ResourceHolderSupport;

import javax.annotation.Nullable;

/**
 * TransactionHolder
 *
 * @author debop (Sunghyouk Bae)
 */
@Getter
public class TransactionHolder extends ResourceHolderSupport {

    @Nullable
    private Transaction currentTransaction;
    private boolean transactionActive = false;


    public TransactionHolder(Transaction transaction) {
        this.currentTransaction = transaction;
    }

    public TransactionHolder(Transaction transaction, boolean transactionActive) {
        this.currentTransaction = transaction;
        this.transactionActive = transactionActive;
    }

    protected boolean hasTransaction() {
        return currentTransaction != null;
    }

    protected boolean isTransactionActive() {
        return currentTransaction != null && currentTransaction.active();
    }

    @Override
    public void released() {
        super.released();
        if (currentTransaction != null) {
            currentTransaction.close();
        }
    }

    @Override
    public void clear() {
        super.clear();
        this.transactionActive = false;
    }
}
