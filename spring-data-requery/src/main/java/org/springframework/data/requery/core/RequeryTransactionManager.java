package org.springframework.data.requery.core;

import io.requery.Transaction;
import io.requery.sql.EntityDataStore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.support.ResourceTransactionManager;

import javax.annotation.Nonnull;

/**
 * Requery용 {@link DataSourceTransactionManager}
 *
 * @author debop
 * @since 18. 6. 14
 */
@Slf4j
public class RequeryTransactionManager extends AbstractRequeryTransactionManager
    implements ResourceTransactionManager, InitializingBean {
    private static final long serialVersionUID = 3291422158479490099L;

    // private EntityDataStore entityDataStore;
    private boolean enforceReadOnly = false;

    public RequeryTransactionManager(@Nonnull final EntityDataStore entityDataStore) {
        super(entityDataStore);
        // this.entityDataStore = entityDataStore;
        // setNestedTransactionAllowed(true);
        afterPropertiesSet();
    }

    public void setEnforceReadOnly(boolean enforceReadOnly) {
        this.enforceReadOnly = enforceReadOnly;
    }

    public boolean isEnforceReadOnly() {
        return this.enforceReadOnly;
    }

    @Override
    public void afterPropertiesSet() {
        if (this.entityDataStore == null) {
            throw new IllegalArgumentException("Property 'entityDataStore' is required");
        }
    }

    @Nonnull
    @Override
    public Object getResourceFactory() {
        return entityDataStore;
    }

    public Transaction getCurrentTransaction() {
        return entityDataStore.transaction();
    }

}
