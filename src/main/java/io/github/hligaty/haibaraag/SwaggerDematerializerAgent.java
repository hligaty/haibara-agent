package io.github.hligaty.haibaraag;

import javassist.ClassPool;
import javassist.CtClass;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * The main entrance of haibara agent, based on javaagent mechanism.
 *
 * @author hligaty
 */
public class SwaggerDematerializerAgent {

    private static final Logger LOG = Logger.getLogger(SwaggerDematerializerAgent.class.getName());

    private static final String CLASS_LOADER_SOURCE_PARAM_NAME = "haibara.agent.class_loader_source";

    private static String classLoaderSourceClass;

    private static SwaggerDematerializerProcessor swaggerDematerializerProcessor;

    public static void premain(String agentArgs, Instrumentation inst) {
        classLoaderSourceClass = getParam(agentArgs, CLASS_LOADER_SOURCE_PARAM_NAME)
                .orElse("org.springframework.boot.SpringApplication");
        swaggerDematerializerProcessor = new SwaggerDematerializerProcessor();
        inst.addTransformer(new SwaggerDematerializeClassTransformer());
    }

    private static Optional<String> getParam(String agentArgs, String name) {
        return Optional.ofNullable(agentArgs)
                .filter(args -> args.contains(name))
                .map(args -> {
                    int beginIndex = args.indexOf(name) + name.length() + "=".length();
                    int endIndex = args.indexOf(',', beginIndex);
                    return args.substring(beginIndex, endIndex != -1 ? endIndex : args.length());
                })
                .or(() -> Optional.ofNullable(System.getProperty(name)));
    }

    static class SwaggerDematerializeClassTransformer implements ClassFileTransformer {
        @Override
        public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
                                ProtectionDomain protectionDomain, byte[] classfileBuffer) {
            String classname = className.replace("/", ".");
            if (classLoaderSourceClass.equals(classname)) {
                try {
                    swaggerDematerializerProcessor.init(loader);
                } catch (IOException e) {
                    LOG.severe("Failed to load classname resource file, reason: %s".formatted(e));
                }
            } else if (swaggerDematerializerProcessor.containsClassName(classname)) {
                try {
                    CtClass ctClass = ClassPool.getDefault().makeClass(new ByteArrayInputStream(classfileBuffer));
                    return swaggerDematerializerProcessor.process(ctClass);
                } catch (Exception e) {
                    LOG.severe("Failed to retransform %s, reason: %s".formatted(classname, e));
                }
            }
            return null;
        }
    }

}
