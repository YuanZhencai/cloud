package models.vo.outbound;

import java.util.HashMap;
import java.util.List;

import org.codehaus.jackson.annotate.JsonIgnore;

import utils.ExtUtil;
import models.Material;
import models.MaterialUom;
import models.vo.inbound.UOMVo;

public class DaseData {
	public double qty;
	public UOMVo UOM;
	public UOMVo SKUUOM;
	public String ConNo;
	@JsonIgnore
	public Material material;
	public void setExt(String ext){
		HashMap<String, String> Ext = ExtUtil.unapply(ext);
		//System.out.println(ext);
		ConNo=Ext.get("containerNo");
		String uomcode = Ext.get("qtyPerPalletUom");
		System.out.println(uomcode);
		List<MaterialUom> materialUoms = MaterialUom.find().where().eq("material", material).eq("deleted", false).eq("id", uomcode).findList();
		if(materialUoms.size()>0){
		SKUUOM = new UOMVo();
		SKUUOM.inMaterialUom(materialUoms.get(0));
		}
	}
}
