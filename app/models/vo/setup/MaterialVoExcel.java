package models.vo.setup;

import utils.Excel.ExcelObj;

public class MaterialVoExcel extends ExcelObj {
	public int no;
	public String materilaCode;
	public String description;
	public Number netWeight;
	public String uom;
	public String skuuom;

	
	public int getNo() {
		return no;
	}
	public void setNo(int no) {
		this.no = no;
	}
	public String getMaterilaCode() {
		return materilaCode;
	}
	public void setMaterilaCode(String materilaCode) {
		this.materilaCode = materilaCode;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	
	public Number getNetWeight() {
		return netWeight;
	}
	public void setNetWeight(Number netWeight) {
		this.netWeight = netWeight;
	}
	public String getUom() {
		return uom;
	}
	public void setUom(String uom) {
		this.uom = uom;
	}
	public String getSkuuom() {
		return skuuom;
	}
	public void setSkuuom(String skuuom) {
		this.skuuom = skuuom;
	}
	
}
