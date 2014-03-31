package models.printVo.StockCount;

import models.vo.query.StockCountVo;
import utils.Excel.ExcelObj;
import action.Cell;

public class StockCountDateByBatch extends ExcelObj{
	 @Cell(name="PI No")
    public String piNo;
	@Cell(name="Material Code")
    public String materialCode;
	@Cell(name="Material Desc")
	public String materialDescription;
    @Cell(name="Batch No")
    public String batchNo;
    @Cell(name="Pallet NO")
    public String StockNo;
    @Cell(name="Warehouse",Type="deleted")
    public String warehouse;
   @Cell(name="Storage Type")
    public String storageType;
    @Cell(name="Storage Area")
    public String storageArea;
    @Cell(name="Stroage Bin")
    public String storageBin;
    @Cell(name="System Qty")
    public double systemQty=0;
    @Cell(name="UOM")
    public String QtyUom;
    
    public  StockCountDateByBatch(StockCountVo stockCountVo){
       this.StockNo=stockCountVo.stockNo;
       this.piNo=stockCountVo.piNo;
       this.materialCode=stockCountVo.materialCode;
       this.materialDescription = stockCountVo.materialDescription;
       this.batchNo=stockCountVo.batchNo;
       this.warehouse=stockCountVo.warehouse;
       this.storageType=stockCountVo.storageType;
       this.storageArea=stockCountVo.storageArea;
       this.storageBin=stockCountVo.storageBin;
       this.systemQty=stockCountVo.systemQty;
       this.QtyUom=stockCountVo.QtyUom;
    }
    public String getPiNo() {
        return piNo;
    }

    public void setPiNo(String piNo) {
        this.piNo = piNo;
    }

    public String getMaterialCode() {
        return materialCode;
    }

    public void setMaterialCode(String materialCode) {
        this.materialCode = materialCode;
    }

    public String getBatchNo() {
        return batchNo;
    }

    public void setBatchNo(String batchNo) {
        this.batchNo = batchNo;
    }

    public String getWarehouse() {
        return warehouse;
    }

    public void setWarehouse(String warehouse) {
        this.warehouse = warehouse;
    }

    public String getStorageType() {
        return storageType;
    }

    public void setStorageType(String storageType) {
        this.storageType = storageType;
    }

    public String getStorageArea() {
        return storageArea;
    }

    public void setStorageArea(String storageArea) {
        this.storageArea = storageArea;
    }

    public String getStorageBin() {
        return storageBin;
    }

    public void setStorageBin(String storageBin) {
        this.storageBin = storageBin;
    }

    public double getSystemQty() {
        return systemQty;
    }

    public void setSystemQty(double systemQty) {
        this.systemQty = systemQty;
    }

    public String getQtyUom() {
        return QtyUom;
    }

    public void setQtyUom(String qtyUom) {
        QtyUom = qtyUom;
    }
	public String getStockNo() {
		return StockNo;
	}
	public void setStockNo(String stockNo) {
		StockNo = stockNo;
	}
    
}
