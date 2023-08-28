[English](README.md) | [中文](README_zh_CN.md)

# HaibaraAgent

<p align="center">
<a href="https://openjdk.java.net/"><img src="https://img.shields.io/badge/JDK-17+-green?logo=java&amp;logoColor=white"></a>
<a href="https://github.com/hligaty/haibara-agent/blob/main/LICENSE"><img src="https://img.shields.io/github/license/hligaty/haibara-agent"></a>
<a href="https://api.github.com/repos/hligaty/haibara-agent/releases/latest"><img src="https://img.shields.io/github/v/release/hligaty/haibara-agent"></a>
<a href="https://github.com/hligaty/haibara-agent/stargazers"><img src="https://img.shields.io/github/stars/hligaty/haibara-agent"></a>
<a href="https://github.com/hligaty/haibara-agent/network/members"><img src="https://img.shields.io/github/forks/hligaty/haibara-agent"></a>
<a href="https://github.com/hligaty/haibara-agent/issues?q=is%3Aissue+is%3Aclosed"><img src="https://img.shields.io/github/issues-closed-raw/hligaty/haibara-agent"></a>
</p>


> **Github：[hligaty/haibara-agent: Swagger Schema Dematerializer (github.com)](https://github.com/hligaty/haibara-agent)**

> **Welcome to use and Star support. If you encounter problems during use,  you can raise an Issue and I will try my best to improve it**

## Introduce

This is an open-source JavaAgent designed to simplify the process of writing field descriptions in Java classes. In typical scenarios, when working with database entity classes, we need to add descriptions to the database columns and manually include Swagger Schema annotations and Jakarta Validation annotations for other classes. This process can be tedious. The goal of this JavaAgent is to reduce this repetitive task, allowing developers to focus solely on writing database column comment, while automatically generating the required descriptions for Swagger Schema and Jakarta Validation annotations.

## Key Features

- Adding Jakarta Validation Annotation message: Improving code maintainability by automatically populating message for Jakarta Validation Annotations based on the Database Column comment  of the Fields.
- Adding Swagger Schema Annotation description : Improving code maintainability by automatically populating description for Swagger Schema Annotation based on the Database Column comment of the Fields.

## Installation and Usage

Add the following Maven dependency:

```xml
<dependency>
    <groupId>io.github.hligaty</groupId>
    <artifactId>haibara-agent</artifactId>
    <version>0.0.1-beta.1</version>
</dependency>
```

In the entity class, write the database column comment as usual：

```java
/**
 * org.hibernate.annotations.Comment("application") or
 * io.swagger.v3.oas.annotations.media.Schema(description = "application")
 */
@Data
@Entity
@Table(name = "application")
@Comment("application")
public class Application {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("id")
    private Long id;

    @Column(name = "type", columnDefinition = "tinyint unsigned comment 'type'")
    private Integer type;

    @Column(name = "name")
    @Comment("name")
    private String name;

    /**
     * jakarta.persistence.Column(columnDefinition = "tinyint unsigned comment 'status'", ...)
     * or org.hibernate.annotations.Comment("status")
     * or io.swagger.v3.oas.annotations.media.Schema(description = "status")
     */
    @Schema(description = "status")
    @EnumProperty(value = Status.class)
    @Column(name = "status")
    private Integer status;

}
```

```
public enum Status {
    CLOSE,
    OPEN,
}
```

Add the `SchemaDematerializer` annotation to the target class, specifying that the field descriptions should be copied from the above `Application` class:

```java
@Data
@Schema(description = "Application Request Body")
@SchemaDematerializer(Application.class)
public class ApplicationRequestVo {

    @NotNull
    @Schema
    private Long id;
    
    @NotBlank
    @Schema
    private String name;

    @NotNull
    @Schema
    private Integer status;

}
```

Add the following startup parameters to the startup command: 

```shell
-javaagent:haibara-agent-0.0.1-beta.1.jar
```

Optional Parameter:

`haibara.agent.class_loader_source`: This parameter serves as the source of the class loader for the agent to access extension points and other resource files. By default, it is the name of the class containing the `main` method. Alternatively, you can provide the value using the system property `-Dhaibara.agent.class_loader_source`. If both the explicit parameter and system property are absent, the default value `org.springframework.boot.SpringApplication` will be used for Spring Boot applications, and there is no need to set this parameter.

After startup, the description and message will be generated as follows:

    @Data
    @Schema(description = "Application Request Body")
    @SchemaDematerializer(Application.class)
    public class ApplicationRequestVo {
    
        @NotNull(message = "applicationid")
        @Schema(description = "applicationidmust not be null")
        private Long id;
        
        @NotBlank(message = "name")
        @Schema(description = "namemust not be blank")
        private String name;
    
        @NotNull(message = "status(0-CLOSE, 1-OPEN)") // If @EnumProperty exists on the field, enumeration description information will be generated
        @Schema(description = "status(0-CLOSE, 1-OPEN)must not be null")
        private Integer type;
    
    }

## Extension

HaibaraAgent offers the flexibility to extend its functionality through the use of SPI (Service Provider Interface).

-  [CommonTableFieldDescriptionProvider](src/main/java/io/github/hligaty/haibaraag/spi/CommonTableFieldDescriptionProvider.java):

The `SchemaDematerializer` does not copy fields from the parent class of the source class. However, common fields are usually defined in the parent class. The default provider, class  [DefaultCommonTableFieldDescriptionProvider](src/main/java/io/github/hligaty/haibaraag/spi/DefaultCommonTableFieldDescriptionProvider.java), offers the 'id' field.

-   [TableDescriptionHandler](src/main/java/io/github/hligaty/haibaraag/spi/TableDescriptionHandler.java),   [EnumDescriptionFactory](src/main/java/io/github/hligaty/haibaraag/spi/EnumDescriptionFactory.java):

TableDescriptionHandler and EnumDescriptionFactory are used for post-processing the obtained table descriptions and generating enum descriptions, respectively. The default implementations are  [DefaultTableDescriptionHandler](src/main/java/io/github/hligaty/haibaraag/spi/DefaultTableDescriptionHandler.java) and  [DefaultEnumDescriptionFactory](src/main/java/io/github/hligaty/haibaraag/spi/DefaultEnumDescriptionFactory.java).

-  [ValidationAnnotationDefinitionProvider](src/main/java/io/github/hligaty/haibaraag/spi/ValidationAnnotationDefinitionProvider.java)：

Used for registering custom validation annotations. For the list of supported default annotations, please refer to  [DefaultValidationAnnotationDefinitionProvider](src/main/java/io/github/hligaty/haibaraag/spi/DefaultValidationAnnotationDefinitionProvider.java).

## Roadmap

- Supports database entity class enumeration types instead of @ EnumProperty annotations
- Support annotation generation on method parameters
