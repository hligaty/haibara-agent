/*
 * Copyright 2023 hligaty
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.hligaty.haibaraag.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * {@link io.swagger.v3.oas.annotations.media.Schema#description()} dematerialize.
 * When the description is empty, generate a description for it,
 * the source of the description is {@link io.swagger.v3.oas.annotations.media.Schema#description()},
 * {@link jakarta.persistence.Column#columnDefinition()} or {@link org.hibernate.annotations.Comment#value()}
 *
 * @author hligaty
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface SchemaDematerializer {

    /**
     * Source of description, prioritize in descending order.
     *
     * @return describe the source class of information.
     */
    Class<?>[] value() default {};

}
