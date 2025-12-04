package com.chess.genesis.processor;

import com.sun.source.tree.*;
import com.sun.source.util.Trees;
import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.tools.Diagnostic.Kind;
import java.io.*;
import java.util.*;

@SupportedSourceVersion(SourceVersion.RELEASE_11)
public class EnumsConstProcessor extends AbstractProcessor {
    private Filer filer;
    private Trees trees;

    @Override
    public void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        filer = processingEnv.getFiler();
        trees = Trees.instance(processingEnv);
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Set.of("com.chess.genesis.processor.EnumsConst", "androidx.room.ColumnInfo", "androidx.room.Query");
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        processingEnv.getMessager().printMessage(Kind.NOTE, "EnumsConstProcessor running");
        List<TypeElement> classes = new ArrayList<>();
        for (Element element : roundEnv.getElementsAnnotatedWith(EnumsConst.class)) {
            processingEnv.getMessager().printMessage(Kind.NOTE, "Found annotated element: " + element);
            if (element.getKind() == ElementKind.INTERFACE) {
                classes.add((TypeElement) element);
            }
        }
        if (!classes.isEmpty()) {
            processingEnv.getMessager().printMessage(Kind.NOTE, "Generating EnumsFixed");
            generateEnumsFixed(classes);
        } else {
            processingEnv.getMessager().printMessage(Kind.NOTE, "No annotated interfaces found");
        }
        return true;
    }

    private void generateEnumsFixed(List<TypeElement> annotatedElements) {
        // Collect constants and enums from all annotated interfaces
        Map<String, String> constants = new HashMap<>();
        Map<String, List<String[]>> enums = new HashMap<>();
        for (TypeElement interfaceElement : annotatedElements) {
            // Collect constants from the interface
            for (Element enclosed : interfaceElement.getEnclosedElements()) {
                if (enclosed.getKind() == ElementKind.FIELD) {
                    VariableElement field = (VariableElement) enclosed;
                    Object value = field.getConstantValue();
                    if (value != null) {
                        String name = field.getSimpleName().toString();
                        constants.put(name, value.toString());
                    }
                }
            }
            // Get the enclosed enum types
            for (Element enclosed : interfaceElement.getEnclosedElements()) {
                if (enclosed.getKind() == ElementKind.ENUM) {
                    TypeElement enumElement = (TypeElement) enclosed;
                    String enumName = enumElement.getSimpleName().toString();
                    List<String[]> items = new ArrayList<>();
                    // Collect enum constants with ids from tree
                    for (Element constant : enumElement.getEnclosedElements()) {
                        if (constant.getKind() == ElementKind.ENUM_CONSTANT) {
                            String itemName = constant.getSimpleName().toString();
                            String id = getEnumIdFromTree(constant);
                            if (id != null) {
                                items.add(new String[]{itemName, id});
                            }
                        }
                    }
                    enums.put(enumName, items);
                }
            }
        }

        processingEnv.getMessager().printMessage(Kind.NOTE, "Collected " + enums.size() + " enums");

        // Generate code
        StringBuilder code = new StringBuilder();
        code.append("package com.chess.genesis.data;\n\n");
        code.append("public interface EnumsFixed {\n");
        for (Map.Entry<String, List<String[]>> entry : enums.entrySet()) {
            String enumName = entry.getKey();
            for (String[] item : entry.getValue()) {
                String itemName = item[0];
                String id = item[1];
                code.append("    int ").append(enumName).append("_").append(itemName).append(" = ").append(id).append(";\n");
                code.append("    String ").append(enumName).append("_").append(itemName).append("_STR = \"").append(id).append("\";\n");
            }
        }
        code.append("}\n");

        try {
            var file = filer.createSourceFile("com.chess.genesis.data.EnumsFixed");
            try (var writer = file.openWriter()) {
                writer.write(code.toString());
            }
        } catch (IOException e) {
            processingEnv.getMessager().printMessage(Kind.ERROR, "Failed to generate EnumsFixed: " + e.getMessage());
        }
    }

    private String getEnumIdFromTree(Element constant) {
        Tree tree = trees.getTree(constant);
        if (tree instanceof VariableTree vt) {
            ExpressionTree init = vt.getInitializer();
            if (init instanceof NewClassTree nct) {
                List<? extends ExpressionTree> args = nct.getArguments();
                if (!args.isEmpty()) {
                    ExpressionTree firstArg = args.get(0);
                    if (firstArg instanceof LiteralTree lt) {
                        Object value = lt.getValue();
                        return value.toString();
                    }
                }
            }
        }
        return null;
    }
}
