package io.github.hligaty.haibaraag.annotations.spi;

import io.github.hligaty.haibaraag.annotations.SchemaDematerializer;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.io.Writer;
import java.util.HashSet;
import java.util.Set;

/**
 * Scan {@link SchemaDematerializer} to generate {@link SwaggerDematerializerScanProcessor#SWAGGER_DEMATERIALIZE_CLASS_RESOURCE_LOCATION}.
 *
 * @author hligaty
 */
public class SwaggerDematerializerScanProcessor extends AbstractProcessor {

    public static final String SWAGGER_DEMATERIALIZE_CLASS_RESOURCE_LOCATION = "META-INF/haibara/" + SchemaDematerializer.class.getName() + ".imports";

    private final Set<String> classNameSet = new HashSet<>();

    private Messager messager;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        messager = processingEnv.getMessager();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Set.of(SchemaDematerializer.class.getName());
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.RELEASE_17;
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Filer filer = processingEnv.getFiler();
        if (roundEnv.processingOver()) {
            try (
                    Writer resourceWriter = filer.createResource(StandardLocation.CLASS_OUTPUT, "",
                            SWAGGER_DEMATERIALIZE_CLASS_RESOURCE_LOCATION).openWriter()
            ) {
                resourceWriter.write(String.join("\n", classNameSet));
            } catch (IOException e) {
                messager.printMessage(Diagnostic.Kind.ERROR, "Failed to create classname resource file. reason: %s".formatted(e));
            }
            messager.printMessage(Diagnostic.Kind.NOTE, "Generated %s".formatted(SWAGGER_DEMATERIALIZE_CLASS_RESOURCE_LOCATION));
        } else {
            for (Element element : roundEnv.getElementsAnnotatedWith(SchemaDematerializer.class)) {
                if (element instanceof TypeElement typeElement) {
                    String className = getClassName(typeElement);
                    classNameSet.add(className);
                }
            }
        }
        return true;
    }

    /**
     * Get the package name and class name of the class.
     * The class name and internal class name are separated by "$" instead of ".".
     *
     * @param element class element
     * @return package name and class name
     */
    private String getClassName(TypeElement element) {
        if (element.getEnclosingElement() instanceof PackageElement) {
            return element.getQualifiedName().toString();
        }
        String className = element.getSimpleName().toString();
        while (true) {
            if (element.getEnclosingElement() instanceof TypeElement typeElement) {
                className = "%s$%s".formatted(typeElement.getSimpleName(), className);
                element = typeElement;
            }
            if (element.getEnclosingElement() instanceof PackageElement packageElement) {
                return "%s.%s".formatted(packageElement.getQualifiedName(), className);
            }
        }
    }

}
