package controllers.setup;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.Material;
import models.MaterialUom;
import models.OrderItem;
import models.Owner;
import models.UomCapacityPoint;
import models.vo.setup.MaterialVo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.db.ebean.Transactional;
import play.libs.Json;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Http.MultipartFormData;
import play.mvc.Http.MultipartFormData.FilePart;
import play.mvc.Http.RequestBody;
import play.mvc.Result;
import play.mvc.With;
import security.NoUserDeadboltHandler;
import utils.CrudUtil;
import utils.EmptyUtil;
import utils.Excel.ExcelHelper;
import utils.Service.DataExchangePlatform;
import utils.exception.CustomException;
import views.html.setup.materialmaster;
import action.Menus;
import be.objectify.deadbolt.java.actions.SubjectPresent;

import com.avaje.ebean.Ebean;

import controllers.report.TemperingRoomController;
 

/** 
* <p>Project: CloudWMS</p> 
* <p>Title: MaterialMasterController.java</p> 
* <p>Description: </p> 
* <p>Copyright (c) 2013 Wilmar Consultancy Services</p>
* <p>All Rights Reserved.</p>
* @author <a href="mailto:wangyang6@wcs-global.com">Wang Yang</a> 
*/
@SubjectPresent(handler = NoUserDeadboltHandler.class)
@With(Menus.class)
public class MaterialMasterController extends Controller {
	private static String COLUMN = "column";
	private static final String ERROR="Error ";
	private static final Logger logger =LoggerFactory.getLogger(TemperingRoomController.class);
	/**
	 * 
	 * @return
	 */
	public static Result index() {
		return ok(materialmaster.render(""));
	}

	/**
	 * 
	 * @return
	 * @throws Exception 
	 */
	@Transactional
	@BodyParser.Of(value = BodyParser.Json.class)
	public static Result saveOrUpdate() {
		logger.info("^^^^^^^^^^^^^^^^^^^you have in saveOrUpdate^^^^^^^^^^^^");
		RequestBody body = request().body();
		boolean flag = false;

		StringBuffer sb = new StringBuffer();
		if (body != null && body.asJson() != null) {
			//logger.info("^^^^^body.asJson " + body.asJson());
			MaterialVo materialVo = Json.fromJson(body.asJson(), MaterialVo.class);
			if (materialVo.customId == null) {
				flag = true;
				sb.append(" Custom ");
			}
			if (materialVo.matCode == null) {
				flag = true;
				sb.append(" MatCode ");
			}
			if (materialVo.fromuom == null) {
				flag = true;
				sb.append(" BaseUom ");
			}
			if (flag) {
				return ok(sb.append(" cannot null").toString());
			}
			try {
				MaterialVo.setMaterial(materialVo);
			} catch (CustomException e) {
				e.printStackTrace();
				return ok(ERROR+e.getMessage());
			}

			return ok("The operation was successful");
		} else {
			return ok(ERROR+" System Error");
		}

	}

	/**
	 * 
	 * @return
	 */
	@BodyParser.Of(value = BodyParser.Json.class)
	public static Result list() {
		logger.info("************* you have in list method ****************");
		RequestBody body = request().body();
		if (body != null && body.asJson() != null) {
			//logger.info("^^^^^body.asJson " + body.asJson());
			MaterialVo materialVo = Json.fromJson(body.asJson(), MaterialVo.class);
			List<MaterialVo> materialVos = MaterialVo.getMaterialVo(materialVo);
			//logger.warn("^^^^^Json.toJson(codeVos) : " + Json.toJson(materialVos));
			return ok(Json.toJson(materialVos));
		} else {
			return ok(" System Error");
		}

	}

	/**
	 * 
	 * @param id
	 * @return
	 */
	@Transactional
	public static Result delete(String id) {
		logger.info("^^^^^^^^^^^   have in save delete method   ^^^^^parma id is :^^^^" + id);
		UomCapacityPoint uomPonit = Ebean.find(UomCapacityPoint.class).where().eq("deleted", false).eq("id", id).findUnique();
		List<OrderItem> orderItemList = Ebean.find(OrderItem.class).where().eq("material.id", uomPonit.materialUom.material.id).eq("deleted", false).findList();
		if(EmptyUtil.isNotEmptyList(orderItemList)){
			return ok(ERROR+"delete fail,this material has been used by PI!");	
		}
		List<MaterialUom> matUoms = Ebean.find(MaterialUom.class).where().eq("deleted", false).eq("material", uomPonit.materialUom.material)
			.findList();
		CrudUtil.delete(uomPonit.materialUom.material);
		//删除物料的时候把与其相关的单位也删除
		if (matUoms != null && !matUoms.isEmpty()) {
			for (MaterialUom matUom : matUoms) {
			  CrudUtil.delete(matUom);
			}
		}
		return ok("delete success!");
	}

	// =========================对uom的操作==================================================
	/**
	 * 
	 * @return
	 */
	@Transactional
	@BodyParser.Of(value = BodyParser.Json.class)
	public static Result saveOrUpdateUom() {
		logger.info("^^^^^^^^^^^^^^^^^^^you have in saveOrUpdate^^^^^^^^^^^^");
		RequestBody body = request().body();
		boolean flag = false;

		StringBuffer sb = new StringBuffer(ERROR);
		if (body != null && body.asJson() != null) {
			//logger.info("^^^^^body.asJson " + body.asJson());
			MaterialVo materialVo = Json.fromJson(body.asJson(), MaterialVo.class);

			if (materialVo.touom == null) {
				flag = true;
				sb.append(" To Uom ");
			}
			if (materialVo.convertRate == null) {
				flag = true;
				sb.append(" ConvertRate ");
			}
			if (flag) {
				return ok(sb.append(" cannot null").toString());
			}
			try {
				MaterialVo.setUom(materialVo);
			} catch (Exception e) {
				return ok(ERROR+e.getMessage()); 
			}
			return ok("you hava  success operate");
		} else {
			return ok(ERROR+" System Error");
		}
	}

	/**
	 * 
	 * @param id
	 * @return
	 */
	public static Result listUom(String id) {
		logger.info("^^^^^^^^^^^   have in save listUom method   ^^^^^parma id is :^^^^" + id);
		UomCapacityPoint uomPonit = Ebean.find(UomCapacityPoint.class).where().eq("deleted", false).eq("id", id).findUnique();
		List<MaterialVo> materialUomVo = MaterialVo.getMaterialUomVo(uomPonit.materialUom.material);
		//logger.info("^^^^^Json.toJson(codeVos) : " + Json.toJson(materialUomVo));
		return ok(Json.toJson(materialUomVo));
	}

	/**
	 * 
	 * @param id
	 * @return
	 */
	public static Result deleteUom(String id) {
		logger.info("^^^^^^^^^^^   have in save deleteUom method   ^^^^^parma id is :^^^^" + id);
		UomCapacityPoint uomPonit = Ebean.find(UomCapacityPoint.class).where().eq("deleted", false).eq("id", id).findUnique();
		 try {
			 MaterialVo.checkUomUsed(id);
		} catch (CustomException e) {
			return ok(ERROR+e.getMessage());
		}
		CrudUtil.delete(uomPonit.materialUom);
		return ok("delete success!");
	}
 
	
	/**
	 * 
	 * @return
	 */
/*	@Transactional
	public static Result upload(){
		 MultipartFormData body = request().body().asMultipartFormData();
		  FilePart file = body.getFile("file");
		  if (file != null) {
		    String fileName = file.getFilename();
		    String contentType = file.getContentType(); 
		    File excel = file.getFile();
		    List<MaterialVo> readExcels = (List<MaterialVo>) ReadExcel.readExcel(new MaterialVo(), 6,excel);
	    	for (MaterialVo materialVo : readExcels) {
				logger.info(play.libs.Json.toJson(materialVo));
			}
	    	return redirect(routes.MaterialMasterController.index());
		  } else {
			  return redirect(routes.MaterialMasterController.index());
		  }
	 
	}*/
	/**
	 * 
	 * @return
	 */
	@Transactional
	public static Result upload(){
		MultipartFormData body = request().body().asMultipartFormData();
	//	logger.info("^^^^^body.asJson " + body.asJson());
		//MaterialVo materialVo = Json.fromJson(body.asJson(), MaterialVo.class);
		FilePart file = body.getFile("file");
		if (file != null) {
			String fileName = file.getFilename();
			String contentType = file.getContentType();
			File excel = file.getFile();
			logger.info("file:  "+file+"  fileName: "+ fileName+"  contentType: "+contentType);
			Map<String, List<Map<String, Object>>> map = new HashMap<String, List<Map<String,Object>>>();
			try {
				map = ExcelHelper.exportDataFromExcel(file, null);
				//1、 校验excel中的值是否正确
				// validateData(map);
				//2、唯一性检查 ，看数据库中是否有相同的数据
				//uniqueCheckData(map);
				//3 保存进数据库
				setMatUomPoint(map);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return redirect(routes.MaterialMasterController.index());
		} else {
			return redirect(routes.MaterialMasterController.index());
		}
		
	}
	/**
	 * 
	 * @return
	 */
	public static Result updateAll(){
		logger.info("^^^^^^^^^^^^^^^^^ you have in updateAll++++++++");
		List<Material> Oldmaterials = Material.find().where().eq("deleted", false).findList();
		for (Material material : Oldmaterials) {
			DataExchangePlatform.updateMaterial(material.materialCode,material.owner.ownerCode);
		}
		return ok("Success");	
		
 
	}
	public static Result updateMaterial(String id){
		Material material = Material.find().where().eq("deleted",false).eq("id", id).findUnique();
		DataExchangePlatform.updateMaterial(material.materialCode,material.owner.ownerCode);
		return ok("Success");
		
	}
	// ================================工具方法==============================================
	/**
	 * 保存从sap过来的物料
	 * @param mat
	 * @throws CustomException
	 */
	public static void saveMatAndUom(Material mat) throws CustomException{
		MaterialUom baseUom = checkMatAndUom(mat);
		List<MaterialUom> items = mat.items;
		UomCapacityPoint uomCapacityPoint = null;
		mat.items = null;
		CrudUtil.save(mat);
		BigDecimal baseCapacityPoint = baseUom.qtyOfBaseNum.divide(baseUom.qtyOfBaseDenom, RoundingMode.HALF_UP);
		for(MaterialUom uom :items ){
			uomCapacityPoint  = new UomCapacityPoint();
			if(uom.baseUom){
				uomCapacityPoint.capacityPoint = baseCapacityPoint;
			}else{
				uomCapacityPoint.capacityPoint = baseCapacityPoint.multiply(uom.qtyOfBaseNum.divide(uom.qtyOfBaseDenom, RoundingMode.HALF_UP));
			}
			uom.material = mat;
			uom.warehouse = mat.owner.warehouse;
			CrudUtil.save(uom);
			uomCapacityPoint.materialUom = uom;
			uomCapacityPoint.capacityType = "Kg";
			CrudUtil.save(uomCapacityPoint);
		}
		
		
	}
	/**
	 * 
	 * @param mat
	 * @return
	 * @throws CustomException
	 */
	public static MaterialUom checkMatAndUom(Material mat) throws CustomException{
		MaterialUom baseUom = null;
         if(mat ==null){
        	 throw new CustomException("Material  is null");
         }
		if(mat.owner == null){
			throw new CustomException("Material owner is null");
		}
		if(EmptyUtil.isEmptyString(mat.materialCode)){
			throw new CustomException("Material materialCode is null");
		}
		if(EmptyUtil.isEmptyList(mat.items)){
			throw new CustomException("Material Uom is null");
		}
		for(MaterialUom uom :  mat.items){
			if(EmptyUtil.isEmptyString(uom.uomCode)||  uom.qtyOfBaseDenom ==null|| uom.qtyOfBaseNum==null){
				throw new CustomException("Material Uom one of uomCode,qtyOfBaseDenom,qtyOfBaseNum is empty");	
			}
			if(uom.baseUom){
				baseUom  =uom;
			}
		}
		if(baseUom==null){
			throw new CustomException("Material BaseUom is null");	
		}
		return baseUom;
		 
	}
	
	/**
	 * 
	 */
	public static void  getMatUomPoint(String filePath,String sheetName){
		List<Material> mats = Ebean.find(Material.class).where().eq("deleted", false).findList();
		logger.info("^^^^^^^^^^^^^^^^^mats size++++++++"+mats.size());
		List<Map<String, String>> data = convertModelToMap(mats);
		logger.info("^^^^^^^^^^^^^^^^^data  "+data);
		try {
			ExcelHelper.importDataToExcel(data, filePath, sheetName);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	/**
	 * 
	 * @param mats
	 * @return
	 */
	public static List<Map<String, String>>  convertModelToMap(List<Material> mats){
		List<Map<String, String>> data = new ArrayList<Map<String, String>>();
		boolean flag = true;
		if(mats!=null&& !mats.isEmpty()){
			int i =0;
			for(Material mat: mats){
				Map<String, String> map = new HashMap<String, String>();
				map.put(COLUMN + 1, String.valueOf(++i));
				map.put(COLUMN + 2, mat.materialCode);
				map.put(COLUMN + 3, mat.materialName);
				 if(flag && map.size()<6){
					 int j = 1;
					 while(map.size()<6){
						 map.put("size"+j, "size"+j);
						 j++;
					 }
					 flag= false;
				 }
				data.add(map);
			}
		}
		return data;
	}
	
	/**
	 * 
	 * @param id
	 * @return
	 */
	public static Result getCuteoms(String id) {
		logger.info("^^^^^^^^^^^   have in save getCuteoms method   ^^^^^parma id is :^^^^" + id);
		List<Map<String, Object>> owners = new ArrayList<Map<String, Object>>();
		List<Owner> ownerEntitys = Ebean.find(Owner.class).where().eq("deleted", false).findList();
		if (ownerEntitys != null && !ownerEntitys.isEmpty()) {
			for (Owner owner : ownerEntitys) {
				Map<String, Object> map = new HashMap<String, Object>();
				map.put("id", owner.id);
				map.put("desc", owner.ownerName);
				owners.add(map);
			}
		}
		//logger.info("^^^^^^^^^Json.toJson(owners)^^^^^^^^^^^^" + Json.toJson(owners));
		return ok(Json.toJson(owners));
	}
	/**
	 * 
	 * @param map
	 */
	public static void  setMatUomPoint(Map<String, List<Map<String, Object>>> map){
		Owner owner = Ebean.find(Owner.class).where().eq("deleted", false).findList().get(0);
		List<Map<String, Object>> sheet = map.get("Final");
		if(sheet!=null&&!sheet.isEmpty()){
		  //操作行
			int i = 1;
			for(Map<String, Object> row :sheet){
			
				Material mat = new Material();
				MaterialUom  baseUom = new MaterialUom();
				MaterialUom  skuUom = new MaterialUom();
				UomCapacityPoint  basePoint= new UomCapacityPoint();
				UomCapacityPoint  skuPoint= new UomCapacityPoint();
				BigDecimal bigOne = new BigDecimal("1");
				// Material
				mat.owner = owner;
				mat.materialCode = (String) row.get("Material Code");
				mat.materialName = (String) row.get("DESCRIPTION");
				mat.weightUnitCode = null;
				mat.grossWeight = null;
				mat.netWeight = bigOne;
				mat.volumnUnitCode = null;
				mat.volumn = null;
				mat.sourceType = null;
				//baseUom
				baseUom.warehouse = owner.warehouse;
				baseUom.material = mat;
				baseUom.uomCode = (String) row.get("UOM");
				baseUom.baseUom = true;
				baseUom.qtyOfBaseNum = bigOne;
				baseUom.qtyOfBaseDenom = bigOne;
			    //basePoint
				basePoint.materialUom = baseUom;
				basePoint.capacityType="Kg";
				basePoint.capacityPoint = bigOne;
				//skuUom
				skuUom.warehouse = owner.warehouse;
				skuUom.material = mat;
				skuUom.uomCode = (String) row.get("SKU UOM");
				skuUom.baseUom = false;
				skuUom.qtyOfBaseNum = new BigDecimal(String.valueOf(row.get("NET WEIGHT")));
				skuUom.qtyOfBaseDenom = bigOne;
				//skuPoint
				skuPoint.materialUom = skuUom;
				skuPoint.capacityType="Kg";
				skuPoint.capacityPoint = skuUom.qtyOfBaseNum.multiply(basePoint.capacityPoint);
				
				CrudUtil.save(mat);
				CrudUtil.save(baseUom);
				CrudUtil.save(basePoint);
				CrudUtil.save(skuUom);
				CrudUtil.save(skuPoint);
			}
		}
	}
}
