/** * Code.java 
* Created on 2013-7-4 上午10:17:50 
*/

package utils.enums;

/** 
 * <p>Project: CloudWMS</p> 
 * <p>Title: Code.java</p> 
 * <p>Description: </p> 
 * <p>Copyright (c) 2013 Wilmar Consultancy Services</p>
 * <p>All Rights Reserved.</p>
 * @author <a href="mailto:liyong@wcs-global.com">liyong</a>
 */

public enum CodeKey {
	// order type
	ORDER_TYPE_PRODUCE("T001"),ORDER_TYPE_CARGO("T004"),

	// order status
	ORDER_STATUS_NEW("S000"),
	// plan type
	PLAN_TYPE_RECEIVE("T001"), PLAN_TYPE_TRANSFER("T002"),

	// plan status
	PLAN_STATUS_NEW("S000"),PLAN_STATUS_CONFIRM("S001"), PLAN_STATUS_EXECUTEING("S002"), PLAN_STATUS_EXECUTED("S003"),

	// plan detai status
	PLAN_DETAIL_STATUS_NEW("S000"), PLAN_DETAIL_STATUS_EXECUTED("S001"),
	// employ type
	EMPLOY_TYPE_FORKLIFTDRIVE("ET006"),

	// transaction code
	
	TRANSACTION_CODE_RECEIPT("T101"),TRANSACTION_CODE_TRANSFER("T201"), TRANSACTION_CODE_COMBIN("T403"), TRANSACTION_CODE_COMBINEREVERSED("T404"),TRANSACTION_CODE_ISSUE("T301"),
	TRANSACTION_CODE_DIRECTTRANSFER("T204"),
	TRANSACTION_CODE_DIRECT_RECEIPT("T103"),
	
	//execution type 
	EXECUTION_TYPE_TRANSFER("T002"),EXECUTION_TYPE_RECEIPT("T001");
 
	 
	
	
	
	
	
	
	
	
	
	
	
	private final String value;

	CodeKey(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}

	public String toString() {
		return value;
	}

}
