/** * StockVo.java 
* Created on 2013-4-22 下午12:57:46 
*/

package models.vo.arrangement;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.ManyToOne;

import models.Area;
import models.Batch;
import models.Bin;
import models.Material;
import models.MaterialUom;
import models.Pallet;
import models.PalletType;
import models.Stock;
import models.Warehouse;
import models.vo.transfer.MapVo;
import models.vo.transfer.PalletNoVo;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import utils.CrudUtil;
import utils.ExtUtil;

import com.ning.http.util.DateUtil;

/** 
 * <p>Project: CloudWMS</p> 
 * <p>Title: StockVo.java</p> 
 * <p>Description: </p> 
 * <p>Copyright (c) 2013 Wilmar Consultancy Services</p>
 * <p>All Rights Reserved.</p>
 * @author <a href="mailto:liyong@wcs-global.com">liyong</a>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class StockVo extends PalletNoVo{
	// 逻辑
	// todo 代表这个stockVo是否被选中做操作
	public Boolean todo = false;
	// main=true 代表合并是这个stockVo是主stock
	public Boolean main = false;
	// 是否编辑
	public Boolean show = false;
	public String stockLineId;
	public String batchId;
	// 页面显示
	public String stockId;
	public String warehouse;
	public MapVo area;
	public MapVo bin;
	public BigDecimal qty;
	public BigDecimal palletQty;
	public String isPrint;

	public StockVo() {
		super();
	  area = new MapVo();
	  bin = new MapVo();
	}

	/**
	 * 
	 * @param stock
	 * @return
	 */
	public static StockVo getStockVo(Stock stock) {
		StockVo stockVo = new StockVo();
		stockVo.stockId = String.valueOf(stock.id);
		stockVo.palletNo = ExtUtil.unapply(stock.ext).get("stockNo");
		stockVo.warehouse = stock.area.warehouse.nameKey;
		stockVo.area.descripton = stock.area.nameKey;
		stockVo.area.key = stock.area.id.toString();
		stockVo.bin.descripton = stock.bin.nameKey;
		stockVo.bin.key = stock.bin.id.toString();
		stockVo.qty = stock.qty;
		//stockVo.palletQty = palletQty;
		return stockVo;
	}

	public static Stock getStock(StockVo stockVo, Stock dismantleStock,String ext) {
		Stock stock = new Stock();
		stock.warehouse = dismantleStock.warehouse;
		stock.material = dismantleStock.material;
		stock.materialUom = dismantleStock.materialUom;
		stock.batch = dismantleStock.batch;
		stock.qty = stockVo.qty;
		stock.palletType = dismantleStock.palletType;
		stock.pallet = dismantleStock.pallet;
		stock.tracingId = dismantleStock.tracingId;
		stock.area = dismantleStock.area;
		stock.bin = dismantleStock.bin;
		stock.ext = ext;
		stock.receivedAt = dismantleStock.receivedAt;
		stock.arrivedAt = dismantleStock.arrivedAt;
		stock.issuedAt = dismantleStock.issuedAt;
		stock.stockStatus = dismantleStock.stockStatus;
		CrudUtil.save(stock);
		return stock;
	}

}
