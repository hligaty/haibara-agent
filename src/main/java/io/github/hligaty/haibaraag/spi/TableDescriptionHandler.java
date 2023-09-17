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

package io.github.hligaty.haibaraag.spi;

/**
 * Table description handler.
 * TableName from {@link io.swagger.v3.oas.annotations.media.Schema#description()} or
 * {@link org.hibernate.annotations.Comment#value()}
 *
 * @author hligaty
 * @see DefaultTableDescriptionHandler
 */
public interface TableDescriptionHandler extends Order {

    /**
     * Convert table description to displayed entity name
     *
     * @param tableDescription table description
     * @return processed entity name
     */
    String get(String tableDescription);

}
