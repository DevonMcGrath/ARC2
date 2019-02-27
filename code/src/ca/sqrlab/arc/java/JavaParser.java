package ca.sqrlab.arc.java;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.regex.Pattern;

public class JavaParser {
	
	public static final int MODIFIER_PUBLIC = 1;
	
	public static final int MODIFIER_PROTECTED = 2;
	
	public static final int MODIFIER_PRIVATE = 3;
	
	public static final int MODIFIER_DEFAULT = 4;
	
	public static final int MODIFIER_ABSTRACT = 5;
	
	public static final int MODIFIER_FINAL = 6;
	
	public static final int MODIFIER_SYNCHRONIZED = 7;
	
	public static final int MODIFIER_VOLATILE = 8;
	
	public static final int MODIFIER_NATIVE = 9;
	
	public static final int MODIFIER_STRICTFP = 10;
	
	public static final int MODIFIER_TRANSIENT = 11;
	
	public static final int MODIFIER_CONST = 12;
	
	public static final int TYPE_VOID = 101;
	
	public static final int TYPE_INT = 102;
	
	public static final int TYPE_FLOAT = 103;
	
	public static final int TYPE_LONG = 104;
	
	public static final int TYPE_DOUBLE = 105;
	
	public static final int TYPE_SHORT = 106;
	
	public static final int TYPE_BYTE = 107;
	
	public static final int TYPE_CHAR = 108;
	
	public static final int TYPE_BOOLEAN = 109;
	
	public static final int TYPE_NON_PRIMITIVE = 110;
	
	public static final int TYPE_INVALID = 199;
	
	public static final String JAVA_FILE_REGEX = "(_[_\\$a-zA-Z]+|"
			+ "[\\$a-zA-Z]+)[_\\$a-zA-Z0-9]*\\.java";
	
	private String code;
	
	public JavaParser(String code) {
		setCode(code);
	}

	public JavaParser(File javaFile) {
		setCode(javaFile);
	}
	
	public boolean setCode(File javaFile) {
		
		// Make sure it is in fact a java file
		if (!isJavaFile(javaFile)) {
			return false;
		}
		
		// Make sure we can read the file
		if (!javaFile.canRead()) {
			return false;
		}
		
		// Parse the file
		String data = "";
		InputStream is = null;
		try {
			is = new FileInputStream(javaFile);
			byte[] buffer = new byte[1024];
			int length = 0;
			while ((length = is.read(buffer)) > 0) {
				buffer = Arrays.copyOf(buffer, length);
				data += new String(buffer);
	        }
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		// Close the file
		finally {
			if (is != null) {
				try {
					is.close();
				} catch (Exception e) {}
			}
		}
		
		// Update the code
		setCode(data);
		
		return true;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}
	
	public static boolean isJavaFile(String path) {
		return path == null? false : isJavaFile(new File(path));
	}
	
	public static boolean isJavaFile(File file) {
		
		// Not a file
		if (file == null || file.isFile()) {
			return false;
		}
		
		// Check the file name more intensely
		String fn = file.getName();
		return Pattern.compile(
				"(_[_\\$a-zA-Z]+|[\\$a-zA-Z]+)[_\\$a-zA-Z0-9]*\\.java")
				.matcher(fn).matches();
	}
	
	public static boolean isValidTypeName(String type) {
		
		// Validate against a regular expression
		if (type == null || type.isEmpty()) {
			return false;
		}
		return Pattern.compile("(_[_\\$a-zA-Z]+|[\\$a-zA-Z]+)[_\\$a-zA-Z0-9]*")
				.matcher(type).matches();
	}
	
	public static int getType(String type) {
		
		// Check if it is a primitive type
		if (type == null || type.isEmpty()) {
			return TYPE_INVALID;
		} if (type.equals("void")) {
			return TYPE_VOID;
		} if (type.equals("int")) {
			return TYPE_INT;
		} if (type.equals("float")) {
			return TYPE_FLOAT;
		} if (type.equals("long")) {
			return TYPE_LONG;
		} if (type.equals("double")) {
			return TYPE_DOUBLE;
		} if (type.equals("short")) {
			return TYPE_SHORT;
		} if (type.equals("byte")) {
			return TYPE_BYTE;
		} if (type.equals("char")) {
			return TYPE_CHAR;
		} if (type.equals("boolean")) {
			return TYPE_BOOLEAN;
		}
		
		// Check if valid type name
		return isValidTypeName(type)? TYPE_NON_PRIMITIVE : TYPE_INVALID;
	}
}
