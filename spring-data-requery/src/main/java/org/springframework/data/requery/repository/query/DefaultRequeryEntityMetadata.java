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

package org.springframework.data.requery.repository.query;

import io.requery.Entity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import javax.annotation.Nonnull;

/**
 * Default implementation for {@link RequeryEntityMetadata}.
 *
 * @author debop
 * @since 18. 6. 7
 */
@Slf4j
public class DefaultRequeryEntityMetadata<T> implements RequeryEntityMetadata<T> {

    @Nonnull
    public static <T> DefaultRequeryEntityMetadata<T> of(@Nonnull Class<T> domainClass) {
        Assert.notNull(domainClass, "domainClass must not be null");
        return new DefaultRequeryEntityMetadata<>(domainClass);
    }

    private final Class<T> domainClass;

    public DefaultRequeryEntityMetadata(@Nonnull Class<T> domainClass) {
        Assert.notNull(domainClass, "domainClass must not be null!");
        this.domainClass = domainClass;
    }

    @Nonnull
    @Override
    public String getEntityName() {
        Entity entity = AnnotatedElementUtils.findMergedAnnotation(domainClass, Entity.class);

        log.trace("Get entity name... domainClass={}, entity={}", domainClass.getName(), entity);

        return (entity != null) && StringUtils.hasText(entity.name())
               ? entity.name()
               : domainClass.getSimpleName();
    }

    @Override
    public String getModelName() {
        Entity entity = AnnotatedElementUtils.findMergedAnnotation(domainClass, Entity.class);

        log.trace("Get model name... domainClass={}, entity={}", domainClass.getName(), entity);

        if (entity != null) {
            return StringUtils.hasText(entity.model()) ? entity.model() : "default";
        }
        return "";
    }

    @Override
    public Class<T> getJavaType() {
        return domainClass;
    }
}

