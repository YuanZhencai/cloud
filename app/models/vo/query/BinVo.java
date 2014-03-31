package models.vo.query;

import models.Bin;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

/** 
* <p>Project: CloudWMS</p> 
* <p>Title: BinVo.java</p> 
* <p>Description: </p> 
* <p>Copyright (c) 2013 Wilmar Consultancy Services</p>
* <p>All Rights Reserved.</p>
* @author <a href="mailto:wangyang6@wcs-global.com">Wang Yang</a> 
*/
@JsonIgnoreProperties(ignoreUnknown = true)
public class BinVo {
	public String id;
	public String nameKey;
	public AreaVo areaVo;
	public void inBin(Bin bin){
		id=bin.id.toString();
		nameKey=bin.nameKey;
		areaVo=new AreaVo();
		areaVo.inArea(bin.area);
	}
}
