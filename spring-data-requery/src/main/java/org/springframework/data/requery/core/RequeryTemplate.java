/*
 * Copyright 2018 Coupang Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.data.requery.core;

import io.requery.TransactionIsolation;
import io.requery.sql.EntityDataStore;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.requery.RequeryExecutionException;
import org.springframework.data.requery.mapping.RequeryMappingContext;
import org.springframework.util.Assert;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.Callable;
import java.util.function.Function;

/**
 * Java용 RequeryTemplate
 *
 * @author debop
 * @since 18. 6. 4
 */
@Slf4j
@Getter
public class RequeryTemplate implements RequeryOperations {

    private final EntityDataStore<Object> dataStore;
    private final RequeryMappingContext mappingContext;

    public RequeryTemplate(@Nonnull EntityDataStore<Object> dataStore,
                           @Nonnull RequeryMappingContext mappingContext) {
        Assert.notNull(dataStore, "dataStore must not be null");
        Assert.notNull(mappingContext, "mappingContext must not be null");

        this.dataStore = dataStore;
        this.mappingContext = mappingContext;
    }

    @Override
    public <V> V runInTransaction(@Nonnull final Callable<V> callable,
                                  @Nullable final TransactionIsolation isolation) {
        Assert.notNull(callable, "Callable must not be null.");

        if (dataStore.transaction().active()) {
            log.debug("Transaction is active already, so run statement in currently active transaction");
            try {
                V result = callable.call();
                dataStore.transaction().commit();
                return result;
            } catch (Exception e) {
                dataStore.transaction().rollback();
                throw new RequeryExecutionException("Fail to requery query.", e);
            }
        } else {
            return (isolation != null)
                   ? dataStore.runInTransaction(callable, isolation)
                   : dataStore.runInTransaction(callable, TransactionIsolation.READ_COMMITTED);
        }
    }

    @Override
    public <V> V withTransaction(@Nonnull final Function<EntityDataStore<Object>, V> block,
                                 @Nullable final TransactionIsolation isolation) {
        Assert.notNull(block, "block must not be null.");

        if (dataStore.transaction().active()) {
            log.debug("Transaction is active already, so run statement in currently active transaction");
            try {
                V result = block.apply(dataStore);
                dataStore.transaction().commit();
                return result;
            } catch (Exception e) {
                dataStore.transaction().rollback();
                throw new RequeryExecutionException("Fail to requery query.", e);
            }
        } else {
            return getDataStore().runInTransaction(() -> block.apply(dataStore), isolation);
        }
    }
}
