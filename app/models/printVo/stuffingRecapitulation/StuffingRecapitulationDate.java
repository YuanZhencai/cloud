/** * StuffingRecapitulationDate.java 
* Created on 2013-6-4 下午5:33:06 
*/

package models.printVo.stuffingRecapitulation;

import models.Execution;
import models.PlanItem;
import utils.ExtUtil;
import utils.Excel.ExcelObj;
import action.Cell;

/** 
 * <p>Project: cloudwms</p> 
 * <p>Title: StuffingRecapitulationDate.java</p> 
 * <p>Description: </p> 
 * <p>Copyright (c) 2013 Wilmar Consultancy Services</p>
 * <p>All Rights Reserved.</p>
 * @author <a href="mailto:liyong@wcs-global.com">liyong</a>
 */

public class StuffingRecapitulationDate extends ExcelObj{
	
	 @Cell(name="NO")
	public Integer no;
	 @Cell(name="NO.STUFFING")
	 public String stuffingNumber;
	 @Cell(name="NO. IPB")
	 public String ipb;
	 @Cell(name="REFERENCE")
	 public String reference;
	 @Cell(name="PI")
	 public String pi;
	 @Cell(name="DESCRIPTION")
	 public String description;
	 @Cell(name="DESTINATION")
	 public String destination;
	 @Cell(name="QTY")
	 public Double qty;
	 
	 
	 /**
	  * SR 是 StuffingRecapitulationDate的缩写
	  * 传入PlanItem Execution 的实例，输出StuffingRecapitulationDate的实例
	  */
	 public static StuffingRecapitulationDate getSR(PlanItem planItem){
		 StuffingRecapitulationDate sr = new StuffingRecapitulationDate();
		 sr.ipb = ExtUtil.unapply(planItem.ext).get("ipb_No");
		 sr.reference =  planItem.order.contractNo;
		 sr.pi = ExtUtil.unapply(planItem.ext).get("pi");
		 sr.description =  planItem.material.materialName;
		 sr.destination =  ExtUtil.unapply(planItem.orderItem.ext).get("dest");
		 sr.qty = planItem.palnnedQty.doubleValue();
		 return sr;
	 }


	@Override
	public String getErrMsg() {
		// TODO Auto-generated method stub
		return super.getErrMsg();
	}


	@Override
	public void setErrMsg(String errMsg) {
		// TODO Auto-generated method stub
		super.setErrMsg(errMsg);
	}


	@Override
	public void putValue(String name, Object value) {
		// TODO Auto-generated method stub
		super.putValue(name, value);
	}


	@Override
	public Object outValue(String name) {
		// TODO Auto-generated method stub
		return super.outValue(name);
	}


	@Override
	public int hashCode() {
		// TODO Auto-generated method stub
		return super.hashCode();
	}


	@Override
	public boolean equals(Object obj) {
		// TODO Auto-generated method stub
		return super.equals(obj);
	}


	@Override
	protected Object clone() throws CloneNotSupportedException {
		// TODO Auto-generated method stub
		return super.clone();
	}


	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return super.toString();
	}


	@Override
	protected void finalize() throws Throwable {
		// TODO Auto-generated method stub
		super.finalize();
	}
	 
	 
	 
	 
	 
	 
	 
 
	 
	 
}
