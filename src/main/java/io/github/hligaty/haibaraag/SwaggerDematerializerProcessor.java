package io.github.hligaty.haibaraag;

import io.github.hligaty.haibaraag.annotations.EnumProperty;
import io.github.hligaty.haibaraag.annotations.SchemaDematerializer;
import io.github.hligaty.haibaraag.spi.CommonTableFieldDefinition;
import io.github.hligaty.haibaraag.spi.CommonTableFieldDescriptionProvider;
import io.github.hligaty.haibaraag.spi.DefaultEnumDescriptionFactory;
import io.github.hligaty.haibaraag.spi.DefaultTableDescriptionHandler;
import io.github.hligaty.haibaraag.spi.EnumDescriptionFactory;
import io.github.hligaty.haibaraag.spi.Order;
import io.github.hligaty.haibaraag.spi.TableDescriptionHandler;
import io.github.hligaty.haibaraag.spi.ValidationAnnotationDefinitionProvider;
import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.NotFoundException;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.ClassFile;
import javassist.bytecode.ConstPool;
import javassist.bytecode.annotation.Annotation;
import javassist.bytecode.annotation.ArrayMemberValue;
import javassist.bytecode.annotation.ClassMemberValue;
import javassist.bytecode.annotation.MemberValue;
import javassist.bytecode.annotation.StringMemberValue;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static io.github.hligaty.haibaraag.annotations.spi.SwaggerDematerializerScanProcessor.SWAGGER_DEMATERIALIZE_CLASS_RESOURCE_LOCATION;

public class SwaggerDematerializerProcessor {

    private final Set<String> classNameSet = new HashSet<>();

    private TableDescriptionHandler tableDescriptionHandler;

    private Map<CommonTableFieldDefinition, String> commonFieldDefinitionMap;

    private EnumDescriptionFactory enumDescriptionFactory;

    private Map<String, String> validationAnnotationDefinitionMap = new HashMap<>();

    /**
     * Initialize the swagger dematerializer processor using the given class loader.
     * Including loading SPI extension points and load configuration files generated during the compilation process.
     *
     * @param loader The class loader to be used to load provider-configuration files and provider classes
     * @throws IOException if an I/O error occurs reading to the file
     */
    void init(ClassLoader loader) throws IOException {
        URL resource = loader.getResource(SWAGGER_DEMATERIALIZE_CLASS_RESOURCE_LOCATION);
        if (resource != null) {
            try (InputStream inputStream = resource.openStream()) {
                classNameSet.addAll(Set.of(new String(inputStream.readAllBytes()).split("\n")));
            }
        }
        tableDescriptionHandler = ServiceLoader.load(TableDescriptionHandler.class, loader).stream()
                .map(ServiceLoader.Provider::get)
                .min(Comparator.comparingInt(Order::value))
                .orElse(new DefaultTableDescriptionHandler());
        commonFieldDefinitionMap = ServiceLoader.load(CommonTableFieldDescriptionProvider.class, loader).stream()
                .map(ServiceLoader.Provider::get)
                .sorted(Comparator.comparingInt(Order::value))
                .flatMap(commonTableFieldDescriptionProvider -> commonTableFieldDescriptionProvider.get().entrySet().stream())
                .collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, Map.Entry::getValue, (oldValue, newValue) -> oldValue));
        enumDescriptionFactory = ServiceLoader.load(EnumDescriptionFactory.class, loader).stream()
                .map(ServiceLoader.Provider::get)
                .min(Comparator.comparingInt(Order::value))
                .orElse(new DefaultEnumDescriptionFactory());
        validationAnnotationDefinitionMap = ServiceLoader.load(ValidationAnnotationDefinitionProvider.class, loader).stream()
                .map(ServiceLoader.Provider::get)
                .sorted(Comparator.comparingInt(Order::value))
                .flatMap(validationAnnotationDefinitionProvider -> validationAnnotationDefinitionProvider.get().entrySet().stream())
                .collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, Map.Entry::getValue, (oldValue, newValue) -> oldValue));
    }

    /**
     * Returns true if and only if the className need retransform.
     *
     * @param className class name
     * @return true if the class name need retransform, false otherwise
     */
    boolean containsClassName(String className) {
        return classNameSet.contains(className);
    }

    /**
     * Retransform the specified class to generate description for its fields.
     *
     * @param ctClass the class to retransform
     * @return a byte array containing the bytes read from the converted class if the class name need retransform, {@code null} otherwise
     * @throws IOException if an I/O error occurs transforming class byte code
     */
    byte[] process(CtClass ctClass) throws ClassNotFoundException, CannotCompileException, NotFoundException, IOException {
        List<CtClass> tableCtClasses = new ArrayList<>();
        try {
            List<String> tableClassNameList = Optional.ofNullable((AnnotationsAttribute) ctClass.getClassFile().getAttribute(AnnotationsAttribute.visibleTag))
                    .map(annotationsAttribute -> annotationsAttribute.getAnnotation(SchemaDematerializer.class.getName()))
                    .map(annotation -> (ArrayMemberValue) annotation.getMemberValue(Annotations.VALUE_NAME))
                    .map(ArrayMemberValue::getValue).stream()
                    .flatMap(Arrays::stream)
                    .map(memberValue -> ((ClassMemberValue) memberValue).getValue())
                    .toList(); // Source of obtaining description
            for (String tableClassName : tableClassNameList) {
                tableCtClasses.add(ClassPool.getDefault().get(tableClassName));
            }
            AtomicBoolean flag = new AtomicBoolean();
            for (CtField ctField : ctClass.getDeclaredFields()) {
                fillFieldDescription(ctClass, ctField, tableCtClasses, flag);
            }
            return flag.get() ? ctClass.toBytecode() : null;
        } finally {
            ctClass.detach();
            tableCtClasses.forEach(CtClass::detach);
        }
    }

    private void fillFieldDescription(CtClass ctClass, CtField ctField, List<CtClass> tableCtClasses, AtomicBoolean flag)
            throws ClassNotFoundException, NotFoundException {
        Optional<MemberValue> memberValue = Optional.ofNullable(ctField.getFieldInfo())
                .map(fieldInfo -> (AnnotationsAttribute) fieldInfo.getAttribute(AnnotationsAttribute.visibleTag))
                .map(annotationsAttribute -> annotationsAttribute.getAnnotation(Annotations.Swagger.Schema.CLASS_NAME))
                .map(annotation -> annotation.getMemberValue(Annotations.Swagger.Schema.DESCRIPTION_NAME));
        if (memberValue.isPresent()) { // Swagger Schema has description information
            retransformValidationAnnotation(ctClass, ctField, ((StringMemberValue) memberValue.get()).getValue());
            return;
        }
        String description = null;
        String signature = ctField.getSignature();
        int i = 0;
        char c = signature.charAt(i);
        while (c == '[') {
            c = signature.charAt(++i);
        }
        if ('L' != signature.charAt(i)) { // Not a reference type
            return;
        }
        String targetFieldTypeName = signature.substring(++i, signature.indexOf(';')).replace('/', '.');
        if (classNameSet.contains(targetFieldTypeName)) {
            // Generate description information for its validation annotations
            CtClass targetFieldCtClass = ClassPool.getDefault().get(targetFieldTypeName);
            try {
                String targetFieldClassDescription = getClassDescription(targetFieldCtClass);
                retransformValidationAnnotation(ctClass, ctField, targetFieldClassDescription);
            } finally {
                targetFieldCtClass.detach();
            }
            return;
        }
        if (!targetFieldTypeName.startsWith("java")) { // The type must be from a Java package
            return;
        }
        String targetFieldName = ctField.getName();
        for (CtClass tableCtClass : tableCtClasses) {
            try {
                // Match the fields of the table class to obtain a description
                CtField tableField = tableCtClass.getDeclaredField(targetFieldName, signature);
                description = getFieldDescription(tableField);
            } catch (NotFoundException ignored) {
            }
            if (description == null || description.isEmpty()) { // The field of the tableClass does not have a description
                // Match common table fields to obtain a description
                String tableDescription = getClassDescription(tableCtClass);
                String tableName = tableCtClass.getSimpleName();
                String fieldNameTablePrefix = tableName.substring(0, 1).toLowerCase() + tableName.substring(1);
                description = commonFieldDefinitionMap.get(new CommonTableFieldDefinition(targetFieldTypeName, targetFieldName));
                if (description == null // Common field description does not exist
                    && targetFieldName.startsWith(fieldNameTablePrefix) // Field name starts with the table class name, matches again
                ) {
                    String suffix = targetFieldName.substring(fieldNameTablePrefix.length());
                    targetFieldName = suffix.substring(0, 1).toLowerCase() + suffix.substring(1);
                    description = commonFieldDefinitionMap.get(new CommonTableFieldDefinition(targetFieldTypeName, targetFieldName));
                }
                if (description != null) {
                    description = tableDescription + description; // Attach this table description to this common field
                }
            }
            if (description != null && !description.isEmpty()) {
                retransformSwaggerSchema(ctClass, ctField, description);
                retransformValidationAnnotation(ctClass, ctField, description);
                flag.set(true);
                return;
            }
        }
    }

    private String getFieldDescription(CtField ctField) throws ClassNotFoundException {
        AnnotationsAttribute attribute = (AnnotationsAttribute) ctField.getFieldInfo().getAttribute(AnnotationsAttribute.visibleTag);
        String result = Optional.ofNullable(attribute.getAnnotation(Annotations.Swagger.Schema.CLASS_NAME))
                .map(annotation -> annotation.getMemberValue(Annotations.Swagger.Schema.DESCRIPTION_NAME))
                .map(memberValue -> ((StringMemberValue) memberValue).getValue())
                .orElse(null); // Obtain description from io.swagger.v3.oas.annotations.media.Schema.description()
        if (result == null) {
            result = Optional.ofNullable(attribute.getAnnotation(Annotations.JakartaPersistence.Column.CLASS_NAME))
                    .map(annotation -> annotation.getMemberValue(Annotations.JakartaPersistence.Column.COLUMN_DEFINITION_NAME))
                    .map(memberValue -> ((StringMemberValue) memberValue).getValue())
                    .map(this::getJakartaPersistenceColumnDefinitionComment)
                    .orElse(null); // Obtain description from jakarta.persistence.Column.columnDefinition()
        }
        if (result == null) {
            result = Optional.ofNullable(attribute.getAnnotation(Annotations.Hibernate.Comment.CLASS_NAME))
                    .map(annotation -> annotation.getMemberValue(Annotations.VALUE_NAME))
                    .map(memberValue -> ((StringMemberValue) memberValue).getValue())
                    .orElse(null); // Obtain description from org.hibernate.annotations.Comment.value()
        }
        String enumDescription = Optional.ofNullable(ctField.getAnnotation(EnumProperty.class))
                .map(annotation -> ((EnumProperty) annotation).value())
                .map(enumClass -> enumDescriptionFactory.get(enumClass))
                .orElse(""); // If EnumProperty exists, obtain description
        return Optional.ofNullable(result)
                .map(r -> r + enumDescription)
                .orElse(null);
    }

    private String getJakartaPersistenceColumnDefinitionComment(String columnDefinition) {
        // Obtain "c&a" from "varchar(255) comment 'c&a'"
        String COMMENT = "comment";
        if (columnDefinition == null || columnDefinition.isEmpty() || !columnDefinition.contains(COMMENT)) {
            return null;
        }
        String APOSTROPHE = "'";
        String comment = columnDefinition.substring(columnDefinition.indexOf(COMMENT) + COMMENT.length());
        int firstApostropheIndex = comment.indexOf(APOSTROPHE);
        int secondApostropheIndex = comment.indexOf(APOSTROPHE, firstApostropheIndex + APOSTROPHE.length());
        return comment.substring(firstApostropheIndex + APOSTROPHE.length(), secondApostropheIndex);
    }

    private String getClassDescription(CtClass ctClass) {
        ClassFile classFile = ctClass.getClassFile();
        AnnotationsAttribute attribute = (AnnotationsAttribute) classFile.getAttribute(AnnotationsAttribute.visibleTag);
        String result = Optional.ofNullable(attribute.getAnnotation(Annotations.Swagger.Schema.CLASS_NAME))
                .map(annotation -> annotation.getMemberValue(Annotations.Swagger.Schema.DESCRIPTION_NAME))
                .map(memberValue -> ((StringMemberValue) memberValue).getValue())
                .orElse(null); // Obtain table name from io.swagger.v3.oas.annotations.media.Schema.description()
        if (result == null) {
            Annotation annotation = attribute.getAnnotation(Annotations.Hibernate.Comment.CLASS_NAME);
            MemberValue memberValue = annotation.getMemberValue(Annotations.VALUE_NAME);
            result = Optional.ofNullable(memberValue)
                    .map(m -> ((StringMemberValue) m).getValue())
                    .map(v -> tableDescriptionHandler.get(v))
                    .orElse(null); // Obtain description from org.hibernate.annotations.Comment.value()
        }
        return result;
    }

    private void retransformSwaggerSchema(CtClass ctClass, CtField ctField, String description) {
        ClassFile classFile = ctClass.getClassFile();
        ConstPool constPool = classFile.getConstPool();

        AnnotationsAttribute attribute = (AnnotationsAttribute) ctField.getFieldInfo().getAttribute(AnnotationsAttribute.visibleTag);
        Annotation annotation = attribute.getAnnotation(Annotations.Swagger.Schema.CLASS_NAME);
        annotation.addMemberValue(Annotations.Swagger.Schema.DESCRIPTION_NAME, new StringMemberValue(description, constPool));
        attribute.addAnnotation(annotation);
    }

    private void retransformValidationAnnotation(CtClass ctClass, CtField ctField, String description) {
        ClassFile classFile = ctClass.getClassFile();
        ConstPool constPool = classFile.getConstPool();

        AnnotationsAttribute attribute = (AnnotationsAttribute) ctField.getFieldInfo().getAttribute(AnnotationsAttribute.visibleTag);
        for (Annotation annotation : attribute.getAnnotations()) {
            String message = validationAnnotationDefinitionMap.get(annotation.getTypeName());
            if (message != null
                && annotation.getMemberValue(Annotations.JakartaValidation.MESSAGE_NAME) == null) {
                annotation.addMemberValue(
                        Annotations.JakartaValidation.MESSAGE_NAME,
                        new StringMemberValue(description + message, constPool)
                );
                attribute.addAnnotation(annotation);
            }
        }
    }

}
