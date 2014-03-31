package Service.Vo;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import utils.ExtUtil;
import utils.SessionSearchUtil;

import models.MaterialUom;
@JsonIgnoreProperties(ignoreUnknown = true)
public class SUomVo {
    public int sortNo;
    public String uom;
    public String name;
    public boolean baseUom;
    public double qtyOfBaseUomNum;
    public double qtyOfBaseUomDenom;
    public String remarks;
    public Map<String,String> ext=new HashMap<String, String>();
    public void get(MaterialUom materialUom){
        materialUom.warehouse=SessionSearchUtil.searchWarehouse();
        materialUom.uomCode=name;
        materialUom.qtyOfBaseDenom=BigDecimal.valueOf(qtyOfBaseUomDenom);
        materialUom.qtyOfBaseNum=BigDecimal.valueOf(qtyOfBaseUomNum);
        materialUom.baseUom=baseUom;
        materialUom.remarks=remarks;
        materialUom.uomName=uom;
        materialUom.ext=ExtUtil.updateExt(materialUom.ext, this.ext);
    }
}
