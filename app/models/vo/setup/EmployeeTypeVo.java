/** * EmployeeTypeVo.java 
* Created on 2013-5-8 上午10:05:46 
*/

package models.vo.setup;

import models.EmployeeType;

/** 
 * 
* <p>Project: CloudWMS</p> 
* <p>Title: EmployeeTypeVo.java</p> 
* <p>Description: </p> 
* <p>Copyright (c) 2013 Wilmar Consultancy Services</p>
* <p>All Rights Reserved.</p>
* @author <a href="mailto:chenaihong@wcs-global.com">Chen Aihong</a>
 */

public class EmployeeTypeVo {
	public String id;
	public String nameKey;

	public void initEmployeeType(EmployeeType employeeType) {
		id = employeeType.id.toString();
		nameKey = employeeType.nameKey;
	}

}
