package com.okada.compiler;


import com.google.auto.service.AutoService;
import com.okada.library.annotation.InjectView;

import java.io.IOException;
import java.io.Writer;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.JavaFileObject;

@AutoService(Processor.class)
public class AnnotationProcessor extends AbstractProcessor {

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Collections.singleton(InjectView.class.getCanonicalName());
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment env) {
        Map<TypeElement, Set<InjectionPoint>> injectionsByClass = new LinkedHashMap<>();

        for (Element element : env.getElementsAnnotatedWith(InjectView.class)) {
            TypeElement enclosingElement = (TypeElement) element.getEnclosingElement();

            Set<InjectionPoint> injections = injectionsByClass.get(enclosingElement);
            if (injections == null) {
                injections = new HashSet<>();
                injectionsByClass.put(enclosingElement, injections);
            }

            String variableName = element.getSimpleName().toString();
            String type = element.asType().toString();
            int value = element.getAnnotation(InjectView.class).value();
            injections.add(new InjectionPoint(variableName, type, value));
        }

        for (Map.Entry<TypeElement, Set<InjectionPoint>> injection : injectionsByClass.entrySet()) {
            TypeElement enclosingElement = injection.getKey();
            String targetClassFullName = enclosingElement.getQualifiedName().toString();
            int lastDot = targetClassFullName.lastIndexOf(".");
            String activityType = targetClassFullName.substring(lastDot + 1);
            String className = activityType + SUFFIX;
            String packageName = targetClassFullName.substring(0, lastDot);

            StringBuilder injections = new StringBuilder();
            for (InjectionPoint injectionPoint : injection.getValue()) {
                injections.append(injectionPoint).append("\n");
            }

            // Write the view injector class.
            try {
                JavaFileObject jfo =
                        processingEnv.getFiler().createSourceFile(packageName + "." + className, enclosingElement);
                Writer writer = jfo.openWriter();
                writer.write(
                        String.format(INJECTOR, packageName, className, activityType, injections.toString()));
                writer.flush();
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return true;
    }

    private static final String SUFFIX = "$$ViewInjector";
    private static final String INJECTOR = ""
            + "package %s;\n\n"
            + "public class %s {\n"
            + "  public static void inject(%s activity) {\n"
            + "%s"
            + "  }\n"
            + "}\n";
}