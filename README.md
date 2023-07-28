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

##  介绍

HaibaraAgent 是一个 JavaAgent，用于减少书写重复的 Swagger 和校验注解的字段说明。很多时候我们在实体类中为字段添加了数据库的列说明，然后又在各处重复书写 Swagger Schema 的 description，以及 Jakarta Validation 的各种 NotNull、NotBlank 等等注解的 message，但这些都是繁琐却必要的，HaibaraAgent 可以减少这些操作。

## 用法

启动命令：-javaagent:haibara-agent-0.0.1-beta.1.jar=haibara.agent.class_loader_source=com.example.Main，其中 haibara.agent.class_loader_source 请填写为 main 方法所在类（它作为 agent 获取扩展点以及其他资源文件的类加载器来源），也可以使用 -Dhaibara.agent.class_loader_source 参数作为输入，当前面两个参数依次无法找到时将使用默认参数 org.springframework.boot.SpringApplication。程序是 SpringBoot 应用时无需填写该参数。

### 快速入门

在实体类中按照往常书写描述（使用 io.swagger.v3.oas.annotations.media.Schema 也可以）：

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

目标类增加 SchemaDematerializer 注解，并指定字段描述拷贝来源为上面的 Application 类。

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

运行时目标类将转换为如下：

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

### 进阶

- 公共字段：

通常表会有一些公共字段，比如名字为 id 类型为 java.lang.Long 的主键，在 Swagger 上要显示的描述应为表名加字段描述，即应用主键如果你有公共字段需要配置，那么请实现 io.github.hligaty.haibaraag.spi.CommonTableFieldDescriptionProvider 接口并为其注册 SPI（还要实现 value 方法修改优先级），默认的实现是 io.github.hligaty.haibaraag.spi.DefaultCommonTableFieldDescriptionProvider 。

- 表名：

io.github.hligaty.haibaraag.spi.TableDescriptionHandler 用于处理表描述，比如去掉“应用表”的“表”，默认不做处理

- 枚举：

io.github.hligaty.haibaraag.spi.EnumDescriptionFactory 用于生成枚举描述，默认为 io.github.hligaty.haibaraag.spi.DefaultEnumDescriptionFactory，格式为(0-枚举一name, 1-枚举二name, ...)
