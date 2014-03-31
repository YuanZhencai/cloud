package models.printVo.planAndRealization;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.TimeZone;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.omg.CORBA.portable.RemarshalException;

import action.Cell;

import models.OrderItem;
import models.PlanItem;
import models.vo.outbound.cargoPlanSplitVo;
import models.vo.outbound.cargoPlanVo;

import scala.reflect.internal.Trees.If;
import utils.DateUtil;
import utils.ExtUtil;
import utils.UnitConversion;
import utils.Excel.ExcelObj;

public class StuffingDate extends ExcelObj{
	@Cell(name="No.IPB")
	public String IPBNUMBER;
	@Cell(name="PI")
	public String PI;
	@Cell(name="REFERENCE")
	public String Referency;
	@Cell(name="COMMODITY")
	public String Commodity;
	@Cell(name="KG")
	public double Kg;
	@Cell(name="QTY/FCL")
	public String QtyperFcl;
	@Cell(name="NO.CONTAINER")
	public String ContainerNumber;
	@Cell(name="NO.SEAL")
	public String SealNumber;
	@Cell(name="FCL")
	public String Fcl;
	@Cell(name="QTY STUFFING ")
	public double StuffingQuantity;
	@Cell(name="DESTINATION")
	public String Destination;
	@Cell(name="VESSEL")
	public String Vessel;
	@Cell(name="NO.BATCH")
	public String BatchNo;
	@Cell(name="STUFF")
	public Date StuffingDate;
	@Cell(name="OUT")
	public Date Out;
	@Cell(name="CLOSING",Type="time")
	public Date Closing;
	@Cell(name="TRANS")
	public String Transport;
	@Cell(name="SPECIAL")
	public String Remaks;
	
	public void inCargoPlanVo(cargoPlanVo cargoPlanVo){
		PI=cargoPlanVo.piNo;
		Referency=cargoPlanVo.refNo;
		//if(cargoPlanVo.fcl!=null&&!cargoPlanVo.fcl.equals(""))
		Fcl=cargoPlanVo.fcl;
		Transport=cargoPlanVo.transp;
		Vessel=cargoPlanVo.vessel;
		Destination=cargoPlanVo.dest;
		if(cargoPlanVo.closedDate!=null&&cargoPlanVo.closedTime!=null){
		   //Closing=DateUtil.strToDate(DateUtil.dateToStrShort(cargoPlanVo.closedDate)+" "+DateUtil.dateToTime(DateUtil.dateToDateTime(cargoPlanVo.closedTime)), "yyyy-MM-dd HH:mm:ss");
		Closing=new Date(cargoPlanVo.closedDate.getTime()+cargoPlanVo.closedTime.getTime());
		System.out.println("Date================================"+Closing);
		}
	}
	public void inCargeplanSplitVo(cargoPlanSplitVo cargoPlanSplitVo){
		//BatchNo=cargoPlanSplitVo.batch_No;
		IPBNUMBER=cargoPlanSplitVo.ipb_No;
		ContainerNumber=cargoPlanSplitVo.containerNo;
		SealNumber=cargoPlanSplitVo.seal_No;
		if(cargoPlanSplitVo.stuffing_Date!=null)
		StuffingDate=cargoPlanSplitVo.stuffing_Date;
		StuffingQuantity=cargoPlanSplitVo.stuffing_Quantity;
		BatchNo=cargoPlanSplitVo.batch_No;
	//	kg=UnitConversion.returnComparing(cargoPlanSplitVo.sku_UOM.uomCode,cargoPlanSplitVo.stuffing_UOM.uomCode,cargoPlanSplitVo.material.id.toString());
	}
	public void inPlanItem(PlanItem planItem){
	    cargoPlanVo cargoPlanVo = new cargoPlanVo();
        cargoPlanVo.inOrder(planItem.order);
        System.out.println(planItem.orderItem+"++++++========orderItem=========+++++++++++++++++++");
        cargoPlanVo.inOrderItem(planItem.orderItem);
        //cargoPlanVo.setExt(planItem.orderItem.ext);
        System.out.println(cargoPlanVo.refNo+"===========================");
        cargoPlanSplitVo cargoPlanSplitVo = new cargoPlanSplitVo();
        cargoPlanSplitVo.inPlanItem(planItem);
        inCargoPlanVo(cargoPlanVo);
        inCargeplanSplitVo(cargoPlanSplitVo);
        inOrderItem(planItem.orderItem);
        StuffingQuantity=cargoPlanSplitVo.pi_SKU*UnitConversion.returnComparing(cargoPlanSplitVo.stuffing_UOM.uomCode, cargoPlanSplitVo.sku_UOM.uomCode,planItem.material.id.toString());
        Kg=UnitConversion.returnComparing(cargoPlanSplitVo.stuffing_UOM.uomCode, cargoPlanSplitVo.sku_UOM.uomCode,planItem.material.id.toString());
        System.out.println(StuffingQuantity);
        if(planItem!=null&&planItem.orderItem!=null)
        QtyperFcl=ExtUtil.unapply(planItem.orderItem.ext).get("qtyPerFcl");
	}
	public void inOrderItem(OrderItem orderItem){
	    Commodity=orderItem.material.materialName;
	    HashMap<String, String> EXT = ExtUtil.unapply(orderItem.ext);
	    Destination=EXT.get("destination");
	    //SealNumber=EXT.get("");
	    Remaks=EXT.get("remarking");
	    System.out.println(orderItem.remarks+"============             =============  f");
	}
	public String getIPBNUMBER() {
		return IPBNUMBER;
	}
	public void setIPBNUMBER(String iPBNUMBER) {
		IPBNUMBER = iPBNUMBER;
	}
	public String getPI() {
		return PI;
	}
	public void setPI(String pI) {
		PI = pI;
	}
	public String getReferency() {
		return Referency;
	}
	public void setReferency(String referency) {
		Referency = referency;
	}
	public String getCommodity() {
		return Commodity;
	}
	public void setCommodity(String commodity) {
		Commodity = commodity;
	}
	public double getKg() {
		return Kg;
	}
	public void setKg(double kg) {
		Kg = kg;
	}
	public String getQtyperFcl() {
		return QtyperFcl;
	}
	public void setQtyperFcl(String qtyperFcl) {
		QtyperFcl = qtyperFcl;
	}
	public String getContainerNumber() {
		return ContainerNumber;
	}
	public void setContainerNumber(String containerNumber) {
		ContainerNumber = containerNumber;
	}
	public String getSealNumber() {
		return SealNumber;
	}
	public void setSealNumber(String sealNumber) {
		SealNumber = sealNumber;
	}
	public String getFcl() {
		return Fcl;
	}
	public void setFcl(String fcl) {
		Fcl = fcl;
	}
	public double getStuffingQuantity() {
		return StuffingQuantity;
	}
	public void setStuffingQuantity(double stuffingQuantity) {
		StuffingQuantity = stuffingQuantity;
	}
	public String getDestination() {
		return Destination;
	}
	public void setDestination(String destination) {
		Destination = destination;
	}
	public String getVessel() {
		return Vessel;
	}
	public void setVessel(String vessel) {
		Vessel = vessel;
	}
	public String getBatchNo() {
		return BatchNo;
	}
	public void setBatchNo(String batchNo) {
		BatchNo = batchNo;
	}
	public Date getStuffingDate() {
		return StuffingDate;
	}
	public void setStuffingDate(Date stuffingDate) {
		StuffingDate = stuffingDate;
	}
	public Date getOut() {
		return Out;
	}
	public void setOut(Date out) {
		Out = out;
	}
	public Date getClosing() {
		return Closing;
	}
	public void setClosing(Date closing) {
		Closing = closing;
	}
	public String getTransport() {
		return Transport;
	}
	public void setTransport(String transport) {
		Transport = transport;
	}
	public String getRemaks() {
		return Remaks;
	}
	public void setRemaks(String remaks) {
		Remaks = remaks;
	}
	
	
}
