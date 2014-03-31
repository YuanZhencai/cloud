package models.vo.outbound;

import org.apache.commons.lang3.StringUtils;

import utils.ExtUtil;
import models.Stock;

public class stockVo {
	private static int i=0;
	public String id;
	public String stockNo;
	public double qty;
	public stockVo(){
		
	}
	public stockVo(Stock stock){
		this.id=stock.id.toString();
		this.stockNo=ExtUtil.unapply(stock.ext).get("stockNo");
		if(StringUtils.isEmpty(stockNo)){
			this.stockNo="TempNo"+String.valueOf(i);
			i++;
		}
		this.qty=stock.qty.doubleValue();
	}
}
