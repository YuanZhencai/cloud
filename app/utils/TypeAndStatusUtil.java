/** * PropertiesUtil.java 
* Created on 2013-5-30 下午12:20:39 
*/

package utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/** 
 * <p>Project: CloudWMS</p> 
 * <p>Title: PropertiesUtil.java</p> 
 * <p>Description: </p> 
 * <p>Copyright (c) 2013 Wilmar Consultancy Services</p>
 * <p>All Rights Reserved.</p>
 * @author <a href="mailto:liyong@wcs-global.com">liyong</a>
 */

public class TypeAndStatusUtil {

	public static final String TYPEPATH="public/properties/type.properties";
	public static final String STATUSPATH="public/properties/stauts.properties";
	/**
	 * 
	 * @return
	 */
	public static Properties getTypeProperties() {
		return getProperties(TYPEPATH);
	}
	/**
	 * 
	 * @return
	 */
	public static Properties getStatusProperties() {
		return getProperties(STATUSPATH);
	}
	/**
	 * 
	 * @param path
	 * @return
	 */
	private static Properties getProperties(String path) {
		Properties	prop = new Properties();
		try {
			InputStream inputStream = new FileInputStream(path);
			prop.load(inputStream);
		} catch ( Exception e) {
			e.printStackTrace();
		}
		return prop;
	}
 
}
