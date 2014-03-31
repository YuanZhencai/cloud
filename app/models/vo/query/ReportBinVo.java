package models.vo.query;

import org.codehaus.jackson.JsonNode;

import play.libs.Json;

public class ReportBinVo {
	public String binVo;
	public String storage_bin;
	public String storage_area;
	public String storage_type;
	public String warehouse;
	public BinVo returnVo(){
	//BinVo binVo2 = new BinVo();
		/*if(storage_bin!=null&&!storage_bin.equals("")){
			Bin bin = Bin.find().where().eq("id", storage_bin).eq("deleted", false).findUnique();
			binVo.inBin(bin);
		}else if(storage_area!=null&&!storage_area.equals("")){
			Area area = Area.find().where().eq("id", storage_area).eq("deleted", false).findUnique();
			AreaVo areaVo = new AreaVo();
			areaVo.inArea(area);
			binVo.nameKey="";
			binVo.areaVo=areaVo;
		}else if(storage_type!=null&&!storage_type.equals("")){
			StorageType storageType = StorageType.find().where().eq("id", storage_type).eq("deleted",false).findUnique();
			StorageTypeVo storageTypeVo = new StorageTypeVo();
			storageTypeVo.inStorageTypeVo(storageType);
			AreaVo areaVo = new AreaVo();
			areaVo.storageTypeVo=storageTypeVo;
			areaVo.nameKey="";
			binVo.areaVo=areaVo;
			binVo.nameKey="";
		}else{
			Warehouse warehouse=SessionSearchUtil.searchWarehouse();
			WarehouseVo warehouseVo = new WarehouseVo();
			warehouseVo.inwarehouse(warehouse);
			StorageTypeVo storageTypeVo = new StorageTypeVo();
			storageTypeVo.nameKey="";
			storageTypeVo.warehouseVo=warehouseVo;
			AreaVo areaVo = new AreaVo();
			areaVo.storageTypeVo=storageTypeVo;
			areaVo.nameKey="";
			binVo.areaVo=areaVo;
			binVo.nameKey="";
		}*/
		JsonNode parse = play.libs.Json.parse(binVo);
		BinVo fromJson = Json.fromJson(parse,BinVo.class);
		return fromJson;
	}
}
