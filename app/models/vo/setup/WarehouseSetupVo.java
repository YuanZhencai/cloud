package models.vo.setup;

import java.math.BigDecimal;
import java.sql.Timestamp;

import models.BinCapacity;

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
public class WarehouseSetupVo {
	public String plantNameId;
	public String plantName;
	public String warehouseId;
	public String warehouse;
	public String storageTypeId;
	public String storageType;
	public String storageAreaId;
	public String storageArea;
	public String storageBinId;
	public String storageBin;
	public BigDecimal maximunCapacity;
	public String capacityType;
	public boolean active;
	public String activeStr;
	public Timestamp lastUpdateDate;

	public void fillVo(BinCapacity binCapacity) {
		plantNameId = binCapacity.bin.area.storageType.warehouse.company.id.toString();
		plantName = binCapacity.bin.area.storageType.warehouse.company.nameKey;
		warehouseId = binCapacity.bin.area.storageType.warehouse.id.toString();
		warehouse = binCapacity.bin.area.storageType.warehouse.nameKey;
		storageTypeId = binCapacity.bin.area.storageType.id.toString();
		storageType = binCapacity.bin.area.storageType.nameKey;
		storageAreaId = binCapacity.bin.area.id.toString();
		storageArea = binCapacity.bin.area.nameKey;
		storageBinId = binCapacity.bin.id.toString();
		storageBin = binCapacity.bin.nameKey;
		if (binCapacity.capacity != null) {
			maximunCapacity = binCapacity.capacity;
		}
		capacityType = binCapacity.capacityType;
		active = !binCapacity.bin.deleted;
		if (active == false) {
			activeStr = "NO";
		} else {
			activeStr = "YES";
		}
		lastUpdateDate = binCapacity.bin.updatedAt;
	}
}
