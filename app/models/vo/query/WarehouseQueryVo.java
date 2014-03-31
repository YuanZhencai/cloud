package models.vo.query;


import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import models.Bin;
@JsonIgnoreProperties(ignoreUnknown = true)
public class WarehouseQueryVo {
	public String warehouse;
	public String storageType;
	public String storageArea;
	public String storageBin;
	public double totalCapacity;
	public double used;
	public double free;
	public void inBin(Bin bin,double totalCapacity,double stockCapacity){
		storageBin=bin.nameKey;
		storageArea=bin.area.nameKey;
		storageType=bin.area.storageType.nameKey;
		warehouse=bin.area.storageType.warehouse.nameKey;
		this.totalCapacity=totalCapacity;
		if(totalCapacity!=0){
		used=stockCapacity/totalCapacity*100;
		free=(totalCapacity-stockCapacity)/totalCapacity*100;
		}else{ 
		used=100;
		free=0;
		}
	}
}
