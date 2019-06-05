package org.springframework.data.requery.core;

import io.requery.Transaction;
import lombok.Getter;
import org.springframework.transaction.support.ResourceHolderSupport;

import javax.annotation.Nullable;

/**
 * RequeryTransactionHolder
 *
 * @author debop (Sunghyouk Bae)
 */
@Getter
public class RequeryTransactionHolder extends ResourceHolderSupport {

    @Nullable
    private Transaction transaction;
    private boolean transactionActive = false;

    RequeryTransactionHolder(Transaction transaction) {
        this.transaction = transaction;
    }

    public RequeryTransactionHolder(Transaction transaction, boolean transactionActive) {
        this.transaction = transaction;
        this.transactionActive = transactionActive;
    }

    public boolean hasTransaction() {
        return transaction != null;
    }

    public boolean isTransactionActive() {
        return transaction != null && transaction.active();
    }

    @Override
    public void released() {
        super.released();
        if (transaction != null) {
            transaction.close();
        }
    }

    @Override
    public void clear() {
        super.clear();
        this.transactionActive = false;
    }
}
