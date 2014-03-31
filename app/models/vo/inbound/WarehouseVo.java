package models.vo.inbound;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

/** 
* <p>Project: CloudWMS</p> 
* <p>Title: WarehouseVo.java</p> 
* <p>Description: </p> 
* <p>Copyright (c) 2013 Wilmar Consultancy Services</p>
* <p>All Rights Reserved.</p>
* @author <a href="mailto:wangyang6@wcs-global.com">Wang Yang</a> 
*/
@JsonIgnoreProperties(ignoreUnknown = true)
public class WarehouseVo {
	public String id;
	public String nameKey;
}
