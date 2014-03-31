package controllers.inbound;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import models.Area;
import models.Bin;
import models.Code;
import models.Execution;
import models.ItemPolicy;
import models.Material;
import models.MaterialUom;
import models.Order;
import models.OrderItem;
import models.Owner;
import models.Plan;
import models.PlanItem;
import models.PlanItemDetail;
import models.StorageType;
import models.TimingPolicy;
import models.vo.Result.ResultVo;
import models.vo.inbound.BinVo;
import models.vo.inbound.MaterialVo;
import models.vo.inbound.PiPlanItemVo;
import models.vo.inbound.PiStatusVo;
import models.vo.inbound.PiVo;
import models.vo.inbound.StorageTypeVo;
import models.vo.inbound.TimingVo;
import models.vo.inbound.UOMVo;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.db.ebean.Transactional;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http.RequestBody;
import play.mvc.Result;
import play.mvc.With;
import security.NoUserDeadboltHandler;
import utils.CodeUtil;
import utils.CrudUtil;
import utils.DateUtil;
import utils.ExtUtil;
import utils.SessionSearchUtil;
import utils.Service.DataExchangePlatform;
import views.html.inbound.piManagement;
import action.Menus;
import be.objectify.deadbolt.java.actions.SubjectPresent;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.SqlRow;

/**
 * 
* <p>Project: CloudWMS</p> 
* <p>Title: OrderController.java</p> 
* <p>Description: </p> 
* <p>Copyright (c) 2013 Wilmar Consultancy Services</p>
* <p>All Rights Reserved.</p>
* @author <a href="mailto:chenaihong@wcs-global.com">Chen Aihong</a>
 */
@SubjectPresent(handler = NoUserDeadboltHandler.class)
@With(Menus.class)
public class OrderController extends Controller {
	
	private static final Logger logger = LoggerFactory.getLogger(OrderController.class);

	//static Warehouse warehouse = SessionSearchUtil.searchWarehouse();
	//static String warehouseId = warehouse.id.toString();
	static String receivingPlan = "T001"; // T001表示收货的plan
	static String sourceTypeDownload = "T001"; // T001表示SAP Download
	static String sourceTypeManul = "T002"; // T002表示手工录入
	static String piStatus = "S000";// 表示新建的PI
	static String downloadStatus = "S003"; // 表示已经下载
	static String pi = "T001";// 表示收货，cargoPlan表示发货
	static String orderTypeOfCargo = "T004";
	static String itemStatus = "S000";
	static String confirmed = "S001";
	static String executing = "S002";
	static String executed = "S003";
	static boolean wrong = false;
	static String loadingBay = "Loading Bay";
	static String receiving = "Receiving";

	/**
	 * @return 数据显示在dataTable中
	 */
	public static Result index() {
		return ok(piManagement.render(""));
	}

	// =================下拉列表的显示========================

	public static Result initPiStatus() {
		List<PiStatusVo> piStatusVos = new ArrayList<PiStatusVo>();
		List<Code> codes = CodeUtil.getOrderStatus();
		for (Code code : codes) {
			PiStatusVo piStatusVo = new PiStatusVo();
			piStatusVo.initCode(code);
			piStatusVos.add(piStatusVo);
		}
		return ok(play.libs.Json.toJson(new ResultVo(piStatusVos)));
	}
	
	public static Result initPiSource(){
		List<Code> codes = CodeUtil.getOrderStatus();
		return ok(play.libs.Json.toJson(new ResultVo(codes)));
	}

	/**
	 * 初始化storageType下拉列表中的数据
	 * 
	 * @return
	 */
	public static Result initStorageType() {
		
		List<StorageTypeVo> storageTypeVos = new ArrayList<StorageTypeVo>();
		// warehouse id待定
		List<StorageType> storageTypes = StorageType.find().where().eq("warehouse.id", SessionSearchUtil.searchWarehouse().id.toString()).eq("deleted", false)
				.eq("warehouse.deleted", false).findList();
		for (StorageType storageType : storageTypes) {
			if (!storageType.nameKey.equals(receiving) && !storageType.nameKey.equals(loadingBay)) {
				StorageTypeVo storageTypeVo = new StorageTypeVo();
				storageTypeVo.inStorageTypeVo(storageType);
				storageTypeVos.add(storageTypeVo);
			}
		}
		return ok(play.libs.Json.toJson(new ResultVo(storageTypeVos)));
	}

	/**
	 * 初始化生产线下拉列表中的数据(生产线为特殊的Bin)
	 * 
	 * @return
	 */
	public static Result initproduciontLine() {
		List<BinVo> binVos = new ArrayList<BinVo>();
		// warehouse id待定
		List<Bin> bins = Bin.find().where().eq("area.warehouse.id", SessionSearchUtil.searchWarehouse().id.toString()).eq("area.storageType.nameKey", receiving).eq("deleted", false)
				.findList();
		for (Bin bin : bins) {
			BinVo binVo = new BinVo();
			binVo.id = String.valueOf(bin.id);
			binVo.nameKey = bin.nameKey;
			binVos.add(binVo);
		}
		return ok(play.libs.Json.toJson(new ResultVo(binVos)));
	}

	public static Result listMaterial() {// 物料下拉列表的显示
		ArrayList<MaterialVo> materialVos = new ArrayList<MaterialVo>();
		Owner owner = new Owner();
		owner = Owner.find().where().eq("warehouse.id", SessionSearchUtil.searchWarehouse().id.toString()).eq("deleted", false).findUnique();
		List<Material> materials = new ArrayList<Material>();
		if (owner != null) {
			materials = Material.find().where().eq("owner.id", owner.id.toString()).eq("deleted", false).findList();
		}
		for (Material material : materials) {
			if(MaterialCheck(material.id.toString())){
				MaterialVo materialVo = new MaterialVo();
				materialVo.initMaterial(material);
				materialVos.add(materialVo);
			}
		}
		return ok(play.libs.Json.toJson(new ResultVo(materialVos)));
	}

	public static Result listMaterialUom(String materialId) {// 物料单位下拉列表的显示（该单位是某物料的单位）
		List<UOMVo> uomVos = new ArrayList<UOMVo>();
		List<MaterialUom> materialUoms = new ArrayList<MaterialUom>();
		if (materialId != null && !"".equals(materialId)) {
			materialUoms = Ebean.find(MaterialUom.class).fetch("material").where().eq("material.id", materialId).eq("deleted", false)
					.eq("material.deleted", false).findList();
		} else {
			materialUoms = MaterialUom.find().where().eq("deleted", false).findList();
		}
		for (MaterialUom materialUom : materialUoms) {
			UOMVo uomVo = new UOMVo();
			uomVo.inMaterialUom(materialUom);
			uomVos.add(uomVo);
		}
		return ok(play.libs.Json.toJson(new ResultVo(uomVos)));
	}

	public static Result listMaterialUomBase(String materialId) {// 物料单位下拉列表的显示（该单位是某物料的单位）
		List<UOMVo> uomVos = new ArrayList<UOMVo>();
		List<MaterialUom> materialUoms = new ArrayList<MaterialUom>();
		if (materialId != null && !"".equals(materialId)) {
			materialUoms = MaterialUom.find().where().eq("material.id", materialId).eq("baseUom", true).eq("deleted", false)
					.eq("material.deleted", false).findList();
		} else {
			materialUoms = MaterialUom.find().where().eq("deleted", false).findList();
		}
		for (MaterialUom materialUom : materialUoms) {
			UOMVo uomVo = new UOMVo();
			uomVo.inMaterialUom(materialUom);
			uomVos.add(uomVo);
		}
		return ok(play.libs.Json.toJson(new ResultVo(uomVos)));
	}

	public static Result listAllMaterialUom() {// 所有单位下拉列表的显示
		List<UOMVo> allUomVos = new ArrayList<UOMVo>();
		List<MaterialUom> materialUoms = new ArrayList<MaterialUom>();
		materialUoms = MaterialUom.find().where().eq("deleted", false).eq("material.deleted", false).findList();
		for (MaterialUom materialUom : materialUoms) {
			UOMVo uomVo = new UOMVo();
			uomVo.inMaterialUom(materialUom);
			allUomVos.add(uomVo);
		}
		return ok(play.libs.Json.toJson(new ResultVo(allUomVos)));
	}

	public static MaterialUom getMaterialUomAdd(String id) { // materialUom应该根据所登录的用户得到的，现在默认是数据库的第一条
		List<MaterialUom> materialUomList = new ArrayList<MaterialUom>();
		materialUomList = MaterialUom.find().where().eq("deleted", false).eq("material.id", id).findList();
		if (materialUomList.size() > 0) {
			return materialUomList.get(0);
		}
		return null;
	}

	public static Owner getOwner() { // owner应该根据所登录的用户得到的
		return Owner.find().where().eq("warehouse.id", SessionSearchUtil.searchWarehouse().id.toString()).eq("deleted", false).findUnique();
	}

	public static Material getMaterialAdd() { // material应该根据所登录的用户得到的，现在默认是数据库的第一条
		List<Material> materialList = new ArrayList<Material>();
		materialList = Material.find().where().eq("deleted", false).findList();
		if (materialList.size() > 0) {
			return materialList.get(0);
		}
		return null;
	}

	/**
	 * @return 
	 * 创建Pi，其中校验PINo，SGPiNo不能重复
	 */
	@Transactional
	public static Result save() {
		logger.info("==========save==begin==============");
		PiVo orderItemVo = new PiVo();
		Order order = new Order();
		MaterialUom materialUom = new MaterialUom();
		OrderItem orderItem = new OrderItem();
		RequestBody body = request().body();
		if (body.asJson() != null) {
			orderItemVo = Json.fromJson(body.asJson(), PiVo.class);
			StringBuffer sb = new StringBuffer("Save Failed:Please check");
			sb = ckeckOut(orderItemVo, sb);// 鍚庡彴楠岃瘉鏁版嵁鐨勬纭�
			if (wrong) {
				return ok(play.libs.Json.toJson(new ResultVo("error",sb.toString())));
			}
			Material material = Material.find().where().eq("id", orderItemVo.materialVo.materialId).findUnique();// 鏍规嵁materialId鑾峰緱鐗╂枡
			if (material != null) {
				if (orderItemVo.uomVo.id != null && !"".equals(orderItemVo.uomVo.id))
					materialUom = MaterialUom.find().where().eq("id", orderItemVo.uomVo.id).findUnique();
				orderItem.material = material;
			}
			if (materialUom != null) {
				orderItem.settlementUom = materialUom;
				// orderItem.materialUom = materialUom;
			}
			List<Order> orderTemp = Order.find().where().eq("internalOrderNo", orderItemVo.piNo).eq("orderType", pi).eq("deleted", false).findList();// 鐢ㄤ簬鏍￠獙PINo鏄惁鏈夐噸澶�
			//List<Order> orderTemp2 = Order.find().where().eq("contractNo", orderItemVo.contractNo).eq("orderType", pi).eq("deleted", false).findList();// 鐢ㄤ簬鏍￠獙SGPiNo鏄惁鏈夐噸澶�
			if (orderTemp.size() > 0) {
				return ok(play.libs.Json.toJson(new ResultVo("error","Create Failed,The PI No has exists!")));
			}
			/*if (orderTemp2.size() > 0) {
				return badRequest("Create Failed,The SG PI No has exists!");
			}*/
			orderItemVo.getOrder(order);// 淇濆瓨 order鐩稿叧淇℃伅
			order.orderStatus = piStatus;
			order.warehouse = SessionSearchUtil.searchWarehouse();// 鑾峰彇褰撳墠鐧诲綍鐢ㄦ埛鎵�湪鐨剋arehouse
			order.sourceType = orderItemVo.sourceType;//sourceTypeManul; // 鎵嬪伐褰曞叆鐨凾ype涓篢002
			order.orderType = pi;
			CrudUtil.save(order);

			orderItemVo.getOrderItem(orderItem);// 淇濆瓨orderItem鐩稿叧淇℃伅
			orderItem.order = order;
			orderItem.ext = orderItemVo.returnExt();
			orderItem.itemStatus = piStatus;
			CrudUtil.save(orderItem);
			logger.info("=============save=========end===============" + orderItem.ext);
		}
		return ok(play.libs.Json.toJson(new ResultVo("success","The Pi has been created successfully!")));
	}

	/**
	 * @return 编辑
	 * 更新PI信息，其中校验1，PINo是否已经存在.2，SGPINo是否已经存在.3，该PI是否已经被拆分成Plan.4，该PI是否已经被创建了CargoPlan
	 */
	@Transactional
	public static Result update() {
		logger.info("================update===begin===============");
		RequestBody body = request().body();
		PiVo vo = Json.fromJson(body.asJson(), PiVo.class);
		Order order = Order.find().byId(vo.id.toString());
		List<OrderItem> orderItems = OrderItem.find().where().eq("order_id", order.id).findList();
//		List<Plan> plans = Plan.find().where().eq("order", order).eq("deleted", false).eq("order.deleted", false).findList();
		/*List<Order> orderList = Order.find().where().eq("internalOrderNo", order.internalOrderNo).eq("orderType", orderTypeOfCargo)
				.eq("deleted", false).findList();
		if (orderList.size() > 0) {// 如果该PI被创建成了CargoPlan则不能被编辑
			return ok(play.libs.Json.toJson(new ResultVo("error","Update Failed,Please check whether the Pi has been created CargoPlan!")));
		}*/
//		if (plans.size() > 0) { // 如果已经拆分成plan则不能编辑
//			return ok(play.libs.Json.toJson(new ResultVo("error","Update Failed,Please check whether has been split into the plan !")));
//		} else {
			List<Order> orderTemp = Order.find().where().eq("internalOrderNo", vo.piNo).eq("orderType", pi).eq("deleted", false).findList();
			//List<Order> orderTemp2 = Order.find().where().eq("contractNo", vo.contractNo).eq("orderType", pi).eq("deleted", false).findList();
			if (orderTemp.size() > 0 && !(vo.id.toString()).equals(orderTemp.get(0).id.toString())) {
				return ok(play.libs.Json.toJson(new ResultVo("error","Update Failed:The PI No has exists !")));
			}
			/*if (orderTemp2.size() > 0 && !(vo.id.toString()).equals(orderTemp2.get(0).id.toString())) {
				return badRequest("Update Failed:The SG PI No has exists !");
			}*/
			StringBuffer sb = new StringBuffer("Update Failed: Please check ");
			sb = ckeckOut(vo, sb);
			if (wrong) {
				return ok(play.libs.Json.toJson(new ResultVo("error",sb.toString())));
			}
			if (order != null) {
				vo.getOrder(order);
				order.orderStatus = itemStatus;
				order.orderType = pi;
				CrudUtil.update(order);
			}
			if (orderItems.size() > 0) {
				Material material = Material.find().where().eq("id", vo.materialVo.materialId).findUnique();
				MaterialUom materialUom = new MaterialUom();
				if (vo.uomVo.id != null) {
					materialUom = MaterialUom.find().where().eq("id", vo.uomVo.id).eq("deleted", false).findUnique();
				}
				for (OrderItem orderItem : orderItems) {
					orderItem.material = material;
					vo.getOrderItem(orderItem);
					orderItem.itemStatus = piStatus;
					orderItem.remarks = vo.remarks;
					orderItem.order = order;
					orderItem.ext = vo.returnExt();
					if (materialUom != null) {
						orderItem.settlementUom = materialUom;
						orderItem.materialUom = materialUom;
					}
					CrudUtil.update(orderItem);
				}
			}
//		}
		return ok(play.libs.Json.toJson(new ResultVo(vo)));

	}

	/**
	 * @param id
	 * @return 删除
	 * 1、校验该Pi是否被拆成了Plan；3、校验该PI是否被创建了CargoPlan
	 * 4、如果判断该Pi是通过SAP下载的，则如果将其删除，需要修改被下载Pi的状态
	 */
	@Transactional
	public static Result delete(String id) {
		logger.info("================delete===begin=================");
		Order order = Order.find().where().eq("id", id).eq("deleted", false).findUnique();
		List<OrderItem> orderItems = OrderItem.find().where().eq("order.id", id).eq("deleted", false).eq("order.deleted", false).findList();
		List<Plan> plans = Plan.find().where().eq("order.id", id).eq("deleted", false).eq("order.deleted", false).findList();
		List<ItemPolicy> itemPolicies = ItemPolicy.find().where().eq("orderItem.order.id", id).eq("orderItem.deleted", false).eq("deleted", false)
				.findList();
		/*List<Order> orderList = Order.find().where().eq("internalOrderNo", order.internalOrderNo).eq("orderType", orderTypeOfCargo)
				.eq("deleted", false).findList();
		if (orderList.size() > 0) {
			return ok(play.libs.Json.toJson(new ResultVo("error","Deleted Failed,Please check whether the Pi has been created CargoPlan!")));
		}*/
		if (plans.size() > 0) {
			return ok(play.libs.Json.toJson(new ResultVo("error","Delete Failed,Please check whether it has been split into the plan!")));
		}/*
		 * else if (itemPolicies.size() > 0) { return badRequest(
		 * "Delete Failed,Please check whether it has been assigned to the cold storage!"
		 * ); } //2、校验该Pi是否被分配至冷库；已经根据需求修改为：若被分配至冷库这该PI仍然可以删除
		 */else {
			for (OrderItem orderItem : orderItems) {
				/*if (sourceTypeDownload.equals(order.sourceType)) {// T001表示SAPDownload
					ExternalOrderItem extOrderItem = ExternalOrderItem.find().where().eq("id", orderItem.externalOrderItem.id.toString())
							.eq("deleted", false).findUnique();
					extOrderItem.itemStatus = itemStatus;
					extOrderItem.externalOrder.orderStatus = itemStatus;
					CrudUtil.update(extOrderItem);
					CrudUtil.update(extOrderItem.externalOrder);
				}*/
				orderItem.deleted = true;
				CrudUtil.update(orderItem);
			}
			order.deleted = true;
			CrudUtil.update(order);
		}
		
		if (itemPolicies.size() > 0) {
			for (ItemPolicy itemPolicy : itemPolicies) {
				List<TimingPolicy> timingPolicies = new ArrayList<TimingPolicy>();
				timingPolicies = TimingPolicy.find().where().eq("itemPolicy.id", itemPolicy.id.toString()).eq("deleted", false)
						.eq("itemPolicy.deleted", false).findList();
				if (timingPolicies.size() > 0) {
					for (TimingPolicy timingPolicy : timingPolicies) {
						timingPolicy.deleted = true;
						CrudUtil.update(timingPolicy);
					}
				}
				itemPolicy.deleted = true;
				CrudUtil.update(itemPolicy);
			}
		}
		
		String result = DataExchangePlatform.deleteOrder(order);
		logger.info("================delete===end=================");
		return ok(play.libs.Json.toJson(new ResultVo("success","The Pi has been deleted successfully ! "+result)));
	}

	/**
	 * @return 根据选择的条件模糊查询数据
	 */
	@Transactional
	public static Result list() {
		logger.info("================search===begin=================");
		RequestBody body = request().body();
		PiVo vo = new PiVo();
		if (body.asJson() != null) {
			vo = Json.fromJson(body.asJson(), PiVo.class);
		}
		Date fromdate = DateUtil.strToDate("1900-01-01", "yyyy-MM-dd");
		Date todate = DateUtil.strToDate("2100-01-01", "yyyy-MM-dd");

		if (vo.piDateFrom == null) {
			vo.piDateFrom = fromdate;
		}
		if (vo.piDateTo == null) {
			vo.piDateTo = todate;
		}
		if (vo.piDateFrom != null && vo.piDateTo != null) {
			if (vo.piDateFrom.after(vo.piDateTo)) {
				return ok(play.libs.Json.toJson(new ResultVo("error","fromDate can not be greater than toDate,Please correct!")));
			}
		}
		
		StringBuffer sb = new StringBuffer("select t_oi.*,t_o.order_status,t_o.source_type, "
				+" t_o.order_timestamp,t_o.internal_order_no,t_o.contract_no,"
				+" t_smu.uom_code,  "
				+" t_m.material_code,t_m.material_name,t_m.net_weight"
				+" from t_order_item t_oi inner join t_order t_o on t_o.id=t_oi.order_id ");
		sb.append(" left join t_material_uom t_smu on t_smu.id=t_oi.settlement_uom_id ");
		sb.append(" left join t_material t_m on t_m.id=t_oi.material_id ");
		
		sb.append(" where 1=1 ");
		
		sb.append(" and t_oi.deleted=false and t_o.deleted=false and t_o.order_type = '"+pi+"' ");
		sb.append(" and  t_o.order_timestamp between '"+DateUtil.dateToStrShort(vo.piDateFrom) + " 00:00:00'"+
				 "  and '"+DateUtil.dateToStrShort(vo.piDateTo) + " 23:59:59'");
		
		if(StringUtils.isNotBlank(vo.piNo)){
			sb.append(" and t_o.internal_order_no like '%" + vo.piNo + "%'");
		}
		if(StringUtils.isNotBlank(vo.contractNo)){
			sb.append(" and t_o.contract_no like '%" + vo.contractNo + "%'");
		}
		if(StringUtils.isNotBlank(vo.piStatus)){
			sb.append(" and t_o.order_status = '"+vo.piStatus+"'");
		}
		
		if(null!=vo.reqShipmentDate){
			sb.append(" and t_o.ext::hstore->'reqShipmentDate'='"+vo.reqShipmentDate+"'");
		}
		
		sb.append(" order by t_o.updated_at desc");
		
		List<SqlRow> lists = Ebean.createSqlQuery(sb.toString()).findList();
		List<PiVo> orderItemList = null;
		if(null!=lists&&lists.size()>0){
			orderItemList = new ArrayList<PiVo>(lists.size());
			for (SqlRow row : lists) {
				PiVo piVo = new PiVo();
				piVo.id = row.getString("order_id");//订单ID
				List<Code> codes = Code.find().where().eq("codeKey",row.get("order_status")).eq("codeCat.catKey", "ORDER_STATUS")
						.eq("codeCat.warehouse", SessionSearchUtil.searchWarehouse()).eq("deleted", false).eq("codeCat.deleted", false).findList();
				if (codes.size() > 0) {
					piVo.piStatus = codes.get(0).nameKey;
				}
				List<Code> sourceTypes = Code.find().where().eq("codeKey",row.get("source_type")).eq("codeCat.catKey", "SOURCE_TYPE")
						.eq("codeCat.warehouse", SessionSearchUtil.searchWarehouse()).eq("deleted", false).eq("codeCat.deleted", false).findList();
				piVo.sourceType = row.getString("source_type");
				if (sourceTypes.size() > 0) {
					piVo.piSource = sourceTypes.get(0).nameKey;
				}
				piVo.piDate = DateUtil.timestampToDate(row.getTimestamp("order_timestamp"));
				piVo.ts = DateUtil.dateToStrShort(piVo.piDate);
				piVo.piNo = row.getString("internal_order_no");
				piVo.contractNo = row.getString("contract_no");
				
				HashMap<String, String> ext = ExtUtil.unapply(row.getString("ext"));
				if (StringUtils.isNotBlank(row.getString("settlement_uom_id"))) {
					UOMVo uomVo = new UOMVo();
					uomVo.id = row.getString("settlement_uom_id");//settlement
					uomVo.uomCode = row.getString("uom_code");
					piVo.uomVo = uomVo;
					piVo.uomVo.id = uomVo.id;
					piVo.qtyToBeShipUom = getMaterialUom(ext.get("qtyToBeShipUom")); 
					piVo.netWeightUom = getMaterialUom(ext.get("netWeightUom"));
					piVo.salesContractQtyUom = getMaterialUom(ext.get("salesContractQtyUom"));
					piVo.qtyPerPalletUom = getMaterialUom(ext.get("qtyPerPalletUom"));
				}
				
				if (ext.get("reqShipmentDate") != null && !"".equals(ext.get("reqShipmentDate"))) {
				    try{
					piVo.reqShipmentDate = DateUtil.strToDate(ext.get("reqShipmentDate"), "yyyy-MM-dd");
				    }catch(IllegalArgumentException e){
				        piVo.reqShipmentDate=new Date(Long.valueOf(ext.get("reqShipmentDate")));
		            }
				}
				String extraQty = ext.get("extraQty");
				BigDecimal settlementQty = row.getBigDecimal("settlement_qty");
				if (settlementQty != null && !"".equals(settlementQty)) {
					piVo.settlementQty = (settlementQty.setScale(2,RoundingMode.HALF_DOWN)).toString();
					if (StringUtils.isNotEmpty(extraQty)) {
						NumberFormat nf = new DecimalFormat("0.00");
						String result = nf.format(Double.parseDouble(String.valueOf(settlementQty.setScale(2).subtract(
								new BigDecimal(Double.parseDouble(extraQty))))));
						piVo.piQty = result;
						piVo.extraQty = nf.format(Double.valueOf(extraQty));
					} else {
						piVo.piQty = String.valueOf(settlementQty.setScale(2,RoundingMode.HALF_DOWN));
					}
				}
				
				String material_id = row.getString("material_id");
				if (StringUtils.isNotBlank(material_id)&&MaterialCheck(material_id)) {
					MaterialVo materialVo = new MaterialVo();
					materialVo.materialCode = row.getString("material_code");
					materialVo.materialName = row.getString("material_name");
					BigDecimal netWeight = row.getBigDecimal("net_weight");
					if (null!=netWeight){
						materialVo.netWeight = String.valueOf(netWeight.setScale(2));
					}
					materialVo.materialId = material_id;
					if (StringUtils.isNotBlank(materialVo.materialCode) && StringUtils.isNotBlank(materialVo.materialName)){
						materialVo.codeAndDesc = materialVo.materialCode + "|" + materialVo.materialName;
					}
					piVo.materialVo = materialVo;
				}
				
				piVo.setExt(row.getString("ext"));
//				HashMap<String, String> orderItemExt = ExtUtil.unapply(row.getString("ext"));

				piVo.remarks = row.getString("remarks");
				BigDecimal minPercent = row.getBigDecimal("min_percent");
				if (null!=minPercent) {
					piVo.minPercent = String.valueOf(minPercent.setScale(2));
				}
				BigDecimal maxPercent = row.getBigDecimal("max_percent");
				if (null!=maxPercent) {
					piVo.maxPercent = String.valueOf(maxPercent.setScale(2));
				}
				// if (orderItem.qty != null && !"".equals(orderItem.qty)) {
				// piVo.qtyPerPallet = (orderItem.qty.setScale(2)).toString();
				// }

				// if (orderItem.order.sourceType.equals(sourceTypeDownload)) {
				// piVo.qtyPerPallet = "40";// 如果是SAP Download则默认策略为40；//待确定
				// }
				List<Plan> plans = Plan.find().where().eq("order.id",piVo.id).eq("deleted", false).eq("order.deleted", false).findList();
				if (plans.size() > 0) { // 如果已经拆分成plan则不能编辑
					piVo.hasPlan = true;
				} else {
					piVo.hasPlan = false;
				}
				/*List<PlanItem> planItems = PlanItem.find().where().eq("orderItem.order.internalOrderNo",piVo.piNo )
										.eq("orderItem.order.orderType", orderTypeOfCargo)
										.eq("deleted", false)
										.eq("orderItem.deleted", false)
										.eq("orderItem.order.deleted", false)
										.eq("orderItem.order.warehouse.id", SessionSearchUtil.searchWarehouse().id)
										.findList();
				if (planItems.size() > 0) {
					piVo.hasCargo = true;
				} else {
					piVo.hasCargo = false;
				}*/
				orderItemList.add(piVo);
			}
		}
		return ok(play.libs.Json.toJson(new ResultVo(orderItemList)));
	}
	public static boolean MaterialCheck(String id){
		 List<MaterialUom> uoms = Ebean.find(MaterialUom.class).fetch("material").where().eq("material.id", id).eq("deleted", false)
					.eq("material.deleted", false).eq("baseUom",true).findList();
			if(uoms.size()>0){
				return true;
			}else{
				return false;
			}
	}
	
	private static UOMVo getMaterialUom(String mUom){
		UOMVo salesContractQtyUomVo = null;
		if(StringUtils.isNotEmpty(mUom)){
			MaterialUom salesContractQtyUom = MaterialUom.find().where().eq("id",mUom).eq("deleted", false).findUnique();
			if (salesContractQtyUom != null) {
				salesContractQtyUomVo = new UOMVo();
				salesContractQtyUomVo.id = String.valueOf(salesContractQtyUom.id);
				salesContractQtyUomVo.uomCode = salesContractQtyUom.uomCode;
			}
		}
		return salesContractQtyUomVo;
	}

	// ======================================Production===Details======================
	/**
	 * @param id
	 * @return
	 * 删除Plan的时候，如果该Plan已经创建了PlanDetail则不能被删除
	 */
	@Transactional
	public static Result deletePlan(String id) {
		PlanItem planItem = PlanItem.find().fetch("plan").where().eq("id", id).eq("deleted", false).eq("plan.deleted", false).findUnique();
		List<PlanItemDetail> planItemDetails = PlanItemDetail.find().where().eq("planItem", planItem).eq("deleted", false).findList();
		if (planItemDetails.size() > 0) {// 删除时判断是否已经有planItemDetail
			return ok(play.libs.Json.toJson(new ResultVo("error","Delete Failed!,Please check whether it has been executed")));
		}
		planItem.deleted = true;
		CrudUtil.update(planItem);
		Plan plan = Plan.find().byId(planItem.plan.id.toString());
		plan.deleted = true;
		CrudUtil.update(plan);
		List<PlanItem> planItems = new ArrayList<PlanItem>();
		planItems = PlanItem.find().where().eq("orderItem.id", planItem.orderItem.id.toString()).eq("planType", receivingPlan).eq("deleted", false)
				.findList();
		System.out.println("***********planItems.size()**************" + planItems.size());
		System.out.println("**************************" + planItem.orderItem.itemStatus);
		if (planItems.size() == 0) {
			planItem.orderItem.itemStatus = itemStatus;
			planItem.orderItem.order.orderStatus = itemStatus;
		}
		CrudUtil.update(planItem.orderItem.order);
		CrudUtil.update(planItem.orderItem);
		return ok(play.libs.Json.toJson(new ResultVo("success","The plan has been deleted successfully !")));
	}

	/**
	 * @param id
	 * @return
	 * 删除时需要判断改timingPolicy是否已经有transferPlan
	 */
	@Transactional
	public static Result deleteTiming(String id) {// 删除时需要判断改timingPolicy是否已经有transferPlan
		logger.info("===========deleteTiming==begin================");
		TimingPolicy timingPolicy = TimingPolicy.find().fetch("itemPolicy").where().eq("id", id).eq("deleted", false).eq("itemPolicy.deleted", false)
				.findUnique();
		List<PlanItemDetail> planItemDetails = PlanItemDetail.find().fetch("planItem").fetch("planItem.orderItem").where()
				.eq("planItem.orderItem.id", timingPolicy.itemPolicy.orderItem.id.toString()).eq("deleted", false)
				.eq("planItem.orderItem.deleted", false).eq("planItem.deleted", false).findList();
		if (planItemDetails.size() > 0) {
			return ok(play.libs.Json.toJson(new ResultVo("error","Delete Failed!,Please Check whether it has been executed!")));
		} else {
			timingPolicy.deleted = true;
			timingPolicy.itemPolicy.deleted = true;
			CrudUtil.update(timingPolicy);
			CrudUtil.update(timingPolicy.itemPolicy);
			logger.info("=============deleteTiming==end================");
			return ok(play.libs.Json.toJson(new ResultVo("success","The timingPlan has been deleted successfully !")));
		}
	}

	/**
	 * @return 点击save按钮的时候 将拆分（编辑）的多条planItem保存至数据库，并做数量总和校验
	 * @throws IOException
	 * @throws JsonMappingException
	 * @throws JsonParseException
	 */
	@Transactional
	public static Result savePlan() {
		logger.info("=============savePlan===begin==============");
		JsonNode body = request().body().asJson();
		ObjectMapper mapper = new ObjectMapper();
		List<PiPlanItemVo> piList = null;
//		double allQty = 0;
		List<OrderItem> orderItems = new ArrayList<OrderItem>();
		try {
			piList = mapper.readValue(body, new TypeReference<List<PiPlanItemVo>>() {
			});
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
//		for (PiPlanItemVo planVo : piList) { // 获得总的数量
//			allQty = allQty + planVo.prodQty;
//		}
		try {
			orderItems = OrderItem.find().fetch("material").where().eq("order.id", piList.get(0).id).eq("deleted", false)
					.eq("material.deleted", false).findList();
		} catch (Exception e) {
			return ok(play.libs.Json.toJson(new ResultVo("error","Can not find data")));
		}
		for (OrderItem orderItem : orderItems) {
			double qtySumMore = 0;
			if (orderItem.minPercent != null && orderItem.maxPercent != null) {
				qtySumMore = orderItem.maxPercent.doubleValue() / 100 * orderItem.settlementQty.doubleValue();
			} else {
				qtySumMore = orderItem.settlementQty.doubleValue();
			}
//			if (allQty - qtySumMore > 0) {
//				DecimalFormat df = new DecimalFormat(".##");
//				String qty = df.format(allQty);
//				String sum = df.format(qtySumMore);
//				return ok(play.libs.Json.toJson(new ResultVo("error","Split Failed," + "  Qty:  " + qty + " exceeds  " + sum + "  !")));
//			} else {
				for (PiPlanItemVo planVo : piList) {
					Plan plan = null;
					PlanItem planItem = null;
					Order order = Order.find().byId(planVo.id);
					if (String.valueOf(planVo.prodQty) == null || "".equals(String.valueOf(planVo.prodQty))) {
						return ok(play.libs.Json.toJson(new ResultVo("error","Split Failed, prodQty can not be null!")));
					}
					if (planVo.haslive) {
						planItem = PlanItem.find().fetch("plan").where().eq("id", planVo.planId).eq("deleted", false).eq("plan.deleted", false)
								.findUnique();
						plan = planItem.plan;
					} else {
						plan = new Plan();
						planItem = new PlanItem();
					}
					Bin bin = Bin.find().where().eq("id", planVo.binVo.id).where().eq("area.warehouse.id", SessionSearchUtil.searchWarehouse().id.toString()).eq("area.deleted", false)
							.eq("deleted", false).findUnique();// 拆分orderItem时toBin默认为一个特殊的bin
					Area area = bin.area;
//							Area.find().where().eq("deleted", false).eq("warehouse.id", warehouseId).eq("storageType.nameKey", receiving)
//							.findUnique();
					planItem.orderItem = orderItem;// 保存planItem
					planItem.plan = plan;
					planItem.order = order;
					if (orderItem.maxPercent != null)
						planItem.maxPercent = orderItem.maxPercent;// 待确认
					if (orderItem.minPercent != null)
						planItem.minPercent = orderItem.minPercent;// 待确认
					if (area != null)
						planItem.toArea = area;
					if (bin != null)
						planItem.toBin = bin;
					if (planVo.piSku >= 0) {
						planItem.palnnedQty = BigDecimal.valueOf(planVo.piSku);
					}
					planItem.material = orderItem.material;
					planItem.planType = receivingPlan;
					planItem.planSubtype = receivingPlan;
					planItem.itemStatus = itemStatus;
					planVo.piNo = order.externalOrderNo;
					planItem.ext = planVo.returnBatchExt();
					// 校验:orderItem.settlementQty=prodQty的总和;
					plan.warehouse = SessionSearchUtil.searchWarehouse();
					plan.order = order;
					plan.planType = receivingPlan; // 保存plan, T001表示收货
					plan.planSubtype = receivingPlan;
					SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					String timesampDate = format.format(planVo.prodDate);
					try {
						plan.plannedTimestamp = Timestamp.valueOf(timesampDate);// 生产日期
						planItem.plannedExecutionAt= Timestamp.valueOf(timesampDate);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						return ok(play.libs.Json.toJson(new ResultVo("error","prodDate can not be null !")));
					}
					plan.planStatus = piStatus; // 表示新建
					if (planVo.haslive) {
						CrudUtil.update(plan);
					} else {
						CrudUtil.save(plan);
					}
					MaterialUom fromMaterialUom = MaterialUom.find().where().eq("uomCode", planVo.uom).eq("material.id", orderItem.material.id)
							.eq("deleted", false).findUnique();
					
					MaterialUom toMaterialUom = MaterialUom.find().where().eq("uomCode", planVo.skuUom).eq("material.id", orderItem.material.id)
							.eq("deleted", false).findUnique();
					
					if (fromMaterialUom != null) {
						planItem.fromMaterialUom = fromMaterialUom;
					}
					if (toMaterialUom != null) {
						planItem.toMaterialUom = toMaterialUom;
					}

					planItem.materialUom = toMaterialUom;
					if (planVo.haslive) {
						CrudUtil.update(planItem);
					} else {
						CrudUtil.save(planItem);
					}
				}
//			}
			List<PlanItem> planItems = new ArrayList<PlanItem>();
			planItems = PlanItem.find().where().eq("orderItem.id", orderItem.id.toString()).eq("planType", receivingPlan).eq("deleted", false)
					.findList();
			if (planItems.size() > 0 && (itemStatus.equals(orderItem.itemStatus)) && itemStatus.equals(orderItem.itemStatus)) {
				orderItem.itemStatus = confirmed;
				orderItem.order.orderStatus = confirmed;
			}
			CrudUtil.update(orderItem.order);
			CrudUtil.update(orderItem);

		}
		logger.info("=============savePlan===end==============");
		return ok(play.libs.Json.toJson(new ResultVo("success","The Pi has been splited successfully !")));
	}

	/**
	 * @param id
	 * @return
	 * 列表展示已经被拆分的Plan
	 */
	public static Result listPlan(String id) {
		logger.info("================listPlan===begin=================");
		Order order = Order.find().byId(id);
		List<OrderItem> orderItems = new ArrayList<OrderItem>();
		orderItems = OrderItem.find().where().eq("order.id", id).findList();
		List<PiPlanItemVo> planVos = new ArrayList<PiPlanItemVo>();
		for (OrderItem orderItem : orderItems) {
			List<PlanItem> planItems = new ArrayList<PlanItem>();
			planItems = PlanItem.find().fetch("plan").fetch("batch").fetch("materialUom").fetch("fromMaterialUom").fetch("toMaterialUom").where()
					.eq("orderItem.order.id", id).eq("planType", receivingPlan).eq("deleted", false).eq("orderItem.deleted", false).findList();
			if (planItems.size() > 0) {
				for (PlanItem paItem : planItems) {
					PiPlanItemVo vo = new PiPlanItemVo();
					BinVo binVo = new BinVo();
					Bin bin = new Bin();
					List<PlanItemDetail> planItemDetails = PlanItemDetail.find().where().eq("planItem", paItem).eq("deleted", false)
							.eq("planItem.deleted", false).findList();
					if (planItemDetails.size() > 0) {// 判断是否已经有planItemDetail
						vo.hasPlan = true;
					}
					vo.piStatus = CodeUtil.getOrderStatus(order.orderStatus);
					vo.planId = String.valueOf(paItem.id);
					HashMap<String, String> ext = ExtUtil.unapply(paItem.ext);
					vo.splitProduction = ext.get("splitProduction");
					vo.prodDate = DateUtil.timestampToDate(paItem.plan.plannedTimestamp);
					bin = Bin.find().where().eq("id", paItem.toBin.id).eq("deleted", false).findUnique();
					vo.piSku = paItem.palnnedQty.doubleValue();
					binVo.id = String.valueOf(bin.id);
					binVo.nameKey = bin.nameKey;
					vo.binVo = binVo;
					vo.uom = paItem.fromMaterialUom.uomCode;
					vo.skuUom = paItem.toMaterialUom.uomCode;
					BigDecimal bg = new BigDecimal(returnComparing(vo.uom, vo.skuUom,String.valueOf(orderItem.material.id)) * vo.piSku);
			        double f = bg.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
					vo.prodQty = f;
					vo.id = order.id.toString();
					vo.haslive = true;
					planVos.add(vo);
				}
			}
		}
		logger.info("================listPlan===end=================");
		return ok(play.libs.Json.toJson(new ResultVo(planVos)));
	}

	/**
	 * @return
	 * 单位换算
	 */
	public static Result getPiSku() {
		RequestBody body = request().body();
		if (body.asJson() != null) {
			PiPlanItemVo vo = Json.fromJson(body.asJson(), PiPlanItemVo.class);
			if (vo.uom != null && vo.skuUom != null) {
				OrderItem orderItem = OrderItem.find().fetch("material").where().eq("order.id", vo.id).eq("deleted", false).findUnique();
				if (orderItem.material == null) {
					return ok(play.libs.Json.toJson(new ResultVo("error","Can not find material")));
				}
				return ok(play.libs.Json.toJson(new ResultVo(returnComparing(vo.uom, vo.skuUom, orderItem.material.id.toString()))));
			}
		}
		return ok(play.libs.Json.toJson(new ResultVo("error","failed!")));
	}

	/**
	 * @param uom
	 * @param sku
	 * @param materialId
	 * @return
	 * 获得单位的换算关系
	 */
	public static double returnComparing(String uom, String sku, String materialId) {
		MaterialUom materialUom = MaterialUom.find().where().eq("uomCode", uom).eq("material.id", materialId).eq("deleted", false).findUnique();
		MaterialUom materialUomSku = MaterialUom.find().where().eq("uomCode", sku).eq("material.id", materialId).eq("deleted", false).findUnique();
		if (materialUom != null && materialUomSku != null) {
			double comparing = (materialUom.qtyOfBaseDenom.doubleValue() / materialUom.qtyOfBaseNum.doubleValue())
					/ (materialUomSku.qtyOfBaseDenom.doubleValue() / materialUomSku.qtyOfBaseNum.doubleValue());
			System.out.println(comparing);
			return comparing;
		} else {
			return 1;
		}
	}

	// ===============================Maintain==Storage==Time==Plan====================

	/**
	 * @return 点击save按钮的时候 将拆分（编辑）的多条记录保存至数据库
	 * 目前只允许添加一条数据
	 */
	@Transactional
	public static Result saveTiming() {
		logger.info("==============saveTiming===begin===============");
		JsonNode body = request().body().asJson();
		ObjectMapper mapper = new ObjectMapper();
		List<TimingVo> timingVos = new ArrayList<TimingVo>();
		try {
			timingVos = mapper.readValue(body, new TypeReference<List<TimingVo>>() {
			});
		} catch (JsonParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (timingVos.size() > 0) {
			Order order = null;
			List<OrderItem> orderItems = new ArrayList<OrderItem>();
			if (timingVos.size() > 1) {
				return ok(play.libs.Json.toJson(new ResultVo("error","Only allow you to assign one ,Please check!")));
			} else {
				for (TimingVo timingVo : timingVos) {
					if (String.valueOf(timingVo.timingDay) == null || "".equals(timingVo.timingDay)) {
						return ok(play.libs.Json.toJson(new ResultVo("error","TimingDay can not be null !")));
					}
					order = Order.find().byId(timingVo.id);
					orderItems = OrderItem.find().where().eq("order", order).eq("deleted", false).eq("order.deleted", false).findList();
					for (OrderItem orderItem : orderItems) {
						TimingPolicy timingPolicy = null;
						ItemPolicy itemPolicy = new ItemPolicy();
						if (timingVo.haslive == true) {
							timingPolicy = TimingPolicy.find().where().eq("deleted", false).eq("id", timingVo.timingId).findUnique();
							if (timingPolicy != null)
								itemPolicy = ItemPolicy.find().where().eq("id", timingPolicy.itemPolicy.id.toString()).eq("deleted", false)
										.findUnique();
						} else {
							timingPolicy = new TimingPolicy();
							itemPolicy = new ItemPolicy();
						}
						itemPolicy.orderItem = orderItem;
						if (timingVo.storageTypeVo.nameKey != null && !"".equals(timingVo.storageTypeVo.nameKey)) {
							itemPolicy.policyType = timingVo.storageTypeVo.nameKey;
							itemPolicy.policyDescription = timingVo.storageTypeVo.nameKey;
						}
						CrudUtil.save(itemPolicy);
						StorageType storageType = StorageType.find().where().eq("id", timingVo.storageTypeVo.id).eq("warehouse.id", SessionSearchUtil.searchWarehouse().id.toString())
								.eq("deleted", false).eq("warehouse.deleted", false).findUnique();
						if (storageType != null) {
							timingPolicy.storageType = storageType;
						}
						if (timingVo.timingDay > 0) {
							timingPolicy.minHours = (int) (timingVo.timingDay * 24);// =========================
						}
						timingPolicy.itemPolicy = itemPolicy;
						timingPolicy.material = orderItem.material;
						CrudUtil.save(timingPolicy);
					}
				}
			}
		} else {
			return ok(play.libs.Json.toJson(new ResultVo("error","Can not find data !")));
		}
		logger.info("==============saveTiming===end===============");
		return ok(play.libs.Json.toJson(new ResultVo("success","It has been allocated to cold storage successfully !")));
	}

	/**
	 * @param id
	 * @return
	 * 列表显示所有TimingPlan
	 */
	@Transactional
	public static Result listTiming(String id) {
		logger.info("==============listTiming===begin===============");
		Order order = new Order();
		order = Order.find().byId(id);
		List<OrderItem> orderItems = new ArrayList<OrderItem>();
		orderItems = OrderItem.find().where().eq("order", order).findList();
		List<TimingVo> planVos = new ArrayList<TimingVo>();
		for (OrderItem orderItem : orderItems) {
			List<TimingPolicy> timingPolicies = new ArrayList<TimingPolicy>();
			timingPolicies = TimingPolicy.find().fetch("storageType").where().eq("itemPolicy.orderItem", orderItem).eq("deleted", false)
					.eq("itemPolicy.orderItem.deleted", false).eq("itemPolicy.deleted", false).findList();
			if (timingPolicies.size() > 0) {
				for (TimingPolicy timingPolicy : timingPolicies) {
					TimingVo timingVo = new TimingVo();
					List<PlanItemDetail> planItemDetails = PlanItemDetail.find().fetch("planItem").fetch("planItem.orderItem").where()
							.eq("planItem.orderItem.id", timingPolicy.itemPolicy.orderItem.id.toString()).eq("deleted", false)
							.eq("planItem.orderItem.deleted", false).eq("planItem.deleted", false).findList();
					if (planItemDetails.size() > 0) {
						timingVo.hasExecution = true;
					}
					StorageTypeVo storageTypeVo = new StorageTypeVo();
					timingVo.id = order.id.toString();
					timingVo.timingId = timingPolicy.id.toString();
					storageTypeVo.nameKey = timingPolicy.storageType.nameKey;
					storageTypeVo.id = String.valueOf(timingPolicy.storageType.id);
					timingVo.storageTypeVo = storageTypeVo;
					timingVo.storageTypeVo.nameKey = timingPolicy.storageType.nameKey;
					timingVo.timingDay = ((double) timingPolicy.minHours) / 24;
					timingVo.haslive = true;
					planVos.add(timingVo);
				}
			}
		}
		logger.info("==============listTiming===end===============");
		return ok(play.libs.Json.toJson(new ResultVo(planVos)));
	}

	/**
	 * @return 点击save按钮的时候，根据选择的条件 查询出要download的Pi,将download的数据保存至数据库
	 */
	@Transactional
	public static Result download() {
		/*
		 * logger.info("==============download==begin==================");
		 * DownloadVo vo = new DownloadVo(); RequestBody body =
		 * request().body(); if (body.asJson() != null) { vo =
		 * Json.fromJson(body.asJson(), DownloadVo.class); }
		 * System.out.println(play.libs.Json.toJson(vo)); Date fromdate =
		 * DateUtil.strToDate("1900-01-01", "yyyy-MM-dd"); Date todate =
		 * DateUtil.strToDate("2100-01-01", "yyyy-MM-dd");
		 * 
		 * if (vo.fromDownloadDate == null) { vo.fromDownloadDate = fromdate; }
		 * if (vo.toDownloadDate == null) { vo.toDownloadDate = todate; } if
		 * (vo.fromDownloadDate != null && vo.toDownloadDate != null) { if
		 * (vo.fromDownloadDate.after(vo.toDownloadDate)) { return
		 * badRequest("fromDate can not be greater than toDate,Please correct!"
		 * ); } } System.out.println("^^^^^^^^^^^^" +
		 * DateUtil.strToDate(DateUtil.dateToStrShort(vo.fromDownloadDate) +
		 * " 00:00:00", "yyyy-MM-dd HH:mm:ss"));
		 * System.out.println("************" +
		 * DateUtil.strToDate(DateUtil.dateToStrShort(vo.toDownloadDate) +
		 * " 23:59:59", "yyyy-MM-dd HH:mm:ss")); List<ExternalOrderItem> list =
		 * new ArrayList<ExternalOrderItem>(); if (vo.sgPiNo != null &&
		 * !"".equals(vo.sgPiNo)) { list = ExternalOrderItem .find() .where()
		 * .like("externalOrder.contractNo", "%" + vo.sgPiNo + "%")
		 * .eq("externalOrder.orderType", pi) .ne("itemStatus", downloadStatus)
		 * .ne("externalOrder.orderStatus", downloadStatus) .eq("deleted",
		 * false) .eq("externalOrder.deleted", false)
		 * .between("externalOrder.orderTimestamp",
		 * DateUtil.strToDate(DateUtil.dateToStrShort(vo.fromDownloadDate) +
		 * " 00:00:00", "yyyy-MM-dd HH:mm:ss"),
		 * DateUtil.strToDate(DateUtil.dateToStrShort(vo.toDownloadDate) +
		 * " 23:59:59", "yyyy-MM-dd HH:mm:ss")).findList();//
		 * DOWNLOAD表示已经下载UNDOWNLOAD表示未下载 } else { list = ExternalOrderItem
		 * .find() .where() .ne("itemStatus", downloadStatus)
		 * .eq("externalOrder.orderType", pi) .ne("externalOrder.orderStatus",
		 * downloadStatus) .eq("deleted", false) .eq("externalOrder.deleted",
		 * false) .between("externalOrder.orderTimestamp",
		 * DateUtil.strToDate(DateUtil.dateToStrShort(vo.fromDownloadDate) +
		 * " 00:00:00", "yyyy-MM-dd HH:mm:ss"),
		 * DateUtil.strToDate(DateUtil.dateToStrShort(vo.toDownloadDate) +
		 * " 23:59:59", "yyyy-MM-dd HH:mm:ss")).findList();//
		 * DOWNLOAD表示已经下载UNDOWNLOAD表示未下载 } if (list.size() > 0) { for
		 * (ExternalOrderItem extOrderItem : list) { Order order = new Order();
		 * OrderItem orderItem = new OrderItem(); order.warehouse = warehouse;
		 * order.orderType = pi; // 表示既非生产，也不是采购和销售 order.sourceType =
		 * sourceTypeDownload; // 表示数据来自SAP Download order.externalOrderNo =
		 * extOrderItem.externalOrder.externalOrderNo; // 保存order
		 * order.internalOrderNo = extOrderItem.externalOrder.externalOrderNo;
		 * order.contractNo = extOrderItem.externalOrder.contractNo;
		 * order.orderTimestamp = extOrderItem.externalOrder.orderTimestamp;
		 * order.orderStatus = extOrderItem.externalOrder.orderStatus;
		 * CrudUtil.save(order);
		 * 
		 * orderItem.order = order; orderItem.externalOrder =
		 * extOrderItem.externalOrder;// 保存orderItem orderItem.externalOrderItem
		 * = extOrderItem;
		 * 
		 * Owner owner = getOwner(); List<Material> materialList = new
		 * ArrayList<Material>(); List<MaterialUom> materialUomList = new
		 * ArrayList<MaterialUom>(); if (owner != null) materialList =
		 * Material.find().where().eq("owner", owner).eq("owner.warehouse",
		 * extOrderItem.externalMaterial.warehouse) .eq("materialCode",
		 * extOrderItem.externalMaterial.materialCode).eq("deleted",
		 * false).findList(); if (materialList.size() > 0) { orderItem.material
		 * = materialList.get(0); // 待确认？？？ materialUomList =
		 * MaterialUom.find().where().eq("uomCode",
		 * extOrderItem.externalMaterialUom.uomCode) .eq("material",
		 * materialList.get(0)).eq("deleted", false).findList(); } else {
		 * orderItem.material = getMaterialAdd(); } if (materialUomList.size() >
		 * 0) { orderItem.settlementUom = materialUomList.get(0);// 待确认？？？ //
		 * orderItem.materialUom = materialUomList.get(0); } else {
		 * orderItem.settlementUom =
		 * getMaterialUomAdd(getMaterialAdd().id.toString()); //
		 * orderItem.materialUom = //
		 * getMaterialUomAdd(getMaterialAdd().id.toString()); } if
		 * (extOrderItem.qty != null) orderItem.settlementQty =
		 * extOrderItem.qty; if (extOrderItem.minPercent != null)
		 * orderItem.minPercent = extOrderItem.minPercent; if
		 * (extOrderItem.maxPercent != null) orderItem.maxPercent =
		 * extOrderItem.maxPercent; orderItem.itemStatus = downloadStatus;
		 * 
		 * HashMap<String, String> ext = new HashMap<String, String>();
		 * ext.put("sgMaterialCode",
		 * extOrderItem.externalMaterial.materialCode); orderItem.ext =
		 * ExtUtil.apply(ext); CrudUtil.save(orderItem);
		 * 
		 * extOrderItem.itemStatus = downloadStatus; // 已完成
		 * extOrderItem.externalOrder.orderStatus = downloadStatus;// 已完成
		 * CrudUtil.update(extOrderItem);// 已经下载好了之后 回写externalOrderItem的状态
		 * CrudUtil.update(extOrderItem.externalOrder);//
		 * 已经下载好了之后回写externalOrder的状态 } } else { return
		 * badRequest("No corresponding results!"); }
		 * logger.info("==============download==end==================");
		 */
	    //String materials = DataExchangePlatform.getMaterials();
	    String orders = DataExchangePlatform.getOrders();
        //System.out.println(orders+":::"+materials);
       // if(materials)
        return ok(play.libs.Json.toJson(new ResultVo("error",orders)));
	}

	/**
	 * @param vo
	 * @param sb
	 * @return 校验数据的合法型
	 */
	@Transactional
	public static StringBuffer ckeckOut(PiVo vo, StringBuffer sb) {
		wrong = false;
		if (vo.piNo == null || "".equals(vo.piNo)) {
			sb.append("The [PI No] can't be null !");
			wrong = true;
		}
		/*if (vo.contractNo == null || "".equals(vo.contractNo)) {
			sb.append("The [SG PI No] can't be null !");
			wrong = true;
		}*/
		if (vo.minPercent != null && !"".equals(vo.minPercent) && vo.maxPercent != null && !"".equals(vo.maxPercent)) {
			if (Double.parseDouble(vo.minPercent) > Double.parseDouble(vo.maxPercent)) {
				sb.append("The scope of [Tolerance] !");
				wrong = true;
			}
		}
		if (vo.meltingPointfrom != null && !"".equals(vo.meltingPointfrom) && vo.meltingPointTo != null && !"".equals(vo.meltingPointTo)) {
			if (Double.parseDouble(vo.meltingPointfrom) > Double.parseDouble(vo.meltingPointTo)) {
				sb.append("The scope of [Melting Point] !");
				wrong = true;
			}
		}
		if (vo.materialVo.materialId == null || "".equals(vo.materialVo.materialId)) {// 后台校验物料必须为非空
			sb.append("[Material] can't be null !");
			wrong = true;
		}
		if (vo.uomVo.id == null || "".equals(vo.uomVo.id)) {// 后台校验物料单位必须为非空
			sb.append("[PI Qty Uom] can't be null !");
			wrong = true;
		}
		if (vo.piQty == null || "".equals(vo.piQty)) {
			sb.append("[PI Qty] can't be null !");
			wrong = true;
		}
		if (vo.qtyPerPallet == null || "".equals(vo.qtyPerPallet)) {
			sb.append("[Qty/Pallet] can't be null !");
			wrong = true;
		}
		return sb;
	}
	
	/**
	 * 根据PI号，查询到此PI下已经received的qty
	 * @param id pi no
	 * @return Result
	 * */
	public static Result getTotalReceivedQty(String id){
		Double receivedQty = 0.0;
		List<Execution> list = Execution.find().where().eq("planItem.order.id",id)
			.eq("executionType","T001").findList();
		for(Execution exe:list){
			receivedQty += Double.valueOf(String.valueOf(exe.executedQty));
		}
		return ok(play.libs.Json.toJson(new ResultVo("success",String.valueOf(receivedQty),"")));
	}

}
