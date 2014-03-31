package utils;

import java.util.ArrayList;
import java.util.List;

import com.avaje.ebean.Ebean;

import models.Code;

public class CodeUtil {
	public static List<Code> getOrderStatus() {
		return Code.find().where().eq("deleted", false).eq("codeCat.deleted", false).eq("codeCat.catKey", "ORDER_STATUS")
				.eq("codeCat.warehouse", SessionSearchUtil.searchWarehouse()).findList();
	}
	public static List<Code> getOrderSource() {
		return Code.find().where().eq("deleted", false).eq("codeCat.deleted", false).eq("codeCat.catKey", "ORDER_SOURCE")
				.eq("codeCat.warehouse", SessionSearchUtil.searchWarehouse()).findList();
	}
	public static List<Code> getPlanStatus() {
		return Code.find().where().eq("deleted", false).eq("codeCat.deleted", false).eq("codeCat.catKey", "PLAN_STATUS")
			.eq("codeCat.warehouse", SessionSearchUtil.searchWarehouse()).findList();
	}

	public static List<Code> getTransActionTypes() {
		return Code.find().where().eq("deleted", false).eq("codeCat.deleted", false).eq("codeCat.catKey", "TRANSACTION_CODE")
				.eq("codeCat.warehouse", SessionSearchUtil.searchWarehouse()).findList();
	}

	/**
	 * 根据codeCat才catKey查找对应的codeList
	 * @param codeCat_catKey
	 * @return
	 */
	public static List<Code> getCode(String codeCat_catKey) {
		List<Code> codeList = new ArrayList<Code>();
		codeList = Code.find().where().eq("deleted", false).eq("codeCat.deleted", false).eq("codeCat.catKey", codeCat_catKey)
				.eq("catKey", codeCat_catKey).eq("codeCat.warehouse", SessionSearchUtil.searchWarehouse()).findList();
		return codeList;
	}

	/**
	 * 
	 * @param nameKey  TRANSACTION_CODE
	 * @return codeKey
	 */
	public static String getTransactionCode(String nameKey) {
		return getCodekey("TRANSACTION_CODE", nameKey);
	}

	/**
	 * 
	 * @param nameKey  STOCK_STATUS
	 * @return  codeKey
	 */
	public static String getStockStatus(String nameKey) {
		return getCodekey("STOCK_STATUS", nameKey);
	}

	/**
	 * 
	 * @param nameKey  Produce  Cargo
	 * @return
	 */
	public static String getOrderType(String nameKey) {
		return getCodekey("ORDER_TYPE", nameKey);
	}
	public static String getOrderStatus(String nameKey){
		return getNamekey("ORDER_STATUS", nameKey);
	}
	/**
	 * 
	 * @param nameKey
	 * @return
	 */
	public static String getPlanStatus(String nameKey) {
		return getCodekey("PLAN_STATUS", nameKey);
	}
	public static String getPlanStatusName(String codeKey) {
		return getNamekey("PLAN_STATUS", codeKey);
	}

	/**
	 * 
	 * @param codeKey
	 * @return
	 */
	public static String getOrderNamekey(String codeKey) {
		return getNamekey("ORDER_TYPE", codeKey);
	}

	public static String getPlanType(String codeKey) {
		return getNamekey("PLAN_TYPE", codeKey);
	}
	 

	/**
	 * 
	 * @param catKey
	 * @param nameKey
	 * @return codeKey
	 */
	private static String getCodekey(String catKey, String nameKey) {
		if (catKey == null || "".equals(catKey) || nameKey == null || "".equals(nameKey)) {
			throw new NullPointerException("catKey and nameKey cannot nulll");
		}
		List<Code> codes = Ebean.find(Code.class).fetch("codeCat").where().eq("deleted", false).eq("nameKey", nameKey).eq("catKey", catKey)
				.eq("codeCat.warehouse", SessionSearchUtil.searchWarehouse()).findList();
		if (codes == null || codes.isEmpty()) {
			throw new RuntimeException("not found corresponding code");
		}
		return codes.get(0).codeKey;
	}

	private static String getNamekey(String catKey, String codeKey) {
		if (catKey == null || "".equals(catKey) || codeKey == null || "".equals(codeKey)) {
			throw new NullPointerException("catKey and nameKey cannot nulll");
		}
		List<Code> codes = Ebean.find(Code.class).fetch("codeCat").where().eq("deleted", false).eq("codeKey", codeKey).eq("catKey", catKey)
				.eq("codeCat.warehouse", SessionSearchUtil.searchWarehouse()).findList();
		if (codes == null || codes.isEmpty()) {
			throw new RuntimeException("not found corresponding code");
		}
		return codes.get(0).nameKey;
	}

	public static String getTransActionTypeByCode(String code) {
		List<Code> Codes = Code.find().where().eq("deleted", false).eq("codeCat.deleted", false).eq("codeKey", code)
				.eq("codeCat.catKey", "TRANSACTION_CODE").eq("codeCat.warehouse", SessionSearchUtil.searchWarehouse()).findList();
		if (Codes.size() > 0)
			return Codes.get(0).nameKey;
		else
			return "";
	}
}
