package com.chess.genesis.processor;

import com.google.auto.service.*;
import javax.annotation.processing.*;
import javax.lang.model.*;
import javax.lang.model.element.*;
import javax.lang.model.type.*;
import javax.tools.Diagnostic.*;
import java.io.*;
import java.util.*;

@SupportedSourceVersion(SourceVersion.RELEASE_11)
@AutoService(Processor.class)
public class ZmqMessageProcessor extends AbstractProcessor
{
	private Filer filer;

	@Override
	public synchronized void init(ProcessingEnvironment processingEnv)
        {
		super.init(processingEnv);
		filer = processingEnv.getFiler();
	}

	@Override
	public Set<String> getSupportedAnnotationTypes()
        {
		return Set.of("com.chess.genesis.processor.ZmqMessage");
	}

	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv)
        {
		var classes = new ArrayList<TypeElement>();
		for (var element : roundEnv.getElementsAnnotatedWith(ZmqMessage.class)) {
			if (element.getKind() == ElementKind.CLASS) {
				classes.add((TypeElement) element);
			}
		}
		if (!classes.isEmpty()) {
			generateGeneratedClass(classes);
		}
		return true;
	}

	private void generateGeneratedClass(List<TypeElement> classes)
        {
		var code = new StringBuilder();
		code.append("package com.chess.genesis.net.msgs;\n");
		code.append("\n");
		code.append("import android.util.Pair;\n");
		code.append("import java.io.IOException;\n");
		code.append("import java.util.ArrayList;\n");
		code.append("import org.msgpack.core.*;\n");
		code.append("\n");
		code.append("public class ZmqMessageHelper {\n");

		for (var classElement : classes) {
			var className = classElement.getSimpleName().toString();
			var fields = getPublicFields(classElement);

			code.append("	private static void parse").append(className).append("(").append(className).append(" msg, MessageUnpacker packer) throws IOException {\n");
			for (var field : fields) {
				var fieldName = field.getSimpleName().toString();
				var type = field.asType();
				var unpack = getUnpack(type, fieldName);
				code.append("		msg.").append(unpack).append("\n");
			}
			code.append("	}\n");
			code.append("\n");

			code.append("	private static void toBytes").append(className).append("(").append(className).append(" msg, MessageBufferPacker packer) throws IOException {\n");
			for (var field : fields) {
				var fieldName = field.getSimpleName().toString();
				var type = field.asType();
				var pack = getPack(type, fieldName);
				code.append("		").append(pack).append("\n");
			}
			code.append("	}\n");
			code.append("\n");
		}

		code.append("	public static ZmqMsg parse(byte[] data) throws IOException {\n");
		code.append("		var packer = MessagePack.newDefaultUnpacker(data);\n");
		code.append("		var type = packer.unpackInt();\n");
		code.append("		var msg = switch (type) {\n");
		for (var classElement : classes) {
			var className = classElement.getSimpleName().toString();
			code.append("			case ").append(className).append(".ID -> new ").append(className).append("();\n");
		}
		code.append("			default -> new UnknownMsg(data);\n");
		code.append("		};\n");
		code.append("		parse(msg, packer);\n");
		code.append("		return msg;\n");
		code.append("	}\n");
		code.append("\n");

		code.append("	private static void parse(ZmqMsg msg, MessageUnpacker packer) throws IOException {\n");
		code.append("		switch (msg.type()) {\n");
		for (var classElement : classes) {
			String className = classElement.getSimpleName().toString();
			code.append("			case ").append(className).append(".ID -> parse").append(className).append("((").append(className).append(") msg, packer);\n");
		}
		code.append("		}\n");
		code.append("	}\n");
		code.append("\n");

		code.append("	public static byte[] toBytes(ZmqMsg msg) throws IOException {\n");
		code.append("		var packer = MessagePack.newDefaultBufferPacker();\n");
		code.append("		packer.packInt(msg.type());\n");
		code.append("		toBytes(msg, packer);\n");
		code.append("		return packer.toByteArray();\n");
		code.append("	}\n");
		code.append("\n");

		code.append("	private static void toBytes(ZmqMsg msg, MessageBufferPacker packer) throws IOException {\n");
		code.append("		switch (msg.type()) {\n");
		for (var classElement : classes) {
			String className = classElement.getSimpleName().toString();
			code.append("			case ").append(className).append(".ID -> toBytes").append(className).append("((").append(className).append(") msg, packer);\n");
		}
		code.append("		}\n");
		code.append("	}\n");
		code.append("}\n");

		try {
			var file = filer.createSourceFile("com.chess.genesis.net.msgs.ZmqMessageHelper");
			try (var writer = file.openWriter()) {
				writer.write(code.toString());
			}
		} catch (IOException e) {
			processingEnv.getMessager().printMessage(Kind.ERROR, "Failed to generate ZmqMessageHelper: " + e.getMessage());
		}
	}

	private List<VariableElement> getPublicFields(TypeElement classElement)
        {
		var fields = new ArrayList<VariableElement>();
		for (var enclosed : classElement.getEnclosedElements()) {
			var modifiers = enclosed.getModifiers();
			if (enclosed.getKind() == ElementKind.FIELD && modifiers.contains(Modifier.PUBLIC) && !modifiers.contains(Modifier.STATIC)) {
				fields.add((VariableElement) enclosed);
			}
		}
		return fields;
	}

	private String getUnpack(TypeMirror type, String fieldName)
        {
		var typeStr = type.toString();
		return switch (typeStr) {
			case "boolean" -> fieldName + " = packer.unpackBoolean();";
			case "int" -> fieldName + " = packer.unpackInt();";
			case "long" -> fieldName + " = packer.unpackLong();";
			case "java.lang.String" -> fieldName + " = packer.unpackString();";
			case "java.util.List<android.util.Pair<java.lang.String,java.lang.Long>>" ->
                                fieldName + " = new ArrayList<>();\n"
			        + "		var size = packer.unpackArrayHeader();\n"
                                + "		for (int i = 0; i < size; i++) {\n"
                                + "			msg." + fieldName + ".add(new Pair<>(packer.unpackString(), packer.unpackLong()));\n"
                                + "		}";
			default -> throw new IllegalArgumentException("Unsupported type: " + typeStr);
		};
	}

	private String getPack(TypeMirror type, String fieldName)
        {
		var typeStr = type.toString();
		return switch (typeStr) {
			case "boolean" -> "packer.packBoolean(msg." + fieldName + ");";
			case "int" -> "packer.packInt(msg." + fieldName + ");";
			case "long" -> "packer.packLong(msg." + fieldName + ");";
			case "java.lang.String" -> "packer.packString(msg." + fieldName + ");";
			case "java.util.List<android.util.Pair<java.lang.String,java.lang.Long>>" ->
				"packer.packArrayHeader(msg." + fieldName + ".size());\n"
				+ "		for (var pair : msg." + fieldName + ") {\n"
				+ "			packer.packString(pair.first);\n"
				+ "			packer.packLong(pair.second);\n"
				+ "		}";
			default -> throw new IllegalArgumentException("Unsupported type: " + typeStr);
		};
	}
}
