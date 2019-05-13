package org.springframework.data.requery.core;

import io.requery.TransactionIsolation;
import io.requery.sql.EntityDataStore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.annotation.Transient;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.JdbcTransactionObjectSupport;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.DefaultTransactionStatus;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.sql.DataSource;
import java.sql.Connection;

/**
 * Requery용 {@link DataSourceTransactionManager}
 *
 * @author debop
 * @since 18. 6. 14
 */
@Slf4j
public class RequeryTransactionManager extends DataSourceTransactionManager {
    private static final long serialVersionUID = 3291422158479490099L;

    @Transient
    private transient EntityDataStore entityDataStore;

    public RequeryTransactionManager(@Nonnull final EntityDataStore entityDataStore,
                                     @Nonnull final DataSource dataSource) {
        super(dataSource);
        this.entityDataStore = entityDataStore;
    }

    @Override
    protected void doBegin(@Nonnull final Object transaction, @Nonnull final TransactionDefinition definition) {
        // NOTE: requery 용 Transaction을 쓰지 않도록 해야 spring 의 transaction을 사용한다
        //
//        if (!definition.isReadOnly() && !entityDataStore.transaction().active()) {
//            TransactionIsolation isolation = getTransactionIsolation(definition.getIsolationLevel());
//
//            log.info("Begin requery transaction. {}", entityDataStore.transaction().getClass().getSimpleName());
//            if (isolation != null) {
//                entityDataStore.transaction().begin(isolation);
//            } else {
//                entityDataStore.transaction().begin();
//            }
//        }
        log.debug("Begin transaction... definition={}", definition);
        super.doBegin(transaction, definition);
    }

    @Override
    protected void doCommit(@Nonnull final DefaultTransactionStatus status) {
        if (status.isNewSynchronization() && entityDataStore.transaction().active()) {
            log.info("Commit requery transaction. status={}", status.getTransaction());
            try {
                entityDataStore.transaction().commit();
            } finally {
                entityDataStore.transaction().close();
                status.setCompleted();
            }
        }
        log.info("Commit transaction. status={}", status);
        super.doCommit(status);
    }

    @Override
    protected void doRollback(@Nonnull final DefaultTransactionStatus status) {
        if (entityDataStore.transaction().active()) {
            log.warn("Rollback requery transaction!!! status={}", status);
            try {
                entityDataStore.transaction().rollback();
            } finally {
                entityDataStore.transaction().close();
                status.setRollbackOnly();
            }
        }
        log.warn("Rollback transaction!!! status={}", status);
        super.doRollback(status);
    }

    @Nonnull
    @Override
    protected Object doSuspend(@Nonnull final Object transaction) {
        log.debug("Suspend transaction. transaction={}", transaction);
        return super.doSuspend(transaction);
    }

    @Override
    protected void doResume(@Nullable final Object transaction,
                            @Nonnull final Object suspendedResources) {
        log.debug("Resume transaction. transaction={}, suspendedResources={}", transaction, suspendedResources);
        super.doResume(transaction, suspendedResources);
    }

    private @Nullable
    TransactionIsolation getTransactionIsolation(int isolationLevel) {
        switch (isolationLevel) {
            case Connection.TRANSACTION_NONE:
                return TransactionIsolation.NONE;
            case Connection.TRANSACTION_READ_UNCOMMITTED:
                return TransactionIsolation.READ_UNCOMMITTED;
            case Connection.TRANSACTION_READ_COMMITTED:
                return TransactionIsolation.READ_COMMITTED;
            case Connection.TRANSACTION_REPEATABLE_READ:
                return TransactionIsolation.REPEATABLE_READ;
            case Connection.TRANSACTION_SERIALIZABLE:
                return TransactionIsolation.SERIALIZABLE;

            default:
                return null;
        }
    }
}
