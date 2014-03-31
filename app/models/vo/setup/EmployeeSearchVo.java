/** * EmployeeSearchVo.java 
* Created on 2013-5-17 下午4:30:49 
*/

package models.vo.setup;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

/** 
 * 
* <p>Project: CloudWMS</p> 
* <p>Title: EmployeeSearchVo.java</p> 
* <p>Description: </p> 
* <p>Copyright (c) 2013 Wilmar Consultancy Services</p>
* <p>All Rights Reserved.</p>
* @author <a href="mailto:chenaihong@wcs-global.com">Chen Aihong</a>
 */

@JsonIgnoreProperties(ignoreUnknown = true)
public class EmployeeSearchVo {
	public String id;
	public String active;
	public String employeeName;
	
	public CompanyVo companyVo;//
	public EmployeeTypeVo employeeTypeVo;//
	

}
