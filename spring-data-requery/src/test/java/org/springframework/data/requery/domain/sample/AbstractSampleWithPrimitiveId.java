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

package org.springframework.data.requery.domain.sample;

import io.requery.Entity;
import io.requery.Key;
import io.requery.Transient;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.requery.domain.AbstractValueObject;
import org.springframework.data.requery.domain.ToStringBuilder;

import java.io.Serializable;

/**
 * AbstractSampleWithPrimitiveId
 *
 * @author debop
 * @since 18. 6. 25
 */
@Getter
@Setter
@Entity
public abstract class AbstractSampleWithPrimitiveId extends AbstractValueObject implements Serializable {

    @Key
    protected long id;

    protected String name;

    @Transient
    @Override
    protected ToStringBuilder buildStringHelper() {
        return super.buildStringHelper()
            .add("id", id)
            .add("name", name);
    }

    private static final long serialVersionUID = -1154174706827967903L;
}
