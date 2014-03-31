/** * WarehouseUploadVo.java 
* Created on 2013-5-28 下午2:45:38 
*/

package models.vo.setup;

import utils.Excel.ExcelObj;

/** 
* <p>Project: CloudWMS</p> 
* <p>Title: 通过excel初始化warehouse数据使用</p> 
* <p>Description: </p> 
* <p>Copyright (c) 2013 Wilmar Consultancy Services</p>
* <p>All Rights Reserved.</p>
* @author <a href="mailto:wangyang6@wcs-global.com">Wang Yang</a> 
*/
public class WarehouseUploadVo extends ExcelObj {
	public String no;
	public String plantName;
	public String warehouse;
	public String storageType;
	public String storageArea;
	public String storageBin;
	public String maximumCapacity;
	public String capacityType;
}
