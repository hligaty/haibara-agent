package io.github.hligaty.haibaraag;

public interface Annotations {

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
