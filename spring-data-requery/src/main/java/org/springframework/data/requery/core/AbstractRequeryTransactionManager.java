package org.springframework.data.requery.core;

import io.requery.sql.EntityDataStore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionStatus;

/**
 * AbstractRequeryTransactionManager
 *
 * @author debop (Sunghyouk Bae)
 */
@Slf4j
abstract class AbstractRequeryTransactionManager implements PlatformTransactionManager {
    protected final EntityDataStore<Object> entityDataStore;

    private ThreadLocal<Integer> transactionCount = ThreadLocal.withInitial(() -> 0);

    AbstractRequeryTransactionManager(EntityDataStore<Object> entityDataStore) {
        this.entityDataStore = entityDataStore;
    }

    @Override
    public TransactionStatus getTransaction(TransactionDefinition definition) throws TransactionException {
        increaseTransactionCount();
        log.info("GetTransaction... transaction count={}, definition={}", transactionCount.get(), definition);
        if (!entityDataStore.transaction().active()) {
            log.info("Begin transaction. definition={}", definition);
            entityDataStore.transaction().begin();
        }
        return new DefaultTransactionStatus(entityDataStore.transaction(), true, true, false, false, null);
    }

    @Override
    public void commit(TransactionStatus status) throws TransactionException {
        if (entityDataStore.transaction().active()) {
            decreaseTransactionCount();
            log.info("commit ... transaction count={}", transactionCount.get());
            if (transactionCount.get() == 0) {
                log.info("Commit transaction. status={}", status);
                entityDataStore.transaction().commit();
                entityDataStore.transaction().close();
                removeTransactionCount();
            }
        }
    }

    @Override
    public void rollback(TransactionStatus status) throws TransactionException {
        if (entityDataStore.transaction().active()) {
            log.warn("Rollback transaction. status={}", status);
            entityDataStore.transaction().rollback();
            entityDataStore.transaction().close();
            removeTransactionCount();
        }
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
