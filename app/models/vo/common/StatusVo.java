/** * StatusVo.java 
* Created on 2013-6-27 下午3:06:09 
*/

package models.vo.common;

import models.Code;

/** 
 * <p>Project: CloudWMS</p> 
 * <p>Title: StatusVo.java</p> 
 * <p>Description: </p> 
 * <p>Copyright (c) 2013 Wilmar Consultancy Services</p>
 * <p>All Rights Reserved.</p>
 * @author <a href="mailto:liyong@wcs-global.com">liyong</a>
 */

public class StatusVo {
	public String code_key;
	public String code_name;

	public void initCode(Code code) {
		code_key = code.codeKey;
		code_name = code.nameKey;
	}
}
