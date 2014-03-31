package models.vo.outbound;

import java.util.ArrayList;
import java.util.List;

import models.Stock;

public class SelectBatch {
	public String batchNo;
	public List<stockVo> stockNos;
	public SelectBatch(){
		
	}
	public SelectBatch(String batchNo,Stock Stock){
		this.batchNo=batchNo;
		this.stockNos=new ArrayList<stockVo>();
		this.stockNos.add(new stockVo(Stock)) ;
	}
	public boolean isSame(SelectBatch selectBatch){
		if(this.batchNo.equals(selectBatch.batchNo)){
			this.stockNos.addAll(selectBatch.stockNos);
			return true;
		}
		return false;
	}
}
