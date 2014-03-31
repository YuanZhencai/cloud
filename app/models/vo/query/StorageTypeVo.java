package models.vo.query;


import models.StorageType;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class StorageTypeVo {
	public String id;
	public String nameKey;
	public WarehouseVo warehouseVo;
	public void inStorageTypeVo(StorageType storageType) {
		id = storageType.id.toString();
		nameKey = storageType.nameKey;
		warehouseVo=new WarehouseVo();
		warehouseVo.inwarehouse(storageType.warehouse);
	}
}
