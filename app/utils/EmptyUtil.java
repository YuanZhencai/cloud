/** * EmptyUtil.java 
* Created on 2013-7-4 上午9:35:48 
*/

package utils;

import java.util.List;

/** 
 * <p>Project: CloudWMS</p> 
 * <p>Title: EmptyUtil.java</p> 
 * <p>Description: </p> 
 * <p>Copyright (c) 2013 Wilmar Consultancy Services</p>
 * <p>All Rights Reserved.</p>
 * @author <a href="mailto:liyong@wcs-global.com">liyong</a>
 */

public class EmptyUtil {

	/**
	 * 判断一个list是否不为空
	 * @param list
	 * @return  true 不为空 false 为空
	 */
	public static <T> boolean isNotEmptyList(List<T> list) {
		if (list != null && !list.isEmpty()) {
			return true;
		} else {
			return false;
		}
	}

	public static <T> boolean isEmptyList(List<T> list) {
		 return !isNotEmptyList(list);
	}
	public static <T> boolean isEmptyString(String str){
		return !isNotEmtpyString(str);
	}
	
	/**
	 * 
	 * @param str
	 * @return
	 */
	public static boolean isNotEmtpyString(String str) {
		if (str != null && !str.isEmpty() && !"null".equals(str)) {
			return true;
		} else {
			return false;
		}
	}
    /**
     * 
     * @param 
     * @return
     */
	public static String nullToEmpty(String str) {
		if (str != null && !"null".equals(str)) {
			return str;
		} else {
			return "";
		}
	}

}
