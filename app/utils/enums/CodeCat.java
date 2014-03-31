/** * CodeCat.java 
* Created on 2013-7-4 下午1:12:45 
*/

package utils.enums;

/** 
 * <p>Project: CloudWMS</p> 
 * <p>Title: CodeCat.java</p> 
 * <p>Description: </p> 
 * <p>Copyright (c) 2013 Wilmar Consultancy Services</p>
 * <p>All Rights Reserved.</p>
 * @author <a href="mailto:liyong@wcs-global.com">liyong</a>
 */

public enum CodeCat {
	ORDER_STATUS("ORDER_STATUS"),
	PLAN_STATUS("PLAN_STATUS"), 
	PALLET_STATUS("PALLET_STATUS"), 
	STOCK_STATUS("STOCK_STATUS"),
	COUNT_STATUS("COUNT_STATUS"),
	TRANSACTION_CODE("TRANSACTION_CODE"),
	UNIT_TYPE("UNIT_TYPE"),
	SOURCE_TYPE("SOURCE_TYPE"), 
	ORDER_TYPE("ORDER_TYPE"), 
	PLAN_TYPE("PLAN_TYPE"),
	PLAN_SUBTYPE("PLAN_SUBTYPE"),
	COUNT_TYPE("COUNT_TYPE");
	
	
 	private final String value;
	CodeCat(String value){
		this.value = value;
	}
	public String getValue(){
		return value;
	}
	public String toString(){
		return value;
	}
	

	 

}
