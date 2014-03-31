/** * BusinessVo.java 
* Created on 2013-5-20 下午3:32:18 
*/

package models.vo.setup;

import models.Business;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

/** 
 * 
* <p>Project: CloudWMS</p> 
* <p>Title: BusinessVo.java</p> 
* <p>Description: </p> 
* <p>Copyright (c) 2013 Wilmar Consultancy Services</p>
* <p>All Rights Reserved.</p>
* @author <a href="mailto:chenaihong@wcs-global.com">Chen Aihong</a>
 */

@JsonIgnoreProperties(ignoreUnknown = true)
public class BusinessVo {
	public String id;
	public String nameKey;

	public void initBusiness(Business business) {
		id = business.id.toString();
		nameKey = business.nameKey;
	}

}
