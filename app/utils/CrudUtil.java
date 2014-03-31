/** * CRUD.java 
* Created on 2013-4-24 下午1:24:10 
*/

package utils;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

import models.BaseModel;
import models.Code;

import com.avaje.ebean.Ebean;

/** 
 * <p>Project: CloudWMS</p> 
 * <p>Title: CRUD.java</p> 
 * <p>Description: </p> 
 * <p>Copyright (c) 2013 Wilmar Consultancy Services</p>
 * <p>All Rights Reserved.</p>
 * @author <a href="mailto:liyong@wcs-global.com">liyong</a>
 */

public class CrudUtil {
   /**
    * 
    * @param t
    */
	public static <T extends BaseModel> void  save(T t){
		Timestamp ts = DateUtil.currentTimestamp();
		UUID id = SessionSearchUtil.searchUser().id;
		t.createdAt = ts;
		t.updatedAt = ts;
		t.createdBy = id;
		t.updatedBy = id;
		t.save();
	}
	/**
	 * 
	 * @param t
	 */
	public static <T extends BaseModel> void  update(T t){
		Timestamp ts = DateUtil.currentTimestamp();
		t.updatedAt = ts;
		t.updatedBy =  SessionSearchUtil.searchUser().id;;
		t.update();
	}
	/**
	 * 
	 * @param t
	 */
	public static <T extends BaseModel> void  delete(T t){
		Timestamp ts = DateUtil.currentTimestamp();
		t.deleted = true;
		t.updatedAt = ts;
		t.updatedBy =  SessionSearchUtil.searchUser().id;;
		t.update();
	}
	
	public static String getCodekey(String catKey,String nameKey){	
		if(catKey==null||"".equals(catKey)||nameKey==null||"".equals(nameKey)){
			throw new NullPointerException("catKey and nameKey cannot nulll");
		}
		List<Code> codes = Ebean.find(Code.class).fetch("codeCat").where().eq("deleted", false).eq("nameKey", nameKey).eq("catKey", catKey).eq("codeCat.warehouse", SessionSearchUtil.searchWarehouse()).findList();
		if(codes==null||codes.isEmpty()){
			throw new RuntimeException("not found corresponding code");
		} 
			return codes.get(0).codeKey;
	}
 
}
