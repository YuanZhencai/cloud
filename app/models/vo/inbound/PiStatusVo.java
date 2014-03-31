/** * piStatusVo.java 
* Created on 2013-6-6 下午1:43:58 
*/

package models.vo.inbound;

import models.Code;

/** 
 * <p>Project: CloudWMS</p> 
 * <p>Title: piStatusVo.java</p> 
 * <p>Description: </p> 
 * <p>Copyright (c) 2013 Wilmar Consultancy Services</p>
 * <p>All Rights Reserved.</p>
 * @author <a href="mailto:chenaihong@wcs-global.com">ChenAi hong</a>
 */

public class PiStatusVo {
	public String code_key;
	public String code_name;
	
	public void initCode(Code code){
		code_key = code.codeKey;
		code_name = code.nameKey;
	}

}
