package models.vo.inbound;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Date;
import java.util.HashMap;

import models.Order;
import models.OrderItem;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import utils.DateUtil;
import utils.ExtUtil;

/**
 * 
* <p>Project: CloudWMS</p> 
* <p>Title: PiManagementVo.java</p> 
* <p>Description: </p> 
* <p>Copyright (c) 2013 Wilmar Consultancy Services</p>
* <p>All Rights Reserved.</p>
* @author <a href="mailto:chenaihong@wcs-global.com">Chen Aihong</a>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class PiVo{// extends Pagination
	public String id;
	public MaterialVo materialVo;
	public UOMVo uomVo;
	public UOMVo qtyToBeShipUom;
	public UOMVo netWeightUom;
	public UOMVo salesContractQtyUom;
	public UOMVo qtyPerPalletUom;
	//public String blendingTank;
	public String specialRequirement; 
	public boolean hasPlan = false;
	public boolean hasCargo = false;
	// 对应order表
	public String piNo;
	public String piStatus;
	public String contractNo;
	public String sourceType;
	public String piSource;
	public Date piDate;
	public String orderType;
	public String settlementQty;
	public String piQty;
	public Date piDateFrom;
	public Date piDateTo;
	public String extraQty;
	// 对应orderItem表
	public String remarks;
	public String minPercent;
	public String maxPercent;
	public String netWeight;
	// 以下为ext中存储的字段
	public String sgMaterialCode;
	public String packingIndex;
	public String blendingTank;
	public String meltingPointfrom;
	public String meltingPointTo;
	public String qtyPerPallet;
	public String sgRemarks;
	// public String salesContract;
	public String buyer;
	public Date reqShipmentDate;
	public String destination;
	public String prodDesc;
	public String qtyToBeShip;
	public String loadPort;
	public String dischargePort;
	public String shipmentBasis;
	public String purchaseContract;
	public String salesContractQty;
	public String packing;
	public String marking;
	public String containerNo;
	public String blconsignee;
	public String totalNoOfPackage;
	
	public String ts;
	public String supplier;
	public String salescontractdate;
	public String brand;
	public String buyerproddesc;
	public String commodity;
	public String destinationport;
	public String blcon;
	public String noti;
	public String surveyor;
	public String preparedby;
	public String referenceNo;
	
	public String piType;//Local/Export

	public void setExt(String ext) {
		HashMap<String, String> Ext = ExtUtil.unapply(ext);
		netWeight=formatStr(Ext.get("netWeight"));
		packingIndex = Ext.get("packingIndex");
		blendingTank = Ext.get("blendingTank");
		specialRequirement=Ext.get("specialRequirement"); 
		meltingPointfrom = Ext.get("meltingPointfrom");
		meltingPointTo = Ext.get("meltingPointTo");
		// salesContract = Ext.get("salesContract");
		buyer = Ext.get("buyer");
		if (Ext.get("reqShipmentDate") != null && !"".equals(Ext.get("reqShipmentDate")))
		    try{
			reqShipmentDate = DateUtil.strToDate(Ext.get("reqShipmentDate"), "yyyy-MM-dd");
		    }catch(IllegalArgumentException e){
		        reqShipmentDate=new Date(Long.valueOf(Ext.get("reqShipmentDate")));
		    }
		destination = Ext.get("destination");
		prodDesc = Ext.get("prodDesc");
		qtyToBeShip = formatStr(Ext.get("qtyToBeShip"));
		if (qtyToBeShipUom != null)
			qtyToBeShipUom.id = Ext.get("qtyToBeShipUom");
		if (netWeightUom != null)
			netWeightUom.id = Ext.get("netWeightUom");
		if (salesContractQtyUom != null)
			salesContractQtyUom.id = Ext.get("salesContractQtyUom");
		if (qtyPerPalletUom != null)
			qtyPerPalletUom.id = Ext.get("qtyPerPalletUom");
		loadPort = Ext.get("loadPort");
		dischargePort = Ext.get("dischargePort");
		shipmentBasis = Ext.get("shipmentBasis");
		purchaseContract = Ext.get("purchaseContract");
		salesContractQty = formatStr(Ext.get("salesContractQty"));
		packing = Ext.get("packing");
		marking = Ext.get("marking");
		containerNo = Ext.get("containerNo");
		blconsignee = Ext.get("blconsignee");
		sgMaterialCode = Ext.get("sgMaterialCode");
		sgRemarks = Ext.get("remarks");
		totalNoOfPackage = Ext.get("totalNoOfPackage");
		piQty = formatStr(Ext.get("piQty"));
		extraQty = formatStr(Ext.get("extraQty"));
		qtyPerPallet = formatStr(Ext.get("qtyPerPallet"));
		
		supplier = Ext.get("supplier");
		salescontractdate = Ext.get("salescontractdate");
		
		brand = Ext.get("brand");//Items Brand
		buyerproddesc = Ext.get("buyerproddesc");//Items Brand
		commodity = Ext.get("commodity");
		destinationport = Ext.get("destinationport");
		blcon = Ext.get("blcon");
		noti = Ext.get("noti");
		surveyor = Ext.get("surveyor");
		preparedby = Ext.get("preparedby");
		referenceNo = Ext.get("referenceNo");
		piType = Ext.get("piType");
	}
	
	private String formatStr(String str){
		if(StringUtils.isNotEmpty(str)){
			NumberFormat nf = new DecimalFormat("0.00");
			return nf.format(Double.valueOf(str));
		}
		return str;
	}

	public String returnExt() {
		HashMap<String, String> Ext = new HashMap<String, String>();
		Ext.put("specialRequirement", specialRequirement!=null?specialRequirement:"");
		Ext.put("packingIndex", packingIndex != null ? packingIndex : "");
		Ext.put("blendingTank", blendingTank != null ? blendingTank : "");
		Ext.put("meltingPointfrom", meltingPointfrom != null ? meltingPointfrom : "");
		Ext.put("meltingPointTo", meltingPointTo != null ? meltingPointTo : "");
		// Ext.put("salesContract", salesContract != null ? salesContract : "");
		Ext.put("buyer", buyer != null ? buyer : "");
		Ext.put("reqShipmentDate", reqShipmentDate != null ? DateUtil.dateToStrShort(reqShipmentDate) : "");
		Ext.put("destination", destination != null ? destination : "");
		Ext.put("prodDesc", prodDesc != null ? prodDesc : "");
		Ext.put("qtyToBeShip", qtyToBeShip != null ? qtyToBeShip : "");
		Ext.put("qtyToBeShipUom", qtyToBeShipUom != null ? qtyToBeShipUom.id : "");
		Ext.put("netWeight",netWeight!=null?netWeight:"");
		Ext.put("netWeightUom", netWeightUom != null ? netWeightUom.id : "");
		Ext.put("salesContractQtyUom", salesContractQtyUom != null ? salesContractQtyUom.id : "");
		Ext.put("qtyPerPalletUom", qtyPerPalletUom != null ? qtyPerPalletUom.id : "");
		Ext.put("loadPort", loadPort != null ? loadPort : "");
		Ext.put("dischargePort", dischargePort != null ? dischargePort : "");
		Ext.put("shipmentBasis", shipmentBasis != null ? shipmentBasis : "");
		Ext.put("purchaseContract", purchaseContract != null ? purchaseContract : "");
		Ext.put("salesContractQty", salesContractQty != null ? salesContractQty : "");
		Ext.put("packing", packing != null ? packing : "");
		Ext.put("marking", marking != null ? marking : "");
		Ext.put("containerNo", containerNo != null ? containerNo : "");
		Ext.put("blconsignee", blconsignee != null ? blconsignee : "");
		Ext.put("sgMaterialCode", sgMaterialCode != null ? String.valueOf(sgMaterialCode) : "");
		Ext.put("totalNoOfPackage", totalNoOfPackage != null ? totalNoOfPackage : "");
		Ext.put("piQty", piQty != null ? piQty : "");
		Ext.put("extraQty", extraQty != null ? extraQty : "");
		Ext.put("qtyPerPallet", qtyPerPallet != null ? qtyPerPallet : "");
		Ext.put("remarks", sgRemarks != null ? sgRemarks : "");
		Ext.put("supplier", supplier != null ? supplier : "");
		Ext.put("salescontractdate", salescontractdate != null ? salescontractdate : "");
		Ext.put("brand", brand != null ? brand : "");
		Ext.put("buyerproddesc", buyerproddesc != null ? buyerproddesc : "");
		Ext.put("commodity",commodity != null ? commodity : "");
		Ext.put("destinationport",destinationport != null ? destinationport : "");
		Ext.put("blcon",blcon != null ? blcon : "");
		Ext.put("noti",noti != null ? noti : "");
		Ext.put("surveyor",surveyor != null ? surveyor : "");
		Ext.put("preparedby",preparedby != null ? preparedby : "");
		Ext.put("referenceNo",referenceNo != null ? referenceNo : "");
		Ext.put("piType",piType!=null?piType:"");
		return ExtUtil.apply(Ext);
	}

	public void getOrder(Order order) {
		order.externalOrderNo = piNo;
		order.internalOrderNo = piNo;
		order.contractNo = contractNo;
		if (piStatus == null || "".equals(piStatus)) {
			order.orderStatus = "S000";
		}
		order.orderStatus = piStatus;
		order.orderType = orderType;
//		order.sourceType = sourceType;
		order.orderTimestamp =  (ts==null||ts.length()<=0)?DateUtil.currentTimestamp():new Timestamp(DateUtil.strToDate(ts, "yyyy-MM-dd").getTime());
	}

	public void getOrderItem(OrderItem orderItem) {
//		if (qtyPerPallet != null) {
//			orderItem.qty = new BigDecimal(Double.parseDouble(qtyPerPallet));
//		}
		if ((piQty != null && !"".equals(piQty)) && (extraQty != null && !"".equals(extraQty))) {
			orderItem.settlementQty = new BigDecimal(Double.parseDouble(piQty) + Double.parseDouble(extraQty));
		}
		if ((piQty != null && !"".equals(piQty)) && (extraQty == null || "".equals(extraQty))) {
			orderItem.settlementQty = new BigDecimal(Double.parseDouble(piQty));
		}
		if (minPercent != null && !"".equals(minPercent)) {
			orderItem.minPercent = new BigDecimal(minPercent);
		} else {
			orderItem.minPercent = null;
		}
		if (maxPercent != null && !"".equals(maxPercent)) {
			orderItem.maxPercent = new BigDecimal(maxPercent);
		} else {
			orderItem.maxPercent = null;
		}
		if (piStatus == null || "".equals(piStatus)) {
			orderItem.itemStatus = "S000";
		}
		orderItem.itemStatus = piStatus;
		orderItem.remarks = remarks;
		orderItem.ext = returnExt();
	}
}
