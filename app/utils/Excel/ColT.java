package utils.Excel;

public class ColT {
	/**不对列进行类型转换*/
	public static final int NO = 0;
	/**字符串类型*/
	public static final int cSTRING = 1;
	/**整型*/
	public static final int cINT = 2;
	/**单精度浮点*/
	public static final int cFlOAT = 3;
	/**双精度浮点*/
	public static final int cDOUBLE = 4;
	/**日期类型*/
	public static final int cDATE = 5;
	/**长整型*/
	public static final int cLONG = 6;
	/**代码值,使用该类型需要设置codeType*/
	public static final int cCode = 7;

	public static final int cBoolean = 8;

	public static int getType(String type) {
		if (type.equals("class java.lang.String")) {
			return cSTRING;
		}
		if (type.equals("class java.lang.Integer") || type.equals("int")) {
			return cINT;
		}
		if (type.equals("class java.lang.Double") || type.equals("double")) {
			return cDOUBLE;
		}
		if (type.equals("class java.util.Date")) {
			return cDATE;
		}
		if (type.equals("class java.lang.Boolean") || type.equals("boolean")) {
			return cBoolean;
		}
		return NO;
	}
}
