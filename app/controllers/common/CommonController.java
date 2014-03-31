/** * CodeController.java 
* Created on 2013-6-27 下午2:34:13 
*/

package controllers.common;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.avaje.ebean.Ebean;

import controllers.transfer.TransferPlanController;

import models.Area;
import models.Bin;
import models.Code;
import models.EmployeeWarehouse;
import models.OrderItem;
import models.StorageType;
import models.vo.common.StatusVo;
import models.vo.inbound.PiStatusVo;
import models.vo.transfer.MapVo;
import play.db.ebean.Transactional;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import utils.CodeUtil;
import utils.EmptyUtil;
import utils.SessionSearchUtil;
import utils.enums.CodeKey;

/** 
 * <p>Project: CloudWMS</p> 
 * <p>Title: CodeController.java</p> 
 * <p>Description: </p> 
 * <p>Copyright (c) 2013 Wilmar Consultancy Services</p>
 * <p>All Rights Reserved.</p>
 * @author <a href="mailto:liyong@wcs-global.com">liyong</a>
 */

public class CommonController extends Controller {

	//private static final String ORDER_TYPE_PRODUCE = "T001";
	private static final String DELETED = "deleted";
	private static final Logger logger = LoggerFactory.getLogger(TransferPlanController.class);

	/**
	 * 初始化头查询Pi status
	 * @return
	 */
	public static Result initPiStatus() {
		List<StatusVo> piStatusVos = new ArrayList<StatusVo>();
		List<Code> codes = CodeUtil.getOrderStatus();
		for (Code code : codes) {
			StatusVo piStatusVo = new StatusVo();
			piStatusVo.initCode(code);
			piStatusVos.add(piStatusVo);
		}
		return ok(play.libs.Json.toJson(piStatusVos));
	}
	/**
	 * 初始化头查询Plan status
	 * @return
	 */
	public static Result initPlanStatus() {
		List<StatusVo> statusVos = new ArrayList<StatusVo>();
		List<Code> codes = CodeUtil.getOrderStatus();
		for (Code code : codes) {
			StatusVo statusVo = new StatusVo();
			statusVo.initCode(code);
			statusVos.add(statusVo);
		}
		return ok(play.libs.Json.toJson(statusVos));
	}

	/**
	 * 
	 * @return
	 */
 
	public static Result initPiNo() {
		logger.info("^^^^^^^^^^^^you have in getPiNos method ^^^^^^^ ^^^^^");
		List<String> piNames = new ArrayList<String>();
		List<OrderItem> orderItems = Ebean.find(OrderItem.class).fetch("order").where().eq("deleted", false).eq("order.deleted", false)
			.eq("order.orderType", CodeKey.ORDER_TYPE_PRODUCE.toString()).eq("order.warehouse", SessionSearchUtil.searchWarehouse()).findList();
		if (orderItems != null) {
			int i = 1;
			for (OrderItem orderItem : orderItems) {
				piNames.add(orderItem.order.internalOrderNo);
			}
		}
		return ok(play.libs.Json.toJson(piNames));
	}
	/**
	 * 
	 * @return
	 */
	public static Result initStoryType(){
		logger.info("^^^^^^^^^^^you have in getStoryType method^^^^^^^^^^^");
		List<StorageType> stEntitys = Ebean.find(StorageType.class).where().eq(DELETED, false).eq("warehouse", SessionSearchUtil.searchWarehouse()).findList();
		List<MapVo> sts = new ArrayList<MapVo>();
		if (EmptyUtil.isNotEmptyList(stEntitys)) {
			for (StorageType stEntity : stEntitys) {
				MapVo st = new MapVo();
				st.key = String.valueOf(stEntity.id);
				st.descripton = stEntity.nameKey;
				sts.add(st);
			}
		}
		return ok(Json.toJson(sts));
	}
	
	/**
	 * 
	 * @param StoryType id 
	 * @return
	 */
	public static Result initArea(String id) {
		logger.info("you have in initAreas method");
		List<Area> areaEntitys = Ebean.find(Area.class).fetch("warehouse", "nameKey").where().eq("deleted", false)
			.eq("warehouse", SessionSearchUtil.searchWarehouse()).eq("storageType.id", id).order("nameKey ascending").findList();
		List<MapVo> areas = new ArrayList<MapVo>();
		if (areaEntitys != null && !areaEntitys.isEmpty()) {
			for (Area areaEntity : areaEntitys) {
				MapVo area = new MapVo();
				area.key = String.valueOf(areaEntity.id);
				area.descripton = areaEntity.nameKey;
				areas.add(area);
			}
		}
		return ok(Json.toJson(areas));
	}
	/**
	 * 
	 * @param StoryType id 
	 * @return
	 */
	public static Result initAreaNoType() {
		logger.info("you have in initAreas method,no select storage type");
		List<Area> areaEntitys = Ebean.find(Area.class).fetch("warehouse", "nameKey").where().eq("deleted", false)
			.eq("warehouse", SessionSearchUtil.searchWarehouse()).order("nameKey ascending").findList();
		List<MapVo> areas = new ArrayList<MapVo>();
		if (areaEntitys != null && !areaEntitys.isEmpty()) {
			for (Area areaEntity : areaEntitys) {
				MapVo area = new MapVo();
				area.key = String.valueOf(areaEntity.id);
				area.descripton = areaEntity.nameKey;
				areas.add(area);
			}
		}
		return ok(Json.toJson(areas));
	}

	/**
	 * 
	 * @param area id
	 * @return
	 */
	public static Result initBin(String id) {
		System.out.println("^^^^^^^^^^^^you have in getBins method^^^^^^^^^^^" + id);

		List<Bin> binEntitys = Ebean.find(Bin.class).fetch("area").where().eq("deleted", false).eq("area.id", id).findList();
		List<MapVo> bins = new ArrayList<MapVo>();
		if (binEntitys != null && !binEntitys.isEmpty()) {
			for (Bin binEntity : binEntitys) {
				MapVo bin = new MapVo();
				bin.key = String.valueOf(binEntity.id);
				bin.descripton = binEntity.nameKey;
				bins.add(bin);

			}
		}
		//System.out.println("^^^^^^^^^^^^tobins messages^^^^^^  " + play.libs.Json.toJson(bins));
		return ok(play.libs.Json.toJson(bins));
	}

	/**
	 * 
	 * @param 
	 * @return
	 */
	public static Result initEmployee() {
		List<EmployeeWarehouse> employWhs = Ebean.find(EmployeeWarehouse.class).fetch("employee").fetch("employee.employeeType")
			.fetch("warehouse", "nameKey").where().where().eq("deleted", false).eq("warehouse", SessionSearchUtil.searchWarehouse())
			.eq("employee.employeeType.typeCode", CodeKey.EMPLOY_TYPE_FORKLIFTDRIVE.toString()).eq("employee.deleted", false).order("employee.employeeName ascending")
			.findList();
		List<MapVo> areas = new ArrayList<MapVo>();
		if (employWhs != null && !employWhs.isEmpty()) {
			for (EmployeeWarehouse ew : employWhs) {
				MapVo area = new MapVo();
				area.key = String.valueOf(ew.employee.id);
				area.descripton = ew.employee.employeeName;
				areas.add(area);
			}
		}
		return ok(play.libs.Json.toJson(areas));
	}

}
