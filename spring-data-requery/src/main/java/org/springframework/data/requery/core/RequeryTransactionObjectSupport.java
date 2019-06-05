package org.springframework.data.requery.core;

import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.SavepointManager;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.support.SmartTransactionObject;
import org.springframework.util.Assert;

import javax.annotation.Nullable;

/**
 * RequeryTransactionObjectSupport
 *
 * @author debop (Sunghyouk Bae)
 */
@Slf4j
public abstract class RequeryTransactionObjectSupport implements SavepointManager, SmartTransactionObject {

    @Nullable
    private RequeryTransactionHolder transactionHolder;

    @Nullable
    private Integer previousIsolationLevel;

    void setTransactionHolder(@Nullable RequeryTransactionHolder transactionHolder) {
        this.transactionHolder = transactionHolder;
    }

    public RequeryTransactionHolder getTransactionHolder() {
        Assert.state(this.transactionHolder != null, "No RequeryTransactionHolder available");
        return this.transactionHolder;
    }

    public boolean hasTransactionHolder() {
        return (this.transactionHolder != null);
    }

    public void setPreviousIsolationLevel(@Nullable Integer previousIsolationLevel) {
        this.previousIsolationLevel = previousIsolationLevel;
    }

    @Nullable
    public Integer getPreviousIsolationLevel() {
        return this.previousIsolationLevel;
    }

    @Override
    public Object createSavepoint() throws TransactionException {
        return null;
    }

    @Override
    public void rollbackToSavepoint(Object savepoint) throws TransactionException {
        // no-op
    }

    @Override
    public void releaseSavepoint(Object savepoint) throws TransactionException {
        // no-op
    }

    @Override
    public void flush() {
        // no-op
    }
}
