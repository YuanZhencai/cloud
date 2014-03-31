/** * CompanyVo.java 
* Created on 2013-5-8 上午9:48:00 
*/

package models.vo.setup;

import models.Company;

/** 
 * 
* <p>Project: CloudWMS</p> 
* <p>Title: CompanyVo.java</p> 
* <p>Description: </p> 
* <p>Copyright (c) 2013 Wilmar Consultancy Services</p>
* <p>All Rights Reserved.</p>
* @author <a href="mailto:chenaihong@wcs-global.com">Chen Aihong</a>
 */

public class CompanyVo {
	public String id;
	public String nameKey;

	public void initCompany(Company company) {
		id = company.id.toString();
		nameKey = company.nameKey;
	}

}
