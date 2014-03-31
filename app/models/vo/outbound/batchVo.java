package models.vo.outbound;


import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import models.Batch;
@JsonIgnoreProperties(ignoreUnknown = true)
public class batchVo {
	public String id;
	public String batchNo;
	public double SumQty;
	public double reserveQty;
/*	public String parentId;
	public String parentNo;*/
	public void inBatch(Batch batch){
		id=batch.id.toString();
		batchNo=batch.batchNo;
	}
	
}
