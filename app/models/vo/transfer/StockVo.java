/** * PalletDetail.java 
* Created on 2013-3-21 下午5:11:43 
*/

package models.vo.transfer;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;

import javax.persistence.Entity;

import models.Area;
import models.Bin;
import models.Employee;
import models.PlanItem;
import models.PlanItemDetail;
import models.Stock;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import utils.ExtUtil;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.annotation.Sql;

/** 
 * <p>Project: CloudWMS</p> 
 * <p>Title: PalletDetail.java</p> 
 * <p>Description: </p> 
 * <p>Copyright (c) 2013 Wilmar Consultancy Services</p>
 * <p>All Rights Reserved.</p>
 * @author <a href="mailto:liyong@wcs-global.com">liyong</a>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class StockVo extends PalletNoVo{

	// todo 代表这个stockVo是否被选中做操作
	public Boolean todo = false;
	// 从数据库中获得的
	public String id;
	public String lot;
	public String warehouse;
	public String area;
	public String bin;
	public String batchNo;
	public BigDecimal qty;

	public StockVo() {
		super();
		// TODO Auto-generated constructor stub
	}

	public static StockVo getStockVo(Stock stock) {
        HashMap<String,String> map = ExtUtil.unapply(stock.batch.ext);
		StockVo stockVo = new StockVo();
		stockVo.id = String.valueOf(stock.id);
		stockVo.palletNo = ExtUtil.unapply(stock.ext).get("stockNo");
		stockVo.warehouse = stock.warehouse.nameKey;
		stockVo.area = stock.area.nameKey;
		stockVo.bin = stock.bin.nameKey;
		stockVo.qty = stock.qty;
		stockVo.batchNo = map.get("lot");
		return stockVo;
	}

}
