package controllers.query;


import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import action.Menus;
import be.objectify.deadbolt.java.actions.SubjectPresent;

import models.Area;
import models.Bin;
import models.BinCapacity;
import models.Stock;
import models.StorageType;
import models.UomCapacityPoint;
import models.Warehouse;
import models.vo.query.AreaVo;
import models.vo.query.BinVo;
import models.vo.query.StorageTypeVo;
import models.vo.query.WarehouseQueryVo;
import models.vo.query.WarehouseVo;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.With;
import play.mvc.Http.RequestBody;
import security.NoUserDeadboltHandler;
import utils.SessionSearchUtil;
@SubjectPresent(handler = NoUserDeadboltHandler.class)
@With(Menus.class)
public class WarehouseQueryController extends Controller {
//	 static List<WarehouseQueryVo> WarehouseQueryVos=null;
//	 static String sqlArea="select distinct name_key from t_area";
//	 static String sqlWarehouse="select distinct name_key from t_warehouse";
//	 static String sqlStorageType="select distinct name_key from t_storage_type";
//	 static String sqlBin="select distinct name_key from t_bin";
	//static String  SessionSearchUtil.searchWarehouse().id.toString() = "fe640b63-5501-44b0-9fb1-6bbd93c5e8e5";
	 public static Result index() {
		 return ok(views.html.query.warehouseQuery.render(""));
	 }
	 public static Result initBin(){
		 System.out.println("================init====================");
		 List<BinVo> binVos=new ArrayList<BinVo>();
		 List<AreaVo> areaVos=new ArrayList<AreaVo>();
		 List<StorageTypeVo> storageTypeVos = new ArrayList<StorageTypeVo>();
		 List<WarehouseVo> warehouseVos= new ArrayList<WarehouseVo>();
		 //List<Bin> bins = Bin.find().where().eq("deleted", false).eq("area.deleted", false).eq("area.storageType.deleted", false).eq("area.storageType.warehouse.deleted", false).eq("area.storageType.warehouse.id",  SessionSearchUtil.searchWarehouse().id.toString()).findList();
		 List<Warehouse> warehouses = Warehouse.find().where().eq("deleted", false).eq("id",  SessionSearchUtil.searchWarehouse().id.toString()).findList();
		 for (Warehouse warehouse : warehouses) {
			 WarehouseVo warehouseVo = new  WarehouseVo();
			 warehouseVo.inwarehouse(warehouse);
			 warehouseVos.add(warehouseVo);
			 List<StorageType> storageTypes = StorageType.find().where().eq("deleted", false).eq("warehouse", warehouse).findList();
			 for (StorageType storageType : storageTypes) {
				StorageTypeVo storageTypeVo = new StorageTypeVo();
				storageTypeVo.inStorageTypeVo(storageType);
				storageTypeVo.warehouseVo=warehouseVo;
				storageTypeVos.add(storageTypeVo);
				List<Area> areas = Area.find().where().eq("deleted", false).eq("storageType", storageType).findList();
				for (Area area : areas) {
					AreaVo areaVo = new AreaVo();
					areaVo.inArea(area);
					areaVo.storageTypeVo=storageTypeVo;
					areaVos.add(areaVo);
					List<Bin> bins = Bin.find().where().eq("deleted",false).eq("area", area).findList();
					for (Bin bin : bins) {
						 BinVo binVo = new BinVo();
						 binVo.inBin(bin);
						 binVo.areaVo=areaVo;
						 binVos.add(binVo);
					}
				}
			}
		}
		 
		 
		 
		 /*for (Bin bin : bins) {
			 BinVo binVo = new BinVo();
			 AreaVo areaVo = new AreaVo();
			 StorageTypeVo storageTypeVo = new StorageTypeVo();
			 WarehouseVo warehouseVo = new  WarehouseVo();
			 binVo.inBin(bin);
			 areaVo.inArea(bin.area);
			 storageTypeVo.inStorageTypeVo(bin.area.storageType);
			 warehouseVo.inwarehouse(bin.area.storageType.warehouse);
			 storageTypeVo.warehouseVo=warehouseVo;
			 areaVo.storageTypeVo=storageTypeVo;
			 binVo.areaVo=areaVo;
			 binVos.add(binVo);
		 }*/
		 System.out.println("===========binVos============"+binVos.size());
		 return ok(play.libs.Json.toJson(binVos));
	 }
	 public static Result initArea(){
		 List<AreaVo> areaVos=new ArrayList<AreaVo>();
		 List<StorageTypeVo> storageTypeVos = new ArrayList<StorageTypeVo>();
		 List<WarehouseVo> warehouseVos= new ArrayList<WarehouseVo>();
		 //List<Bin> bins = Bin.find().where().eq("deleted", false).eq("area.deleted", false).eq("area.storageType.deleted", false).eq("area.storageType.warehouse.deleted", false).eq("area.storageType.warehouse.id",  SessionSearchUtil.searchWarehouse().id.toString()).findList();
		 List<Warehouse> warehouses = Warehouse.find().where().eq("deleted", false).eq("id",  SessionSearchUtil.searchWarehouse().id.toString()).findList();
		 for (Warehouse warehouse : warehouses) {
			 WarehouseVo warehouseVo = new  WarehouseVo();
			 warehouseVo.inwarehouse(warehouse);
			 warehouseVos.add(warehouseVo);
			 List<StorageType> storageTypes = StorageType.find().where().eq("deleted", false).eq("warehouse", warehouse).findList();
			 for (StorageType storageType : storageTypes) {
				StorageTypeVo storageTypeVo = new StorageTypeVo();
				storageTypeVo.inStorageTypeVo(storageType);
				storageTypeVo.warehouseVo=warehouseVo;
				storageTypeVos.add(storageTypeVo);
				List<Area> areas = Area.find().where().eq("deleted", false).eq("storageType", storageType).findList();
				for (Area area : areas) {
					AreaVo areaVo = new AreaVo();
					areaVo.inArea(area);
					areaVo.storageTypeVo=storageTypeVo;
					areaVos.add(areaVo);
				}
			 }
		 }
		 return ok(play.libs.Json.toJson(areaVos));
	 }
	 public static Result initStorageType(){
		 List<StorageTypeVo> storageTypeVos = new ArrayList<StorageTypeVo>();
		 List<Warehouse> warehouses = Warehouse.find().where().eq("deleted", false).eq("id",  SessionSearchUtil.searchWarehouse().id.toString()).findList();
		 for (Warehouse warehouse : warehouses) {
			 WarehouseVo warehouseVo = new  WarehouseVo();
			 warehouseVo.inwarehouse(warehouse);
		 List<StorageType> storageTypes = StorageType.find().where().eq("deleted", false).eq("warehouse", warehouse).findList();
		 for (StorageType storageType : storageTypes) {
			StorageTypeVo storageTypeVo = new StorageTypeVo();
			storageTypeVo.inStorageTypeVo(storageType);
			storageTypeVo.warehouseVo=warehouseVo;
			storageTypeVos.add(storageTypeVo);
			}
		 }
		 return ok(play.libs.Json.toJson(storageTypeVos));
	 }
	 public static Result initWarehouse(){
		 List<WarehouseVo> warehouseVos= new ArrayList<WarehouseVo>();
		 //List<Bin> bins = Bin.find().where().eq("deleted", false).eq("area.deleted", false).eq("area.storageType.deleted", false).eq("area.storageType.warehouse.deleted", false).eq("area.storageType.warehouse.id",  SessionSearchUtil.searchWarehouse().id.toString()).findList();
		 List<Warehouse> warehouses = Warehouse.find().where().eq("deleted", false).eq("id",  SessionSearchUtil.searchWarehouse().id.toString()).findList();
		 for (Warehouse warehouse : warehouses) {
			 WarehouseVo warehouseVo = new  WarehouseVo();
			 warehouseVo.inwarehouse(warehouse);
			 warehouseVos.add(warehouseVo);
		 }
		 return ok(play.libs.Json.toJson(warehouseVos));
	 }
	 public static Result list(){
		 System.out.println("=================list==================");
		 BinVo binVo=new BinVo();
		 RequestBody body = request().body();
		 if(body.asJson()!=null)
		 binVo = Json.fromJson(body.asJson(), BinVo.class);
		 System.out.println(play.libs.Json.toJson(binVo));
		 List<WarehouseQueryVo> WarehouseQueryVos=new ArrayList<WarehouseQueryVo>();
		 List<Bin> bins=null;
		 if(binVo.areaVo==null){
			 bins= Bin.find().where().eq("deleted", false).eq("area.deleted", false).eq("area.storageType.deleted", false).eq("area.storageType.warehouse.deleted", false).eq("area.storageType.warehouse.id",  SessionSearchUtil.searchWarehouse().id.toString()).findList(); 
		 	}else{
		 bins = Bin.find().where().eq("deleted", false).eq("area.deleted", false).eq("area.storageType.deleted", false).eq("area.storageType.warehouse.deleted", false).eq("area.storageType.warehouse.id",  SessionSearchUtil.searchWarehouse().id.toString()).like("area.storageType.warehouse.nameKey",binVo.areaVo.storageTypeVo.warehouseVo.nameKey.equals("")?"%":"%"+binVo.areaVo.storageTypeVo.warehouseVo.nameKey+"%").like("area.storageType.nameKey", binVo.areaVo.storageTypeVo.nameKey.equals("")?"%":"%"+binVo.areaVo.storageTypeVo.nameKey+"%").like("area.nameKey",binVo.areaVo.nameKey.equals("")?"%":"%"+binVo.areaVo.nameKey+"%").like("nameKey",binVo.nameKey.equals("")?"%":"%"+binVo.nameKey+"%").findList();
		 	}
		 	for (Bin bin : bins) {
			List<Stock> stocks = Stock.find().fetch("bin").where().eq("deleted", false).eq("bin.deleted", false).eq("bin", bin).findList();
			HashMap<String,Double> stockSums=new HashMap<String,Double>();
			for (Stock stock : stocks) {
				List<UomCapacityPoint> UomCapacityPoints = UomCapacityPoint.find().where().eq("materialUom", stock.materialUom).eq("deleted", false).findList();
				for (UomCapacityPoint uomCapacityPoint : UomCapacityPoints) {
					System.out.println(uomCapacityPoint.capacityPoint);
					if(stockSums.containsKey(uomCapacityPoint.capacityType)){
						stockSums.put(uomCapacityPoint.capacityType, stockSums.get(uomCapacityPoint.capacityType)+uomCapacityPoint.capacityPoint.doubleValue()*stock.qty.doubleValue());
					}else{
						stockSums.put(uomCapacityPoint.capacityType, uomCapacityPoint.capacityPoint.doubleValue()*stock.qty.doubleValue());
					}
				}
			}
			List<BinCapacity> binCapacitys = BinCapacity.find().fetch("bin").where().eq("bin",bin).findList();
			HashMap<String,Double> binSums=new HashMap<String,Double>();
			for (BinCapacity binCapacity : binCapacitys) {
				//binCapacity.capacityType=="weight"
				if(binSums.containsKey(binCapacity.capacityType)){
				    if(!binSums.get(binCapacity.capacityType).equals(Double.MAX_VALUE)&&binCapacity.capacity!=null){
				        binSums.put(binCapacity.capacityType, binSums.get(binCapacity.capacityType)+binCapacity.capacity.doubleValue());
				    }else{
				        binSums.put(binCapacity.capacityType, Double.MAX_VALUE);
				    }
					//binSums.put(binCapacity.capacityType, binSums.get(binCapacity.capacityType)+(binCapacity.capacity==null?Double.MAX_VALUE:binCapacity.capacity.doubleValue()));
				}else{
					binSums.put(binCapacity.capacityType, binCapacity.capacity==null?Double.MAX_VALUE:binCapacity.capacity.doubleValue());
				}
			}
			Set<String> keySet = binSums.keySet();
			for (String key : keySet) {
				Double stockSum=(double) 0;
				if(stockSums.containsKey(key)){
					stockSum=stockSums.get(key);
				}
				Double binSum = binSums.get(key);
				WarehouseQueryVo warehouseQueryVo = new WarehouseQueryVo();
				warehouseQueryVo.inBin(bin, binSum, stockSum);
				WarehouseQueryVos.add(warehouseQueryVo);
				//System.out.println(key);
			}
		 }
		 //System.out.println("=============WarehouseQueryVos=========="+WarehouseQueryVos.size());
		// System.out.println(play.libs.Json.toJson(WarehouseQueryVos));
		 return ok(play.libs.Json.toJson(WarehouseQueryVos));
	 }
}
