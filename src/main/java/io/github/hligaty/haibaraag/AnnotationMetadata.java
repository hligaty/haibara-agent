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

package io.github.hligaty.haibaraag;

public interface AnnotationMetadata {

    String VALUE_NAME = "value";

    interface Swagger {

        interface Schema {

            String CLASS_NAME = "io.swagger.v3.oas.annotations.media.Schema";

            String DESCRIPTION_NAME = "description";

        }

    }

    interface JakartaPersistence {

        interface Column {

            String CLASS_NAME = "jakarta.persistence.Column";

            String COLUMN_DEFINITION_NAME = "columnDefinition";

        }

    }

    interface Hibernate {

        interface Comment {

            String CLASS_NAME = "org.hibernate.annotations.Comment";

        }

    }

    interface JakartaValidation {

        String MESSAGE_NAME = "message";

    }

}
