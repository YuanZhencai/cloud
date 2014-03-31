package models.printVo.mould;

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

public class CargoPlanDate extends ExcelObj{
	@Cell(name="Booking")
	public String piNo;
	@Cell(name="Split Shipment")
	public String splitShpment;
	@Cell(name="Loc")
	public String loc;
	@Cell(name="Ref No")
	public String refNo;
	@Cell(name="Fcl")
	public String fcl;
	@Cell(name="Feet")
	public String feet;
	@Cell(name="Type")
	public String thistype;
	@Cell(name="Dest")
	public String dest;
	@Cell(name="Agent")
	public String agent;
	@Cell(name="Feeder/Vessel")
	public String feederVessel;
	@Cell(name="Cls Dt(yyyy-MM-dd)")
	public Date closedDate;
	@Cell(name="Cls Tm(HH:mm)")
	public Date closedTime;
	@Cell(name="Transp")
	public String transp;
	@Cell(name="Remarks")
	public String Remarks;
	@Cell(name="isMould",Type="deleted")
	public boolean isMould;
	
	public boolean getMould() {
		return isMould;
	}
	public void setMould(boolean isMould) {
		this.isMould = isMould;
	}
	public String getPiNo() {
		return piNo;
	}
	public void setPiNo(String piNo) {
		this.piNo = piNo;
	}
	public String getLoc() {
		return loc;
	}
	public void setLoc(String loc) {
		this.loc = loc;
	}
	public String getRefNo() {
		return refNo;
	}
	public void setRefNo(String refNo) {
		this.refNo = refNo;
	}
	public String getFcl() {
		return fcl;
	}
	public void setFcl(String fcl) {
		this.fcl = fcl;
	}
	public String getFeet() {
		return feet;
	}
	public void setFeet(String feet) {
		this.feet = feet;
	}
	public String getThistype() {
		return thistype;
	}
	public void setThistype(String thistype) {
		this.thistype = thistype;
	}
	public String getDest() {
		return dest;
	}
	public void setDest(String dest) {
		this.dest = dest;
	}
	public String getAgent() {
		return agent;
	}
	public void setAgent(String agent) {
		this.agent = agent;
	}
	public String getFeederVessel() {
		return feederVessel;
	}
	public void setFeederVessel(String feederVessel) {
		this.feederVessel = feederVessel;
	}
	public Date getClosedDate() {
		return closedDate;
	}
	public void setClosedDate(Date closedDate) {
		this.closedDate = closedDate;
	}
	public Date getClosedTime() {
		return closedTime;
	}
	public void setClosedTime(Date closedTime) {
		this.closedTime = closedTime;
	}
	public String getTransp() {
		return transp;
	}
	public void setTransp(String transp) {
		this.transp = transp;
	}
	public String getRemarks() {
		return Remarks;
	}
	public void setRemarks(String remarks) {
		Remarks = remarks;
	}
	
	
	
	
}
