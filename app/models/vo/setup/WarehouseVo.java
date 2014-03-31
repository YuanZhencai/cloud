/** * WarehouseVo.java 
* Created on 2013-5-14 下午4:22:27 
*/

package models.vo.setup;

import models.Warehouse;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

/** 
 * 
* <p>Project: CloudWMS</p> 
* <p>Title: WarehouseVo.java</p> 
* <p>Description: </p> 
* <p>Copyright (c) 2013 Wilmar Consultancy Services</p>
* <p>All Rights Reserved.</p>
* @author <a href="mailto:chenaihong@wcs-global.com">Chen Aihong</a>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class WarehouseVo {
	public String id;
	public String nameKey;

	public void initWarehouse(Warehouse warehouse) {
		id = warehouse.id.toString();
		nameKey = warehouse.nameKey;
	}

}
