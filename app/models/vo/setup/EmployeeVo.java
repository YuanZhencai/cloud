/** * EmployeeVo.java 
* Created on 2013-5-7 下午4:56:27 
*/

package models.vo.setup;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

/** 
 * 
* <p>Project: CloudWMS</p> 
* <p>Title: EmployeeVo.java</p> 
* <p>Description: </p> 
* <p>Copyright (c) 2013 Wilmar Consultancy Services</p>
* <p>All Rights Reserved.</p>
* @author <a href="mailto:chenaihong@wcs-global.com">Chen Aihong</a>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class EmployeeVo {

	public String id;
	public String employeeName;
	public String employeeCode;
	public String updateAt;
	public boolean delete = false;
	public String active = "Yes";

	public CompanyVo companyVo;//
	public EmployeeTypeVo employeeTypeVo;//
	public WarehouseVo warehouseVo;
}
