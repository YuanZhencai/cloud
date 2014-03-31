package controllers.setup;

import java.io.File;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import models.Area;
import models.Bin;
import models.BinCapacity;
import models.Code;
import models.Company;
import models.Stock;
import models.StorageType;
import models.Warehouse;
import models.vo.setup.CompanyVo;
import models.vo.setup.StorageTypeVo;
import models.vo.setup.WarehouseSetupSearchVo;
import models.vo.setup.WarehouseSetupVo;
import models.vo.setup.WarehouseUploadVo;
import models.vo.setup.WarehouseVo;
import security.NoUserDeadboltHandler;
import org.codehaus.jackson.JsonNode;

import action.Menus;
import be.objectify.deadbolt.java.actions.SubjectPresent;

import com.avaje.ebean.ExpressionList;

import play.Logger;
import play.db.ebean.Transactional;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.With;
import play.mvc.Http.MultipartFormData;
import play.mvc.Http.MultipartFormData.FilePart;
import utils.CodeUtil;
import utils.CrudUtil;
import utils.DateUtil;
import utils.SessionSearchUtil;
import utils.Excel.ReadExcel;
import views.html.setup.warehouse;

/** 
* <p>Project: CloudWMS</p> 
* <p>Title: WarehouseController.java</p> 
* <p>Description: </p> 
* <p>Copyright (c) 2013 Wilmar Consultancy Services</p>
* <p>All Rights Reserved.</p>
* @author <a href="mailto:wangyang6@wcs-global.com">Wang Yang</a> 
*/
@SubjectPresent(handler = NoUserDeadboltHandler.class)
@With(Menus.class)
public class WarehouseController extends Controller {
	// 仓库区域类型
	static String storageType = "STORAGE_TYPE";

	/**
	 * 页面数据初始化
	 * @return
	 */
	public static Result index() {
		return ok(warehouse.render(""));
	}

	/**
	 * 初始化列表
	 * @return
	 */
	@Transactional
	public static Result get() {
		List<WarehouseSetupVo> warehouseSetVoList = new ArrayList<WarehouseSetupVo>();
		List<BinCapacity> binCapacityList = BinCapacity.find().where().eq("deleted", false).eq("bin.area.deleted", false)
				.eq("bin.area.storageType.deleted", false).eq("bin.area.storageType.warehouse.deleted", false)
				.eq("bin.area.storageType.warehouse.company.deleted", false).findList();
		System.out.println("binCapacity:" + binCapacityList.size());
		for (BinCapacity binCapacity : binCapacityList) {
			WarehouseSetupVo warehouseSetupVo = new WarehouseSetupVo();
			warehouseSetupVo.fillVo(binCapacity);
			warehouseSetVoList.add(warehouseSetupVo);
		}
		System.out.println("warehouseSetVoList:" + warehouseSetVoList.size());
		return ok(play.libs.Json.toJson(warehouseSetVoList));
	}

	/**
	 * 查询当前用户所在公司
	 * @return
	 */
	@Transactional
	public static Result initPlantName() {
		List<CompanyVo> companyVoList = new ArrayList<CompanyVo>();
		List<Company> companyList = new ArrayList<Company>();
		Warehouse warehouse = SessionSearchUtil.searchWarehouse();
		if (warehouse != null) {
			companyList = Company.find().where().eq("id", warehouse.company.id.toString()).eq("deleted", false).findList();
		}
		for (Company company : companyList) {
			CompanyVo companyVo = new CompanyVo();
			companyVo.initCompany(company);
			companyVoList.add(companyVo);
		}
		return ok(play.libs.Json.toJson(companyVoList));
	}

	/**
	 * 初始化仓库
	 * @return
	 */
	@Transactional
	public static Result initWarehouse() {
		List<WarehouseVo> warehouseVoList = new ArrayList<WarehouseVo>();
		Warehouse warehouse = SessionSearchUtil.searchWarehouse();
		if (warehouse != null) {
			WarehouseVo warehouseVo = new WarehouseVo();
			warehouseVo.initWarehouse(warehouse);
			warehouseVoList.add(warehouseVo);
		}
		return ok(play.libs.Json.toJson(warehouseVoList));
	}

	/**
	 * 初始化页面查询StorageType
	 * @return
	 */
	@Transactional
	public static Result initStorageType() {
		List<StorageTypeVo> storageTypeVoList = new ArrayList<StorageTypeVo>();
		List<Code> codeList = CodeUtil.getCode(storageType);
		for (Code code : codeList) {
			StorageTypeVo storageTypeVo = new StorageTypeVo();
			storageTypeVo.name = code.nameKey;
			storageTypeVo.nameKey = code.nameKey;
			storageTypeVoList.add(storageTypeVo);
		}
		return ok(play.libs.Json.toJson(storageTypeVoList));
	}

	/**
	 * 页面头查询
	 * @return
	 */
	@Transactional
	public static Result list() {
		JsonNode body = request().body().asJson();
		List<WarehouseSetupVo> warehouseSetVoList = new ArrayList<WarehouseSetupVo>();
		if (body != null) {
			WarehouseSetupSearchVo warehouseSerchVo = Json.fromJson(body, WarehouseSetupSearchVo.class);
			List<BinCapacity> binCapacityList = new ArrayList<BinCapacity>();
			ExpressionList<BinCapacity> el = BinCapacity.find().where().eq("deleted", false).eq("bin.area.deleted", false)
					.eq("bin.area.storageType.deleted", false).eq("bin.area.storageType.warehouse.deleted", false)
					.eq("bin.area.storageType.warehouse.company.deleted", false);
			if (!"".equals(warehouseSerchVo.plantName) && warehouseSerchVo.plantName != null) {
				el.eq("bin.area.storageType.warehouse.company.id", warehouseSerchVo.plantName);
			}
			if (!"".equals(warehouseSerchVo.warehouse) && warehouseSerchVo.warehouse != null) {
				el.eq("bin.area.storageType.warehouse.id", warehouseSerchVo.warehouse);
			}
			if (!"".equals(warehouseSerchVo.storageType) && warehouseSerchVo.storageType != null) {
				el.like("bin.area.storageType.nameKey", "%" + warehouseSerchVo.storageType + "%");
			}
			if (!"".equals(warehouseSerchVo.active)) {
				if ("Y".equals(warehouseSerchVo.active)) {
					el.eq("bin.deleted", false);
				} else {
					el.eq("bin.deleted", true);
				}
			}
			binCapacityList = el.findList();
			for (BinCapacity binCapacity : binCapacityList) {
				WarehouseSetupVo warehouseSetupVo = new WarehouseSetupVo();
				warehouseSetupVo.fillVo(binCapacity);
				warehouseSetVoList.add(warehouseSetupVo);
			}
		}
		return ok(play.libs.Json.toJson(warehouseSetVoList));
	}

	/**
	 * 更改bin的deleted状态
	 * @return
	 */
	@Transactional
	public static Result activeOrNot() {
		JsonNode body = request().body().asJson();
		Timestamp time = DateUtil.currentTimestamp();
		if (body != null) {
			WarehouseSetupVo warehouseSetupVo = Json.fromJson(body, WarehouseSetupVo.class);
			Bin bin = Bin.find().where().eq("id", warehouseSetupVo.storageBinId).findUnique();
			if (bin != null) {
				List<Stock> stockList = Stock.find().where().eq("deleted", false).eq("bin", bin).findList();
				if (stockList.size() > 0) {
					return badRequest("There has stocks in the bin! Can not delete!");
				} else {
					bin.deleted = warehouseSetupVo.active;
					CrudUtil.update(bin);
					time = bin.updatedAt;
				}
			} else {
				return badRequest("Can not find the bin data!");
			}
			return ok(play.libs.Json.toJson(time));
		} else {
			return badRequest("Can not get the data in the page!Please refresh the page!");
		}
	}

	/**
	 * 添加warehouseVo
	 * @return
	 */
	@Transactional
	public static Result addWarehouse() {
		JsonNode body = request().body().asJson();
		WarehouseSetupVo warehouseSetup = new WarehouseSetupVo();
		if (body != null) {
			WarehouseSetupVo warehouseSetupVo = Json.fromJson(body, WarehouseSetupVo.class);
			if (warehouseSetupVo.plantNameId != null || !"".equals(warehouseSetupVo.plantNameId)) {
				Company company = Company.find().where().eq("deleted", false).eq("id", warehouseSetupVo.plantNameId).findUnique();
				if (company != null) {
					Warehouse warehouse = Warehouse.find().where().eq("deleted", false).eq("company", company).eq("id", warehouseSetupVo.warehouseId)
							.findUnique();
					if (warehouse != null) {
						StorageType storageType = StorageType.find().where().eq("deleted", false).eq("warehouse", warehouse)
								.eq("nameKey", warehouseSetupVo.storageType).findUnique();
						if (storageType != null) {
							List<Area> areaList = Area.find().where().eq("deleted", false).eq("nameKey", warehouseSetupVo.storageArea)
									.eq("storageType", storageType).findList();
							if (areaList.size() > 0) {
								if (areaList.size() == 1) {
									List<Bin> binList = Bin.find().where().eq("area", areaList.get(0)).eq("nameKey", warehouseSetupVo.storageBin)
											.findList();
									if (binList.size() > 0) {
										if (binList.size() == 1) {
											List<BinCapacity> binCapacityList = BinCapacity.find().where().eq("bin", binList.get(0)).findList();
											if (binCapacityList.size() > 0) {
												return badRequest("Error : there has a capacity data in the bin!");
											} else {
												BinCapacity newBinCapacity = new BinCapacity();
												newBinCapacity.bin = binList.get(0);
												if (warehouseSetupVo.maximunCapacity != null) {
													newBinCapacity.capacity = warehouseSetupVo.maximunCapacity;
												}
												newBinCapacity.capacityType = "Kg";
												CrudUtil.save(newBinCapacity);
												warehouseSetup.fillVo(newBinCapacity);
											}
										} else {
											return badRequest("there has same bin in the same area!");
										}
									} else {
										Bin newBin = new Bin();
										newBin.binCode = warehouseSetupVo.storageBin;
										newBin.nameKey = warehouseSetupVo.storageBin;
										newBin.area = areaList.get(0);
										CrudUtil.save(newBin);
										BinCapacity newBinCapacity = new BinCapacity();
										newBinCapacity.bin = newBin;
										if (warehouseSetupVo.maximunCapacity != null) {
											newBinCapacity.capacity = warehouseSetupVo.maximunCapacity;
										}
										newBinCapacity.capacityType = "Kg";
										CrudUtil.save(newBinCapacity);
										warehouseSetup.fillVo(newBinCapacity);
									}
								} else {
									return badRequest("there has same area in the same storage type!");
								}
							} else {
								Area newArea = new Area();
								newArea.warehouse = warehouse;
								newArea.areaCode = warehouseSetupVo.storageArea;
								newArea.nameKey = warehouseSetupVo.storageArea;
								newArea.storageType = storageType;
								CrudUtil.save(newArea);
								Bin newBin = new Bin();
								newBin.binCode = warehouseSetupVo.storageBin;
								newBin.nameKey = warehouseSetupVo.storageBin;
								newBin.area = newArea;
								CrudUtil.save(newBin);
								BinCapacity newBinCapacity = new BinCapacity();
								newBinCapacity.bin = newBin;
								if (warehouseSetupVo.maximunCapacity != null) {
									newBinCapacity.capacity = warehouseSetupVo.maximunCapacity;
								} else {
									newBinCapacity.capacity = null;
								}
								newBinCapacity.capacityType = "Kg";
								CrudUtil.save(newBinCapacity);
								System.out.println("newBinCapacity:" + newBinCapacity.id.toString());
								warehouseSetup.fillVo(newBinCapacity);
							}
						} else {
							StorageType newStorageType = new StorageType();
							newStorageType.warehouse = warehouse;
							newStorageType.nameKey = warehouseSetupVo.storageType;
							CrudUtil.save(newStorageType);
							Area newArea = new Area();
							newArea.warehouse = warehouse;
							newArea.areaCode = warehouseSetupVo.storageArea;
							newArea.nameKey = warehouseSetupVo.storageArea;
							newArea.storageType = storageType;
							CrudUtil.save(newArea);
							Bin newBin = new Bin();
							newBin.binCode = warehouseSetupVo.storageBin;
							newBin.nameKey = warehouseSetupVo.storageBin;
							newBin.area = newArea;
							CrudUtil.save(newBin);
							BinCapacity newBinCapacity = new BinCapacity();
							newBinCapacity.bin = newBin;
							if (warehouseSetupVo.maximunCapacity != null) {
								newBinCapacity.capacity = warehouseSetupVo.maximunCapacity;
							} else {
								newBinCapacity.capacity = null;
							}
							newBinCapacity.capacityType = "Kg";
							CrudUtil.save(newBinCapacity);
							System.out.println("newBinCapacity:" + newBinCapacity.id.toString());
							warehouseSetup.fillVo(newBinCapacity);
						}
					} else {
						return badRequest("Can not find the warehouse in the database!");
					}
				} else {
					return badRequest("Can not find the plant Name in the database!");
				}
			} else {
				return badRequest("Can not get the plant Name!");
			}
		}
		return ok(play.libs.Json.toJson(warehouseSetup));
	}

	/**
	 * 编辑warehouse
	 * @return
	 */
	@Transactional
	public static Result editWarehouse() {
		JsonNode body = request().body().asJson();
		WarehouseSetupVo warehouseSetup = new WarehouseSetupVo();
		if (body != null) {
			WarehouseSetupVo warehouseSetupVo = Json.fromJson(body, WarehouseSetupVo.class);
			if (warehouseSetupVo.plantNameId != null || !"".equals(warehouseSetupVo.plantNameId)) {
				Company company = Company.find().where().eq("deleted", false).eq("id", warehouseSetupVo.plantNameId).findUnique();
				if (company != null) {
					Warehouse warehouse = Warehouse.find().where().eq("deleted", false).eq("company", company)
							.eq("nameKey", warehouseSetupVo.warehouse).findUnique();
					if (warehouse != null) {
						StorageType storageType = StorageType.find().where().eq("deleted", false).eq("warehouse", warehouse)
								.eq("nameKey", warehouseSetupVo.storageType).findUnique();
						if (storageType != null) {
							Area area = Area.find().where().eq("id", warehouseSetupVo.storageAreaId).eq("storageType", storageType).findUnique();
							if (area != null) {
								Bin bin = Bin.find().where().eq("area", area).eq("id", warehouseSetupVo.storageBinId).findUnique();
								if (bin != null) {
									List<BinCapacity> bincapaciytList = BinCapacity.find().where().eq("bin", bin).findList();
									if (bincapaciytList.size() == 1) {
										if (warehouseSetupVo.maximunCapacity != null) {
											bincapaciytList.get(0).capacity = warehouseSetupVo.maximunCapacity;
										} else {
											bincapaciytList.get(0).capacity = null;
										}
										bincapaciytList.get(0).capacityType = "Kg";
										CrudUtil.update(bincapaciytList.get(0));
										warehouseSetup.fillVo(bincapaciytList.get(0));
									} else if (bincapaciytList.size() > 1) {
										return badRequest("this bin has more than one bincapacity!");
									}
								} else {
									return badRequest("Can not find the bin in the database!");
								}
							} else {
								return badRequest("Can not find the area in the database!");
							}
						} else {
							return badRequest("Can not find the storageType in the database!");
						}
					} else {
						return badRequest("Can not find the warehouse in the database!");
					}
				} else {
					return badRequest("Can not find the plant Name in the database!");
				}
			} else {
				return badRequest("Can not get the plant Name!");
			}
		}
		return ok(play.libs.Json.toJson(warehouseSetup));
	}

	// 上传excel数据
	@SuppressWarnings("unchecked")
	@Transactional
	public static Result upload() {
		System.out.println("^^^^^^^^^^^^^^you have in uploat method^^^^^^^^^^^^^^^^^^^^^^^");
		MultipartFormData body = request().body().asMultipartFormData();
		FilePart file = body.getFile("file");
		if (file != null) {
			String fileName = file.getFilename();
			String contentType = file.getContentType();
			File excel = file.getFile();
			System.out.println("file:  " + file + "  fileName: " + fileName + "  contentType: " + contentType);
			try {
				List<WarehouseUploadVo> WarehouseUploadVoList = (List<WarehouseUploadVo>) ReadExcel.readExcel(new WarehouseUploadVo(), excel,
						fileName);
				Warehouse warehouse = SessionSearchUtil.searchWarehouse();
				if (warehouse != null) {
					for (WarehouseUploadVo warehouseUploadVo : WarehouseUploadVoList) {
						System.out.println("#####:" + warehouseUploadVo.storageType);
						System.out.println("#####:" + warehouseUploadVo.storageArea);
						System.out.println("#####:" + warehouseUploadVo.storageBin);
						System.out.println("#####:" + warehouseUploadVo.maximumCapacity);
						System.out.println("#####:" + warehouseUploadVo.capacityType);
						System.out.println("#####:" + "".equals(warehouseUploadVo.maximumCapacity.trim()));
						List<StorageType> storageTypeList = StorageType.find().where().eq("warehouse", warehouse)
								.eq("nameKey", warehouseUploadVo.storageType).findList();
						if (storageTypeList.size() > 0) {
							List<Area> areaList = Area.find().where().eq("storageType", storageTypeList.get(0)).eq("warehouse", warehouse)
									.eq("nameKey", warehouseUploadVo.storageArea).findList();
							if (areaList.size() > 0) {
								List<Bin> binList = Bin.find().where().eq("area", areaList.get(0)).eq("nameKey", warehouseUploadVo.storageBin)
										.findList();
								if (binList.size() > 0) {
									List<BinCapacity> binCapacityList = BinCapacity.find().where().eq("bin", binList.get(0)).findList();
									if (binCapacityList.size() > 0) {
										Logger.info(">>>>>>>there has a binCapacity data in the bin");
									} else {
										BinCapacity binCapacity = new BinCapacity();
										binCapacity.bin = binList.get(0);
										if (!"".equals(warehouseUploadVo.maximumCapacity.trim())) {
											binCapacity.capacity = new BigDecimal(Double.valueOf(warehouseUploadVo.maximumCapacity));
										} else {
											binCapacity.capacity = null;
										}
										binCapacity.capacityType = warehouseUploadVo.capacityType;
										CrudUtil.save(binCapacity);
									}
								} else {
									Bin bin = new Bin();
									bin.binCode = warehouseUploadVo.storageBin;
									bin.nameKey = warehouseUploadVo.storageBin;
									bin.area = areaList.get(0);
									CrudUtil.save(bin);
									BinCapacity binCapacity = new BinCapacity();
									binCapacity.bin = bin;
									if (!"".equals(warehouseUploadVo.maximumCapacity.trim())) {
										binCapacity.capacity = new BigDecimal(Double.valueOf(warehouseUploadVo.maximumCapacity));
									} else {
										binCapacity.capacity = null;
									}
									binCapacity.capacityType = warehouseUploadVo.capacityType;
									CrudUtil.save(binCapacity);
								}
							} else {
								Area area = new Area();
								area.warehouse = warehouse;
								area.areaCode = warehouseUploadVo.storageArea;
								area.nameKey = warehouseUploadVo.storageArea;
								area.storageType = storageTypeList.get(0);
								CrudUtil.save(area);
								Bin bin = new Bin();
								bin.binCode = warehouseUploadVo.storageBin;
								bin.nameKey = warehouseUploadVo.storageBin;
								bin.area = area;
								CrudUtil.save(bin);
								BinCapacity binCapacity = new BinCapacity();
								binCapacity.bin = bin;
								if (!"".equals(warehouseUploadVo.maximumCapacity.trim())) {
									binCapacity.capacity = new BigDecimal(Double.valueOf(warehouseUploadVo.maximumCapacity));
								} else {
									binCapacity.capacity = null;
								}
								binCapacity.capacityType = warehouseUploadVo.capacityType;
								CrudUtil.save(binCapacity);
							}
						} else {
							StorageType storageType = new StorageType();
							storageType.nameKey = warehouseUploadVo.storageType;
							storageType.warehouse = warehouse;
							CrudUtil.save(storageType);
							Area area = new Area();
							area.warehouse = warehouse;
							area.areaCode = warehouseUploadVo.storageArea;
							area.nameKey = warehouseUploadVo.storageArea;
							area.storageType = storageType;
							CrudUtil.save(area);
							Bin bin = new Bin();
							bin.binCode = warehouseUploadVo.storageBin;
							bin.nameKey = warehouseUploadVo.storageBin;
							bin.area = area;
							CrudUtil.save(bin);
							BinCapacity binCapacity = new BinCapacity();
							binCapacity.bin = bin;
							if (!"".equals(warehouseUploadVo.maximumCapacity.trim())) {
								binCapacity.capacity = new BigDecimal(Double.valueOf(warehouseUploadVo.maximumCapacity));
							} else {
								binCapacity.capacity = null;
							}
							binCapacity.capacityType = warehouseUploadVo.capacityType;
							CrudUtil.save(binCapacity);
						}
					}
				} else {
					return badRequest("Can not find the warehouse!");
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			return redirect(routes.WarehouseController.index());
		} else {
			return redirect(routes.WarehouseController.index());
		}
	}
}
