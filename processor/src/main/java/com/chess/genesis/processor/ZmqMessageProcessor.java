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
		code.append("import java.util.HashMap;\n");
		code.append("import java.util.Map;\n");
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
			case "byte" -> fieldName + " = packer.unpackByte();";
			case "int" -> fieldName + " = packer.unpackInt();";
			case "long" -> fieldName + " = packer.unpackLong();";
			case "java.lang.String" -> fieldName + " = packer.unpackString();";
			case "java.util.List<java.lang.String>" -> unpackStringList(fieldName);
			case "java.util.List<android.util.Pair<java.lang.String,java.lang.Long>>" -> unpackMoveList(fieldName);
			case "java.util.Map<com.chess.genesis.net.GameKey,com.chess.genesis.net.RatingValue>" -> unpackRatingsMap(fieldName);
			case "android.util.Pair<java.lang.Double,java.lang.Double>" -> unpackDoublePair(fieldName);
			case "java.util.List<com.chess.genesis.api.WaitingData>" -> unpackWaitingDataList(fieldName);
			default -> throw new IllegalArgumentException("Unsupported type: " + typeStr);
		};
	}

	private String unpackStringList(String fieldName)
	{
		return fieldName + " = new ArrayList<>();\n"
			+ "		var size = packer.unpackArrayHeader();\n"
			+ "		for (int i = 0; i < size; i++) {\n"
			+ "			msg." + fieldName + ".add(packer.unpackString());\n"
			+ "		}";
	}

	private String unpackMoveList(String fieldName)
	{
		return fieldName + " = new ArrayList<>();\n"
			+ "		var size = packer.unpackArrayHeader();\n"
			+ "		for (int i = 0; i < size; i++) {\n"
			+ "			msg." + fieldName + ".add(new Pair<>(packer.unpackString(), packer.unpackLong()));\n"
			+ "		}";
	}

	private String unpackRatingsMap(String fieldName)
	{
		return fieldName + " = new HashMap<>();\n"
			+ "		var size = packer.unpackMapHeader();\n"
			+ "		for (int i = 0; i < size; i++) {\n"
			+ "			var gameType = packer.unpackInt();\n"
			+ "			var baseTime = packer.unpackInt();\n"
			+ "			var incTime = packer.unpackInt();\n"
			+ "			var key = new com.chess.genesis.net.GameKey(gameType, baseTime, incTime);\n"
			+ "			var rating = packer.unpackDouble();\n"
			+ "			var deviation = packer.unpackDouble();\n"
			+ "			var volatility = packer.unpackDouble();\n"
			+ "			var value = new com.chess.genesis.net.RatingValue(rating, deviation, volatility);\n"
			+ "			msg." + fieldName + ".put(key, value);\n"
			+ "		}";
	}

	private String unpackDoublePair(String fieldName)
	{
		return fieldName + " = new Pair<>(packer.unpackDouble(), packer.unpackDouble());";
	}

	private String unpackWaitingDataList(String fieldName)
	{
		return fieldName + " = new ArrayList<>();\n"
			+ "		var size = packer.unpackArrayHeader();\n"
			+ "		for (int i = 0; i < size; i++) {\n"
			+ "			var gameType = packer.unpackInt();\n"
			+ "			var playAs = packer.unpackInt();\n"
			+ "			var baseTime = packer.unpackInt();\n"
			+ "			var incTime = packer.unpackInt();\n"
			+ "			msg." + fieldName + ".add(new com.chess.genesis.api.WaitingData(gameType, playAs, baseTime, incTime));\n"
			+ "		}";
	}

	private String getPack(TypeMirror type, String fieldName)
        {
		var typeStr = type.toString();
		return switch (typeStr) {
			case "boolean" -> "packer.packBoolean(msg." + fieldName + ");";
			case "byte" -> "packer.packByte(msg." + fieldName + ");";
			case "int" -> "packer.packInt(msg." + fieldName + ");";
			case "long" -> "packer.packLong(msg." + fieldName + ");";
			case "java.lang.String" -> "packer.packString(msg." + fieldName + ");";
			case "java.util.List<java.lang.String>" -> packStringList(fieldName);
			case "java.util.List<android.util.Pair<java.lang.String,java.lang.Long>>" -> packMoveList(fieldName);
			case "java.util.Map<com.chess.genesis.net.GameKey,com.chess.genesis.net.RatingValue>" -> packRatingsMap(fieldName);
			case "android.util.Pair<java.lang.Double,java.lang.Double>" -> packDoublePair(fieldName);
			case "java.util.List<com.chess.genesis.api.WaitingData>" -> packWaitingDataList(fieldName);
			default -> throw new IllegalArgumentException("Unsupported type: " + typeStr);
		};
	}

	private String packStringList(String fieldName)
	{
		return "packer.packArrayHeader(msg." + fieldName + ".size());\n"
			+ "		for (var item : msg." + fieldName + ") {\n"
			+ "			packer.packString(item);\n"
			+ "		}";
	}

	private String packMoveList(String fieldName)
	{
		return "packer.packArrayHeader(msg." + fieldName + ".size());\n"
			+ "		for (var pair : msg." + fieldName + ") {\n"
			+ "			packer.packString(pair.first);\n"
			+ "			packer.packLong(pair.second);\n"
			+ "		}";
	}

	private String packRatingsMap(String fieldName)
	{
		return "packer.packMapHeader(msg." + fieldName + ".size());\n"
			+ "		for (var entry : msg." + fieldName + ".entrySet()) {\n"
			+ "			var key = entry.getKey();\n"
			+ "			var value = entry.getValue();\n"
			+ "			packer.packInt(key.gameType());\n"
			+ "			packer.packInt(key.baseTime());\n"
			+ "			packer.packInt(key.incTime());\n"
			+ "			packer.packDouble(value.rating());\n"
			+ "			packer.packDouble(value.deviation());\n"
			+ "			packer.packDouble(value.volatility());\n"
			+ "		}";
	}

	private String packDoublePair(String fieldName)
	{
		return "packer.packDouble(msg." + fieldName + ".first);\n"
			+ "		packer.packDouble(msg." + fieldName + ".second);";
	}

	private String packWaitingDataList(String fieldName)
	{
		return "packer.packArrayHeader(msg." + fieldName + ".size());\n"
			+ "		for (var item : msg." + fieldName + ") {\n"
			+ "			packer.packInt(item.gameType());\n"
			+ "			packer.packInt(item.playAs());\n"
			+ "			packer.packInt(item.baseTime());\n"
			+ "			packer.packInt(item.incTime());\n"
			+ "		}";
	}
}
