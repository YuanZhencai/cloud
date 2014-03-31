/** * StockAdjustLocationVo.java 
* Created on 2013-6-22 上午10:57:52 
*/

package models.vo.adjustment;

import java.math.BigDecimal;
import java.util.HashMap;

import models.Stock;
import models.vo.transfer.PalletNoVo;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import utils.ExtUtil;

/** 
 * <p>Project: CloudWMS</p> 
 * <p>Title: StockAdjustLocationVo.java</p> 
 * <p>Description: </p> 
 * <p>Copyright (c) 2013 Wilmar Consultancy Services</p>
 * <p>All Rights Reserved.</p>
 * @author <a href="mailto:liyong@wcs-global.com">liyong</a>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class StockAdjustLocationVo extends PalletNoVo{
   public  Boolean todo= false;
   public String  id;
   public String  piNo;
   public  String  batchNo;
   public  String  material;
   public  String  materialUom;
   public  BigDecimal qty;
   public  String area;
   public  String bin;
   public  String areaTo;
   public  String binTo;
   public  String forkLiftDriver;
   
 
  /**
   * 
   * @param stock
   */
 public void setStockAdjustLocationVo(Stock stock){
	   HashMap<String, String> stockExt = ExtUtil.unapply(stock.ext);
	   HashMap<String, String> batchExt = ExtUtil.unapply(stock.batch.ext);
	    this.id = stock.id.toString();
	    this.palletNo = stockExt.get("stockNo");
	    this.piNo = batchExt.get("pi");
	    this.batchNo = batchExt.get("lot");
	    this.material = stock.material.materialCode;
	    this.materialUom = stock.materialUom.uomCode;
	    this.qty = stock.qty;
	    this.area = stock.area.areaCode;
	    this.bin = stock.bin.binCode;
 }
}
