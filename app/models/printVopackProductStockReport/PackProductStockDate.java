/** * PackProductStockDate.java 
* Created on 2013-6-5 涓嬪崍4:55:16 
*/

package models.printVopackProductStockReport;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import models.Area;
import models.Bin;
import models.Material;
import models.MaterialUom;
import models.OrderItem;
import models.Stock;
import models.StockTransaction;
import utils.DateUtil;
import utils.ExtUtil;
import utils.UnitConversion;
import utils.Excel.ExcelObj;
import action.Cell;

/** 
 * <p>Project: CloudWMS</p> 
 * <p>Title: PackProductStockDate.java</p> 
 * <p>Description: </p> 
 * <p>Copyright (c) 2013 Wilmar Consultancy Services</p>
 * <p>All Rights Reserved.</p>
 * @author <a href="mailto:chenaihong@wcs-global.com">ChenAi hong</a>
 */

public class PackProductStockDate extends ExcelObj implements Cloneable{
    @Cell(name="PI Type")
    public String piType;
    @Cell(name = "PI No.")
    public String pi;
    @Cell(name = "Material Description")
    public String commodity;// 琛ㄧずmaterial 鐨刣escription
    @Cell(name = "Net Weight(KG)")
    public Double kg=new Double(0);
    @Cell(name = "Destination")
    public String destination;
    @Cell(name = "Opening")
    public Double opening=new Double(0);// 琛ㄧず鏄ㄥぉ浠撳簱鐨勫簱瀛�
    @Cell(name = "Receive")
    public Double receive=new Double(0);// 琛ㄧず褰撳墠鍏ュ簱鐨勯噺
    @Cell(name = "Issue")
    public Double issue=new Double(0);// 琛ㄧず褰撳墠鍑哄簱鐨勯噺
    @Cell(name = "Sample")
    public Double sample=new Double(0);// Sample銆丱ut To Lab銆丅lend涓変釜瀛楁鏀剧┖
    @Cell(name = "Blend")
    public Double blend=new Double(0);// Sample銆丱ut To Lab銆丅lend涓変釜瀛楁鏀剧┖
    @Cell(name = "Others")
    public Double others=new Double(0);// Sample銆丱ut To Lab銆丅lend涓変釜瀛楁鏀剧┖
    @Cell(name="Nca",Type="deleted")
    public Double Nca=new Double(0);
    @Cell(name = "Closing")
    public Double closing=new Double(0);//Opening Stock+IN-Ship
    @Cell(name = "UOM")
    public String sku;//Closing*KG/1000
    @Cell(name = "Total")
    public Double total=new Double(0);
    @Cell(name = "UOM")
    public String uom;
    @Cell(name = "First Prod Date")
    public Date proDate;
    @Cell(name = "Batch No")
    public String batchs;
    @Cell(name = "Contract No")
    public String refNo;
    @Cell(name = "Material Code")
    public String materialCode;
    @Cell(name="stockId",Type="deleted")
    public String StockId;
    @Cell(name="stockOldQty",Type="deleted")
    public Double stockOldQty=new Double(0);
    @Cell(name="stockOldDate",Type="deleted")
    public Date stockOldDate;
    @Cell(name="stockNewQty",Type="deleted")
    public Double stockNewQty=new Double(0);
    @Cell(name="stockNewDate",Type="deleted")
    public Date stockNewDate;
    @Cell(name="batchs",Type="deleted")
    public List<String> tempBatchs=new ArrayList<String>();
    @Cell(name="isNca",Type="deleted")
    public boolean isNca;
    @Cell(name="tempNca",Type="deleted")
    public Double tempNca=new Double(0);
    @Override
    public String getErrMsg() {
        // TODO Auto-generated method stub
        return super.getErrMsg();
    }

    @Override
    public void setErrMsg(String errMsg) {
        // TODO Auto-generated method stub
        super.setErrMsg(errMsg);
    }

    @Override
    public void putValue(String name, Object value) {
        // TODO Auto-generated method stub
        super.putValue(name, value);
    }

    @Override
    public Object outValue(String name) {
        // TODO Auto-generated method stub
        return super.outValue(name);
    }

    @Override
    public int hashCode() {
        // TODO Auto-generated method stub
        return super.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        // TODO Auto-generated method stub
        return super.equals(obj);
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        // TODO Auto-generated method stub
        return super.clone();
    }

    @Override
    public String toString() {
        // TODO Auto-generated method stub
        return super.toString();
    }

    @Override
    protected void finalize() throws Throwable {
        // TODO Auto-generated method stub
        super.finalize();
    }
    public void initTransaction(StockTransaction transaction){
        this.opening=transaction.newQty!=null?transaction.newQty.doubleValue():new Double(0);
        this.closing=transaction.newQty!=null?transaction.newQty.doubleValue():new Double(0);
    }
    public void initNacTransaction(StockTransaction transaction){
        this.Nca=new Double(0);
        this.opening=transaction.newQty!=null?transaction.newQty.doubleValue():new Double(0);
        this.closing=transaction.newQty!=null?transaction.newQty.doubleValue():new Double(0);
        getTransactionCode(transaction);
    }
    public void inNacTransaction(StockTransaction transaction){
        getTransactionCode(transaction);
    }
    public void inTransaction(StockTransaction transaction){
        if(transaction.transactionAt!=null){
            Date date = new Date(transaction.transactionAt.getTime());
            if(this.stockOldDate!=null){
                if(isLater(this.stockOldDate, date)){
                    this.stockOldDate=date;
                    this.stockOldQty=transaction.oldQty!=null?transaction.oldQty.doubleValue():new Double(0);
                    this.opening=transaction.oldQty!=null?transaction.oldQty.doubleValue():new Double(0);
                }
            }else{
                this.stockOldDate=date;
                this.stockOldQty=transaction.oldQty!=null?transaction.oldQty.doubleValue():new Double(0);
                this.opening=transaction.oldQty!=null?transaction.oldQty.doubleValue():new Double(0);
            }
            if(this.stockNewDate!=null){
                if(!isLater(this.stockNewDate, date)){
                    this.stockNewDate=date;
                    this.stockNewQty=transaction.newQty!=null?transaction.newQty.doubleValue():new Double(0);
                    this.closing=transaction.newQty!=null?transaction.newQty.doubleValue():new Double(0);
                
                }
            }else{
                this.stockNewDate=date;
                this.stockNewQty=transaction.newQty!=null?transaction.newQty.doubleValue():new Double(0);
                this.closing=transaction.newQty!=null?transaction.newQty.doubleValue():new Double(0);
                //this.isNca=chechNewArea(transaction);
            }
            
        }
        getTransactionCode(transaction);
        //setStock(transaction.stock);
    }
    
    public void getTransactionCode(StockTransaction transaction){
        String transactionCode=transaction.transactionCode;
        Double changQty = new Double( (transaction.newQty!=null?transaction.newQty.doubleValue():new Double(0))-(transaction.oldQty!=null?transaction.oldQty.doubleValue():new Double(0)));
        if("T101".equals(transactionCode)){
            this.receive+=changQty;
            this.isNca=chechNewArea(transaction);
        }else if("T102".equals(transactionCode)){
            this.receive+=changQty;
            this.isNca=chechNewArea(transaction);
        }else if("T103".equals(transactionCode)){
            this.receive+=changQty;
            this.isNca=chechNewArea(transaction);
        }else if("T204".equals(transactionCode))
            this.receive+=changQty;
        else if("T301".equals(transactionCode))
            this.issue+=changQty;
        else if("T302".equals(transactionCode))
            this.issue+=changQty;
        else if("T303".equals(transactionCode))
            this.blend+=changQty;
        else if("T304".equals(transactionCode))
            this.sample+=changQty;
        else if("T399".equals(transactionCode))
            this.others+=changQty;
        else if("T403".equals(transactionCode)){
            this.others+=changQty;
            this.isNca=chechNewArea(transaction);
        }else if("T501".equals(transactionCode)){
            this.others+=changQty;
            this.isNca=chechNewArea(transaction);
        }else if("T201".equals(transactionCode)){
            this.isNca=chechNewArea(transaction);
            if(chechNewArea(transaction)){
                this.Nca+=new Double(transaction.newQty.doubleValue());
            }
            if(chechOldArea(transaction)){
                this.Nca-=new Double(transaction.newQty.doubleValue());
            }
        }
        /*if(this.isNca&&(!"T201".equals(transactionCode))){
            if(!transaction.newBinId.equals(transaction.oldBinId)){
            this.tempNca=new Double(changQty);
            }
        }*/
        
    }

    public void setOrder(String piNo){
        List<OrderItem> orderItems = OrderItem.find().where().eq("order.internalOrderNo",piNo).eq("order.orderType", "T001").findList();
        if(null!=orderItems&&orderItems.size()>0){
            OrderItem orderItem = orderItems.get(0);
            this.refNo=orderItem.order.contractNo;
            HashMap<String, String> ext = ExtUtil.unapply(orderItem.ext);
            if (ext.get("destinationport") != null && !"".equals(ext.get("destinationport")))
                this.destination = ext.get("destinationport");
        }
    }
    public void setStock(Stock stock){
        if(stock!=null){
            this.StockId=stock.id.toString();
            Material material = stock.material;
            if(material!=null){
                this.materialCode=material.materialCode;
                this.commodity = material.materialName;//execution.planItem.material.materialName;
                 MaterialUom materialUom = MaterialUom.find().where().eq("material.id", material.id).eq("baseUom",Boolean.TRUE).findUnique();
                if(stock.materialUom!=null&&materialUom!=null){
                    this.sku =stock.materialUom.uomCode;
                    this.uom =materialUom.uomCode;
                    this.kg = UnitConversion.returnComparing(uom, sku, material.id.toString());
                }
            }
            if(stock.batch!=null)
            setBatchExt(stock.batch.ext);
        }
    }
    @Cell(name="area",Type="deleted")
    public static final String AREA="Non Conformity";
    @Cell(name="bin",Type="deleted")
    public static final String BIN="NC";
    public boolean chechNewArea(StockTransaction transaction){
        if(transaction!=null){
            if(transaction.newAreaId!=null&&transaction.newBinId!=null)
                if(AREA.equals(Area.find().byId(transaction.newAreaId.toString())!=null?Area.find().byId(transaction.newAreaId.toString()).nameKey:"")&&BIN.equals(Bin.find().byId(transaction.newBinId.toString())!=null?Bin.find().byId(transaction.newBinId.toString()).nameKey:""))
                    return true;
        }
        return false;
    }
    public boolean chechOldArea(StockTransaction transaction){
        if(transaction!=null){
            if(transaction.oldAreaId!=null&&transaction.oldBinId!=null)
                if(AREA.equals(Area.find().byId(transaction.oldAreaId.toString())!=null?Area.find().byId(transaction.oldAreaId.toString()).nameKey:"")&&BIN.equals(Bin.find().byId(transaction.oldBinId.toString())!=null?Bin.find().byId(transaction.oldBinId.toString()).nameKey:""))
                    return true;
        }
        return false;
    }
    public void Number(){
        this.issue=Math.abs(this.issue);
        this.sample=Math.abs(this.sample);
        this.blend=Math.abs(this.blend);
        this.others=Math.abs(this.others);
    }
    public void setBatchExt(String ext){
        HashMap<String,String> map = ExtUtil.unapply(ext);
        this.pi=map.get("pi");
        if(StringUtils.isNotEmpty(this.pi)){
            setOrder(this.pi);
        }
        this.batchs=map.get("lot");
        if(StringUtils.isNotEmpty(this.batchs))
        tempBatchs.add(map.get("lot"));
        if(StringUtils.isNotEmpty(map.get("date"))) {
                this.proDate=DateUtil.strToDate(map.get("date"), "yyyy-MM-dd");
        }
    }
    public boolean addPackProductStock(PackProductStockDate packProductStockDate){
        boolean Result=false;
        if(packProductStockDate.pi!=null&&packProductStockDate.pi.equals(this.pi)){
            if(packProductStockDate.StockId!=null&&packProductStockDate.StockId.equals(this.StockId)){
                Result=true;
                if(isLater(this.proDate,packProductStockDate.proDate))
                    this.proDate=packProductStockDate.proDate;
                if(!isLater(this.stockNewDate, packProductStockDate.stockNewDate)){
                    this.stockNewDate=packProductStockDate.stockNewDate;
                    this.stockNewQty=packProductStockDate.stockNewQty;
                    this.closing=packProductStockDate.closing;
                }
                if(!isLater(packProductStockDate.stockOldDate, this.stockOldDate)){
                    this.stockOldDate=packProductStockDate.stockOldDate;
                    this.stockOldQty=packProductStockDate.stockOldQty;
                    this.opening=packProductStockDate.opening;
                }
                if(!this.tempBatchs.contains(packProductStockDate.batchs)){
                    this.batchs=this.batchs+","+packProductStockDate.batchs;
                    this.tempBatchs.add(packProductStockDate.batchs);
                }
                this.receive+=packProductStockDate.receive;
                this.issue+=packProductStockDate.issue;
                this.sample+=packProductStockDate.sample;
                this.blend+=packProductStockDate.blend;
                this.others+=packProductStockDate.others;
            }
        }
        return Result;
    }
    
    public boolean addPackProductPi(PackProductStockDate packProductStockDate){
        boolean Result=false;
        if(packProductStockDate.pi!=null&&packProductStockDate.pi.equals(this.pi)){
            if(packProductStockDate.StockId!=null&&!packProductStockDate.StockId.equals(this.StockId)){
                Result=true;
                if(isLater(this.proDate,packProductStockDate.proDate))
                    this.proDate=packProductStockDate.proDate;
                this.opening+=packProductStockDate.opening;
                this.closing+=packProductStockDate.closing;
                for (String batchNo : packProductStockDate.tempBatchs) {
                    if(!this.tempBatchs.contains(batchNo)){
                        this.batchs=this.batchs+","+batchNo;
                        this.tempBatchs.add(batchNo);
                    }
                }
                this.receive+=packProductStockDate.receive;
                this.issue+=packProductStockDate.issue;
                this.sample+=packProductStockDate.sample;
                this.blend+=packProductStockDate.blend;
                this.others+=packProductStockDate.others;
                this.Nca+=packProductStockDate.Nca;
            }
        }
        return Result;
    }
    public Date getTransactionPordDate(StockTransaction transaction){
        if(transaction!=null&&transaction.stock!=null&&transaction.stock.batch!=null){
            String date = ExtUtil.unapply(transaction.stock.batch.ext).get("date");
            if(StringUtils.isNotEmpty(date))    {
                return DateUtil.strToDate(date, "yyyy-MM-dd");
            }
        }
        return null;
    }
    public String getTransactionbatch(StockTransaction transaction){
        if(transaction!=null&&transaction.stock!=null&&transaction.stock.batch!=null){
            String batch = ExtUtil.unapply(transaction.stock.batch.ext).get("lot");
            if(StringUtils.isNotEmpty(batch))   {
                return batch;
            }
        }
        return null;
    }
    public boolean isLater(Date date1,Date date2){
            if(date1!=null&&date1.after(date2))
                return true;
            else
                return false;
    }
    
    public boolean search(String pi,boolean local){
        if(((pi!=null||"".equals(pi))?(this.pi!=null&&this.pi.indexOf(pi)>0):true)&&isLoacl()==local){
            return true;
        }else{
            return false;
        }
    }
    public boolean isLoacl(){
        if(this.pi!=null&&this.pi.equals(this.refNo)){
            return true;
        }else{
            return false;
        }
    }
    public void ncaInit(){
        this.opening=this.Nca;
        this.closing=this.Nca;
        this.total=closing*kg;
        this.receive=new Double(0);
        this.issue=new Double(0);
        this.sample=new Double(0);
        this.blend=new Double(0);
        this.others=new Double(0);
    }
    public void Init(){
        this.others-=this.Nca;
        //this.opening-=this.Nca;
        if(this.closing!=0)
        this.closing-=this.Nca;
        this.total=closing*kg;
    }
    
    public String getPi() {
        return pi;
    }

    public void setPi(String pi) {
        this.pi = pi;
    }

    public String getCommodity() {
        return commodity;
    }

    public void setCommodity(String commodity) {
        this.commodity = commodity;
    }

    public Double getKg() {
        return kg;
    }

    public void setKg(Double kg) {
        this.kg = kg;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public Double getOpening() {
        return opening;
    }

    public void setOpening(Double opening) {
        this.opening = opening;
    }

    public Double getReceive() {
        return receive;
    }

    public void setReceive(Double receive) {
        this.receive = receive;
    }

    public Double getIssue() {
        return issue;
    }

    public void setIssue(Double issue) {
        this.issue = issue;
    }

    public Double getSample() {
        return sample;
    }

    public void setSample(Double sample) {
        this.sample = sample;
    }

    public Double getBlend() {
        return blend;
    }

    public void setBlend(Double blend) {
        this.blend = blend;
    }

    public Double getOthers() {
        return others;
    }

    public void setOthers(Double others) {
        this.others = others;
    }

    public Double getClosing() {
        return closing;
    }

    public void setClosing(Double closing) {
        this.closing = closing;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public Double getTotal() {
        return total;
    }

    public void setTotal(Double total) {
        this.total = total;
    }

    public String getUom() {
        return uom;
    }

    public void setUom(String uom) {
        this.uom = uom;
    }

    public Date getProDate() {
        return proDate;
    }

    public void setProDate(Date proDate) {
        this.proDate = proDate;
    }

    public String getBatchs() {
        return batchs;
    }

    public void setBatchs(String batchs) {
        this.batchs = batchs;
    }

    public String getRefNo() {
        return refNo;
    }

    public void setRefNo(String refNo) {
        this.refNo = refNo;
    }

    public String getMaterialCode() {
        return materialCode;
    }

    public void setMaterialCode(String materialCode) {
        this.materialCode = materialCode;
    }

    public String getStockId() {
        return StockId;
    }

    public void setStockId(String stockId) {
        StockId = stockId;
    }

    public Double getStockOldQty() {
        return stockOldQty;
    }

    public void setStockOldQty(Double stockOldQty) {
        this.stockOldQty = stockOldQty;
    }

    public Date getStockOldDate() {
        return stockOldDate;
    }

    public void setStockOldDate(Date stockOldDate) {
        this.stockOldDate = stockOldDate;
    }

    public Double getStockNewQty() {
        return stockNewQty;
    }

    public void setStockNewQty(Double stockNewQty) {
        this.stockNewQty = stockNewQty;
    }

    public Date getStockNewDate() {
        return stockNewDate;
    }

    public void setStockNewDate(Date stockNewDate) {
        this.stockNewDate = stockNewDate;
    }

    public List<String> getTempBatchs() {
        return tempBatchs;
    }

    public void setTempBatchs(List<String> tempBatchs) {
        this.tempBatchs = tempBatchs;
    }
     public Object Clone() throws CloneNotSupportedException {
            //鐩存帴璋冪敤鐖剁被鐨刢lone()鏂规硶,杩斿洖鍏嬮殕鍓湰
            return super.clone();
        }
    
}
