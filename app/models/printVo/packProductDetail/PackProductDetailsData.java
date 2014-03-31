/** * PackProductDetailsVo.java 
* Created on 2013-8-2 下午4:01:40 
*/

package models.printVo.packProductDetail;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import models.Batch;
import models.Execution;
import models.MaterialUom;
import models.OrderItem;
import utils.BatchSearchUtil;
import utils.DateUtil;
import utils.EmptyUtil;
import utils.ExtUtil;
import utils.UnitConversion;
import utils.Excel.ExcelObj;
import action.Cell;

import com.avaje.ebean.Ebean;

/** 
 * <p>Project: CloudWMS</p> 
 * <p>Title: PackProductDetailsVo.java</p> 
 * <p>Description: </p> 
 * <p>Copyright (c) 2013 Wilmar Consultancy Services</p>
 * <p>All Rights Reserved.</p>
 * @author <a href="mailto:liyong@wcs-global.com">liyong</a>
 */

public class PackProductDetailsData  extends ExcelObj {
	@Cell(name = "PI No")
	public String piNo;
	@Cell(name = "Material Desc")
	public String matDesc;
	@Cell(name = "Net Weight(KG)")
	public String netWeigth;
	@Cell(name = "Destination")
	public String destination;
	@Cell(name = "Kg/Sku")
	public BigDecimal kgSku;
	@Cell(name = "SKU Qty")
	public BigDecimal skuQty;
	@Cell(name = "SKU UOM")
	public String skuUom;
	@Cell(name = "Total tonnage")
	public BigDecimal totalTonnage;
	@Cell(name = "Uom")
	public String uom;
	@Cell(name = "Remark")
	public String remark;
	@Cell(name = "First Prod Date")
	public Date firstProdDate;
	@Cell(name = "Batch No.")
	public String batchNo;
	@Cell(name = "Contract No.")
	public String contractNo;
	@Cell(name = "Material Code")
	public String matCode;

	/**
	 * 
	 * @param orderItem
	 */
	public void setPackProductDetailsVo(OrderItem orderItem) {
		HashMap<String, String> oiExt = ExtUtil.unapply(orderItem.ext);

		this.piNo = orderItem.order.internalOrderNo;
		List<Batch> batchs = BatchSearchUtil.getBatch(this.piNo);
		StringBuffer sb = new StringBuffer();

		String fpd = "";
		// 这段代码是为了 firstProdDate 和 batchNo
		if (EmptyUtil.isNotEmptyList(batchs)) {
			for (Batch batch : batchs) {
				Batch b = Ebean.find(Batch.class).where().eq("id", batch.id).findUnique();
				HashMap<String, String> batchExt = ExtUtil.unapply(b.ext);
				sb.append(batchExt.get("lot") + ",");
				if (batchExt.get("date").compareTo(fpd) > 0) {
					fpd = batchExt.get("date");
				}
			}
			if (sb.toString().endsWith(","))
				sb.deleteCharAt(sb.length() - 1);
			this.firstProdDate = DateUtil.strShortToDate(fpd);
		}
		this.skuQty = this.totalTonnage = new BigDecimal(0);
		List<Execution> executions = Ebean.find(Execution.class).where().eq("deleted", false).eq("executionSubtype", "T001.001")
			.eq("planItem.orderItem.id", orderItem.id).findList();
		if (EmptyUtil.isNotEmptyList(executions)) {
			for (Execution execution : executions) {
				skuQty = skuQty.add(execution.executedQty);
			}
		}
		this.batchNo = sb.toString();
		this.matDesc = orderItem.material.materialName;
		this.netWeigth = oiExt.get("netWeight");
		this.destination = oiExt.get("destinationport");
		String skuUomId = oiExt.get("qtyPerPalletUom");
		MaterialUom skuUomEntity = Ebean.find(MaterialUom.class).where().eq("id", skuUomId).findUnique();
		try {
			this.kgSku = UnitConversion.SkuToQuantity(orderItem.settlementUom.id.toString(), skuUomId);
		} catch (Exception e) {
		    System.out.println("orderItem.settlementUom.id.toString() "+orderItem.settlementUom);
		    System.out.println("skuUomId "+skuUomId);
		    this.kgSku = new BigDecimal(1);
		}
		this.skuUom = skuUomEntity.uomCode;
		this.totalTonnage = this.skuQty.multiply(this.kgSku);
		this.uom = "KG";
		this.remark = orderItem.remarks;
		this.contractNo = orderItem.order.contractNo;
		this.matCode = orderItem.material.materialCode;
	}

}
