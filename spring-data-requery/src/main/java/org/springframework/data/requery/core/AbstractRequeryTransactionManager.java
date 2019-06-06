package org.springframework.data.requery.core;

import io.requery.sql.EntityDataStore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionStatus;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.annotation.Nonnull;

/**
 * AbstractRequeryTransactionManager
 *
 * @author debop (Sunghyouk Bae)
 */
@Slf4j
abstract class AbstractRequeryTransactionManager implements PlatformTransactionManager {

    protected final EntityDataStore<Object> entityDataStore;
    private ThreadLocal<Integer> transactionCount = ThreadLocal.withInitial(() -> 0);

    AbstractRequeryTransactionManager(@Nonnull final EntityDataStore<Object> entityDataStore) {
        this.entityDataStore = entityDataStore;
    }

    @Override
    public TransactionStatus getTransaction(TransactionDefinition definition) throws TransactionException {
        increaseTransactionCount();
        log.trace("GetTransaction... transaction count={}, definition={}", transactionCount.get(), definition);

        if (!definition.isReadOnly() && !entityDataStore.transaction().active()) {
            log.debug("Begin Requery transaction. definition={}", definition);
            entityDataStore.transaction().begin();
        }
        TransactionSynchronizationManager.setCurrentTransactionReadOnly(definition.isReadOnly());

        return new DefaultTransactionStatus(entityDataStore.transaction(),
                                            true,
                                            true,
                                            definition.isReadOnly(),
                                            false,
                                            null);
    }

    @Override
    public void commit(TransactionStatus status) throws TransactionException {
        if (entityDataStore.transaction().active()) {
            decreaseTransactionCount();
            log.debug("Commit ... transaction count={}, status={}", transactionCount.get(), getTransactionStatusDescription(status));
            if (transactionCount.get() == 0) {
                log.info("Commit Requery transaction. status={}", getTransactionStatusDescription(status));
                entityDataStore.transaction().commit();
                entityDataStore.transaction().close();
                removeTransactionCount();
            }
        }
    }

    @Override
    public void rollback(TransactionStatus status) throws TransactionException {
        log.debug("Rollback ...");
        if (entityDataStore.transaction().active()) {
            log.warn("Rollback Requery transaction. status={}", getTransactionStatusDescription(status));
            entityDataStore.transaction().rollback();
            entityDataStore.transaction().close();
            removeTransactionCount();
        }
    }

    private String getTransactionStatusDescription(TransactionStatus status) {
        return "isCompleted=" + status.isCompleted()
               + ", isNewTransaction=" + status.isNewTransaction()
               + ", isRollbackOnly=" + status.isRollbackOnly();
    }

    private void increaseTransactionCount() {
        transactionCount.set(transactionCount.get() + 1);
    }

    private void decreaseTransactionCount() {
        transactionCount.set(transactionCount.get() - 1);
    }

    private void removeTransactionCount() {
        transactionCount.remove();
    }
}
