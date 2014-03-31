package utils.Excel;

import java.lang.reflect.Method;

public abstract class ExcelObj {
	private String errMsg;

	public String getErrMsg() {
		return errMsg;
	}

	public void setErrMsg(String errMsg) {
		this.errMsg = errMsg;
	}

	/**
	 * 通过属性名设置属性值
	 * @param name
	 * @param value
	 */
	@SuppressWarnings("unchecked")
	public void putValue(String name, Object value) {
		Class c = this.getClass();
		Class v = value.getClass();
		try {
			Method m = c.getMethod("set" + firstLetterToUpper(name), new Class[] { v });
			if (new Object[] { value } != null)
				m.invoke(this, new Object[] { value });
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 返回属性名对应的值
	 * @param name
	 * @return 属性名对应的值
	 */
	@SuppressWarnings("unchecked")
	public Object outValue(String name) {
		Class c = this.getClass();
		Object o = null;
		try {
			Method m = c.getMethod("get" + firstLetterToUpper(name), new Class[] {});
			o = m.invoke(this, new Object[] {});
		} catch (Exception e) {
			e.printStackTrace();
		}
		return o;
	}

	public static String firstLetterToUpper(String str) {
		char[] array = str.toCharArray();
		if (array[0] >= 97)
			array[0] -= 32;
		return String.valueOf(array);
	}

}
