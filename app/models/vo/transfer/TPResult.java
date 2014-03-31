/** * OrderOutBound.java 
* Created on 2013-3-20 下午1:41:15 
*/

package models.vo.transfer;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.OneToOne;

import org.apache.commons.lang3.ArrayUtils;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import utils.CodeUtil;
import utils.EmptyUtil;
import utils.ExtUtil;

import models.PlanItem;

import com.avaje.ebean.annotation.Sql;

/** 
 * <p>Project: CloudWMS</p> 
 * <p>Title: OrderOutBound.java</p> 
 * <p>Description: </p> 
 * <p>Copyright (c) 2013 Wilmar Consultancy Services</p>
 * <p>All Rights Reserved.</p>
 * @author <a href="mailto:liyong@wcs-global.com">liyong</a>
 */

@JsonIgnoreProperties(ignoreUnknown = true)
public class TPResult {
	public String id;
	public String pino; // order
	public String warehouse; // order
	public String proDate; // plan ext
	public String proUom; // planitem ext
	public BigDecimal proQty; // planitem ext
	public String batchNo; // batch
	public List<String> batchId;
	public String mat; // material
	public String matUom; // material_uom

	public BigDecimal transferQty;
	public Date transferDate;
	public String transferType;
	public String fromUom; // material_uom
	public String fromArea; // area
	public String fromBin; // bin
	public String status;
	public String statusName;

	// 下面的四个字段是为了cargoPlan
	public String containerNo; // area
	public String palletized; // bin
	public String closeDateTime;
	public String remaks;

	public TPResult() {
		super();
		// TODO Auto-generated constructor stub
	}

	public static TPResult getTPResult(PlanItem planItem) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		HashMap<String, String> planItemExt = null;
		planItemExt = ExtUtil.unapply(planItem.ext);
		TPResult tpr = new TPResult();
		tpr.id = String.valueOf(planItem.id);
		tpr.pino = planItem.order.internalOrderNo; // order
		tpr.warehouse = planItem.order.warehouse.nameKey; // order
		tpr.proUom = planItem.materialUom.uomCode; // planitem ext
		tpr.proQty = new BigDecimal(planItemExt.get("proQuantity")); // planitem
																		// ext

		String lot = planItemExt.get("lot");
		String[] lots = lot.split("[,]");
		tpr.batchNo = lot;
		tpr.batchId = Arrays.asList(lots);
		tpr.mat = planItem.material.materialCode; // material
		tpr.matUom = planItem.materialUom.uomCode; // material_uom
		tpr.transferQty = planItem.palnnedQty;
		tpr.transferDate = planItem.plannedExecutionAt;
		tpr.transferType = ExtUtil.unapply(planItem.ext).get("transferType");
		tpr.fromUom = planItem.fromMaterialUom.uomCode; // material_uom
		// tpr.fromArea =planItem.fromArea.nameKey; // area
		// tpr.fromBin =planItem.fromBin.nameKey; // bin
		tpr.status = planItem.itemStatus;
		tpr.statusName = CodeUtil.getPlanStatusName(planItem.itemStatus);
		tpr.containerNo = planItemExt.get("containerNo");
		tpr.palletized = changeString(planItemExt.get("palletized"));
		tpr.closeDateTime = planItemExt.get("closeDateTime");
		tpr.proDate = planItemExt.get("date");
		tpr.remaks = planItemExt.get("remaks");
		// tpr.toUom =planItem.toMaterialUom.uomCode;
		// tpr.toArea =planItem.toArea.nameKey;
		// tpr.toBin =planItem.toBin.nameKey;
		return tpr;
	}

	private static  String changeString(String str) {
		String s = null;
		if (str == null) {
			return null;
		}
		if ("".equals(str.trim())) {
			s = str.trim();
		} else if ("false".equals(str.trim())) {
			s = "N";
		} else if ("true".equals(str.trim())) {
			s = "Y";
		}
		return s;
	}

}
