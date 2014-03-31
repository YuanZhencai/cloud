/** * DirectTransferVo.java 
* Created on 2013-7-10 下午5:54:22 
*/

package models.vo.transfer;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import models.Area;
import models.Batch;
import models.Bin;
import models.MaterialUom;
import models.OrderItem;
import models.Stock;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import utils.BatchSearchUtil;
import utils.CrudUtil;
import utils.EmptyUtil;
import utils.ExtUtil;
import utils.SessionSearchUtil;

import com.avaje.ebean.Ebean;

/** 
 * <p>Project: CloudWMS</p> 
 * <p>Title: DirectTransferVo.java</p> 
 * <p>Description: </p> 
 * <p>Copyright (c) 2013 Wilmar Consultancy Services</p>
 * <p>All Rights Reserved.</p>
 * @author <a href="mailto:liyong@wcs-global.com">liyong</a>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class DirectTransferVo {
	public Boolean todo;
	public String palletNo;
	public String piNo;
	public String batchNo;
	public String proDate;
	public MapVo area;
	public MapVo bin;
	public BigDecimal qty;
	public BigDecimal rate;
	public MapVo employee;
//""stockNo"=>"2""
	public Stock setStock() {
		Stock stock = new Stock();
		HashMap<String, String> map = new HashMap<String,String>();
		Batch batch = null;
		List<OrderItem> orderItems = orderItems = Ebean.find(OrderItem.class).where().eq("deleted", false).eq("order.internalOrderNo", piNo).findList();
		List<Batch> batchs = BatchSearchUtil.serchBatch(batchNo, "", proDate,piNo);
		//处理batch
		if(EmptyUtil.isNotEmptyList(batchs)){
			 batch= Ebean.find(Batch.class).where().eq("id", batchs.get(0).id).findUnique();
		}else{
			batch = new Batch();
			map.put("pi", piNo);
			map.put("lot", batchNo);
			map.put("line", "");
			map.put("date", proDate);
			batch.ext = ExtUtil.apply(map);
			if(EmptyUtil.isNotEmptyList(orderItems)){
				batch.material = orderItems.get(0).material;
			}
			CrudUtil.save(batch);
		}
		if(EmptyUtil.isNotEmptyList(orderItems)){
			String skuUomId = ExtUtil.unapply(orderItems.get(0).ext).get("qtyPerPalletUom");
			MaterialUom matUom = Ebean.find(MaterialUom.class).where().eq("deleted", false).eq("id", skuUomId).findUnique();
			stock.materialUom = matUom;
		}
		Area areaEntity = Ebean.find(Area.class).where().eq("deleted", false).eq("id", area.key).findUnique();
		Bin binEntity = Ebean.find(Bin.class).where().eq("deleted", false).eq("id", bin.key).findUnique();
		stock.batch = batch;
		stock.qty = qty;
		stock.warehouse = SessionSearchUtil.searchWarehouse();
		stock.material = batch.material;
		stock.area = areaEntity;
		stock.bin = binEntity;
		map.clear();
		map.put("stockNo", palletNo);
		stock.ext = ExtUtil.apply(map);
		Timestamp now = new Timestamp(System.currentTimeMillis());
		stock.receivedAt = now;
		stock.arrivedAt = now;
		return stock;

	}
}
