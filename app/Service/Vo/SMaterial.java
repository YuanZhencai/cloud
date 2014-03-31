package Service.Vo;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import utils.DateUtil;
import utils.ExtUtil;
import utils.SessionSearchUtil;

import models.Material;
import models.MaterialUom;
import models.OrderItem;
import models.Owner;
@JsonIgnoreProperties(ignoreUnknown = true)
public class SMaterial {
    public String owner;
    public String material;
    public String name;
    public String weightUnit;
    public double gressWeight;
    public double netWeight;
    public String volumeUnit;
    public double volunme;
    public List<SUomVo> uoms=new ArrayList<SUomVo>();
    public String remarks="";
    public Map<String ,String> ext=new HashMap<String, String>();
    public void setValueMaterial(Material material){
            List<Owner> owners = Owner.find().where().eq("deleted", false).eq("ownerName", this.owner).eq("warehouse", SessionSearchUtil.searchWarehouse()).findList();
            System.out.println("*********************************"+owners.size());
            if(owners.size()>0)
            material.owner=owners.get(0);
            material.materialCode=this.material;
            material.materialName=this.name;
            material.sourceType="T001";
            material.weightUnitCode=this.weightUnit;
            material.grossWeight=BigDecimal.valueOf(this.gressWeight);
            material.netWeight=BigDecimal.valueOf(this.netWeight);
            material.volumnUnitCode=this.volumeUnit;
            material.volumn=BigDecimal.valueOf(this.volunme);
            material.remarks=this.remarks;
            material.ext=ExtUtil.updateExt(material.ext, this.ext);
            List<MaterialUom> materialUoms = MaterialUom.find().where().eq("deleted", false).eq("material", material).eq("material.deleted", false).findList();
            for (MaterialUom materialUom : materialUoms) {
                materialUom.deleted=true;
                materialUom.updatedAt=DateUtil.currentTimestamp();
                materialUom.updatedBy=SessionSearchUtil.searchUser().id;
                materialUom.update();
            }
            for (SUomVo suomVo : this.uoms) {
                 MaterialUom materialUom = new  MaterialUom();
                 suomVo.get(materialUom);
                 materialUom.material=material;
                 material.items.add(materialUom);
            }
    }
}
