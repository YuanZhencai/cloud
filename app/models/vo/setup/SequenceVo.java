/** * SequenceVo.java 
* Created on 2013-5-8 下午3:38:55 
*/

package models.vo.setup;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import models.Config;
import models.WarehouseSequence;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import utils.CrudUtil;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.ExpressionList;

/** 
 * <p>Project: CloudWMS</p> 
 * <p>Title: SequenceVo.java</p> 
 * <p>Description: </p> 
 * <p>Copyright (c) 2013 Wilmar Consultancy Services</p>
 * <p>All Rights Reserved.</p>
 * @author <a href="mailto:liyong@wcs-global.com">liyong</a>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class SequenceVo {

	public String id;
	public String code;
	public String format;
	public Long startNo;
	public Long endNo;
	public Boolean recycle;
	public String cycleType;
	
	/**
	 * 
	 * @param searchMap
	 * @return
	 */
	public static List<SequenceVo> getSequence(Map<String,Object> searchMap){
		ArrayList<SequenceVo> sequenceVos = new ArrayList<SequenceVo>();
		ExpressionList<WarehouseSequence> el = Ebean.find(WarehouseSequence.class).where().eq("deleted", false);
		if (searchMap.get("code") != null && !"".equals(String.valueOf(searchMap.get("code")).trim())) {
		el.like("sequenceKey", "%" + String.valueOf(searchMap.get("code")).trim() + "%");
		}
		if (searchMap.get("startNo") != null) {
			el.eq("startNo",searchMap.get("startNo") );
		}
		List<WarehouseSequence> sequences = el.order("updatedAt descending").findList();
		if (sequences != null && !sequences.isEmpty()) {
			for (WarehouseSequence sequence : sequences) {
				SequenceVo sequenceVo = new SequenceVo();
				sequenceVo.id = String.valueOf(sequence.id);
				sequenceVo.code = sequence.sequenceKey ;
				sequenceVo.startNo = sequence.startNo ;
				sequenceVos.add(sequenceVo); 
			}
		}
		return sequenceVos;
	}
	/**
	 * 
	 * @param sequenceVo
	 */
   public static void setSequence(SequenceVo sequenceVo){
	   WarehouseSequence sequence = new  WarehouseSequence();
		boolean flag = true;
		if (sequenceVo.id != null && !"".equals(sequenceVo.id.trim())) {
			sequence = Ebean.find(WarehouseSequence.class).where().eq("deleted", false).eq("id", sequenceVo.id).findUnique();
			flag = false;
		}
	   sequence.warehouse = null;
	   sequence.sequenceKey =sequenceVo.code;
	   sequence.startNo = sequenceVo.startNo;
//	   warehouseSequence.currentNo = 1;
	   sequence.remarks = null;
	   sequence.ext = null;
		// warehouseSequence.version = 1;
	   sequence.schemaCode = null;
	   if(flag){
		   CrudUtil.save(sequence);
	   }else{
		   CrudUtil.update(sequence);
	   }
   }
}
