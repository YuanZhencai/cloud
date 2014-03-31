package models.vo.setup;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

/** 
* <p>Project: CloudWMS</p> 
* <p>Title: WarehouseSearchVo.java</p> 
* <p>Description: </p> 
* <p>Copyright (c) 2013 Wilmar Consultancy Services</p>
* <p>All Rights Reserved.</p>
* @author <a href="mailto:wangyang6@wcs-global.com">Wang Yang</a> 
*/
@JsonIgnoreProperties(ignoreUnknown = true)
public class WarehouseSetupSearchVo {
	public String plantName;
	public String warehouse;
	public String storageType;
	public String active;
}
