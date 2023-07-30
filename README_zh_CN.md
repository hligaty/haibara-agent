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

> **欢迎使用和Star支持。如果你在使用过程中遇到问题，你可以提出问题，我会尽力改进**

##  介绍

这是一个开源的JavaAgent，旨在简化在Java类中编写字段描述的过程。在典型的场景中，当编写数据库实体类时，我们需要向数据库列添加描述，并手动为其他类添加 Swagger Schema 注释和 Jakarta Validation 注释。这个过程可能很乏味。该 JavaAgent 的目标是减少这种重复性任务，使开发人员能够专注于编写数据库列注释，同时自动生成 Swagger Schema 和 Jakarta Validation 注释所需的描述。

## 主要特点

- 添加 Jakarta Validation Annotation 消息：通过根据字段的数据库列注释自动填充其消息，提高代码可维护性。
- 添加 Swagger Schema Annotation 描述：通过基于字段的数据库列注释自动填充其描述，提高代码的可维护性。

## 安装和使用

添加以下Maven依赖项：

```xml
<dependency>
    <groupId>io.github.hligaty</groupId>
    <artifactId>haibara-agent</artifactId>
    <version>0.0.1-beta.1</version>
</dependency>
```

在实体类中，像往常一样编写数据库列注释：

```java
/**
 * org.hibernate.annotations.Comment("应用表") or
 * io.swagger.v3.oas.annotations.media.Schema(description = "应用表")
 */
@Data
@Entity
@Table(name = "application")
@Comment("应用表")
public class Application {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("主键")
    private Long id;

    @Column(name = "type", columnDefinition = "tinyint unsigned comment '类型'")
    private Integer type;

    @Column(name = "name")
    @Comment("名字")
    private String name;

    /**
     * jakarta.persistence.Column(columnDefinition = "tinyint unsigned comment '状态'", ...)
     * or org.hibernate.annotations.Comment("状态")
     * or io.swagger.v3.oas.annotations.media.Schema(description = "状态")
     */
    @Schema(description = "状态")
    @Column(name = "status")
    @EnumProperty(value = Status.class)
    private Integer status;

}
```

```
public enum Status {
    CLOSE,
    OPEN,
}
```

将 `SchemaDematerializer` 注释添加到目标类，指定字段描述应从上面的 `Application` 类复制：

```java
@Data
@Schema(description = "应用请求Vo")
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
    private Integer type;

}
```

为启动命令添加下面的启动参数：

```java
-javaagent:haibara-agent-0.0.1-beta.1.jar
```

可选参数：
`haibara.agent.class_loader_source`：此参数用作代理访问扩展点和其他资源文件的类加载器的源。默认情况下，它是包含“main”方法的类的名称。或者，您可以使用系统属性`-Daibara.agent.class_loader_source`提供该值。如果显式参数和系统属性都不存在，则Spring boot应用程序将使用默认值`org.springframework.boot.SpringApplication`，无需设置此参数。

启动后，将生成如下描述和消息：

    @Data
    @Schema(description = "应用请求Vo")
    @SchemaDematerializer(Application.class)
    public class ApplicationRequestVo {
    
        @NotNull(message = "应用表主键")
        @Schema(description = "应用表主键不能为空")
        private Long id;
        
        @NotBlank(message = "名字")
        @Schema(description = "名字不能为空")
        private String name;
    
        @NotNull(message = "类型(0-CLOSE, 1-OPEN)") // 如果字段上存在 @EnumProperty, 将生成枚举描述信息
        @Schema(description = "类型(0-CLOSE, 1-OPEN)不能为空")
        private Integer type;
    
    }

## 扩展

HaibaraAgent通过使用SPI（服务提供商接口）提供了扩展其功能的灵活性。

-  [CommonTableFieldDescriptionProvider](src/main/java/io/github/hligaty/haibaraag/spi/CommonTableFieldDescriptionProvider.java):

`SchemaDematerializer` 不会从源类的父类复制字段。但是，公共字段通常是在父类中定义的。默认提供程序类[DefaultCommonTableFieldDescriptionProvider](src/main/java/io/github/hligaty/haibaraag/spi/DefaultCommonTableFieldDescriptionProvider.java) 提供“id”字段。

-   [TableDescriptionHandler](src/main/java/io/github/hligaty/haibaraag/spi/TableDescriptionHandler.java),   [EnumDescriptionFactory](src/main/java/io/github/hligaty/haibaraag/spi/EnumDescriptionFactory.java):

TableDescriptionHandler和EnumDescriptionFactory分别用于对获得的表描述进行后处理和生成枚举描述。默认的实现是[DefaultTableDescriptionHandler](src/main/java/io/github/hligaty/haibaraag/spi/DefaultTableDescriptationHandler.java)和[DefaultEnumDescriptionFactory](srg/main/java/io/github/hlegaty/haibaraag/spi/DfaultEnumDescriptationFactory.java)。

-  [ValidationAnnotationDefinitionProvider](src/main/java/io/github/hligaty/haibaraag/spi/ValidationAnnotationDefinitionProvider.java)：

用于注册自定义校验注解。有关支持的默认注释列表，请参阅[DefaultValidationAnnotationDefinitionProvider](src/main/java/io/github/hligaty/haibaraag/spi/DefaultValidationAnnotationDefinitionProvider.java)。
