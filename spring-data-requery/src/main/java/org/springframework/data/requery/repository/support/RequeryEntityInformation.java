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

package org.springframework.data.requery.repository.support;

import io.requery.meta.Attribute;
import org.springframework.data.repository.core.EntityInformation;
import org.springframework.data.requery.repository.query.RequeryEntityMetadata;

import javax.annotation.Nullable;

/**
 * Requery용 Entity의 정보를 제공합니다.
 *
 * @author debop
 * @since 18. 6. 7
 */
public interface RequeryEntityInformation<T, ID> extends EntityInformation<T, ID>, RequeryEntityMetadata<T> {

    /**
     * Returns the id attriute of the entity
     */
    @Nullable
    Attribute<? super T, ?> getIdAttribute();


    default Attribute<? super T, ?> getRequiredIdAttribute() {
        Attribute<? super T, ?> id = getIdAttribute();
        if (id != null) {
            return id;
        }
        throw new IllegalArgumentException(
            String.format("Could not obtain required identifier attribute for type %s!", getEntityName()));
    }

    boolean hasCompositeId();


    Iterable<String> getIdAttributeNames();

    @Nullable
    Object getCompositeIdAttributeValue(final Object id, final String idAttribute);

}