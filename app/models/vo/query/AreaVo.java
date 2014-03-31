package models.vo.query;


import models.Area;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

/** 
* <p>Project: CloudWMS</p> 
* <p>Title: AreaVo.java</p> 
* <p>Description: </p> 
* <p>Copyright (c) 2013 Wilmar Consultancy Services</p>
* <p>All Rights Reserved.</p>
* @author <a href="mailto:wangyang6@wcs-global.com">Wang Yang</a> 
*/
@JsonIgnoreProperties(ignoreUnknown = true)
public class AreaVo {
	public String id;
	public String nameKey;
	public StorageTypeVo storageTypeVo;
	public void inArea(Area area){
		id=area.id.toString();
		nameKey=area.nameKey;
		storageTypeVo=new StorageTypeVo();
		storageTypeVo.inStorageTypeVo(area.storageType);
	}
}
