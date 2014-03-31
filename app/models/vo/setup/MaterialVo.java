package models.vo.setup;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import models.Material;
import models.MaterialUom;
import models.OrderItem;
import models.Owner;
import models.UomCapacityPoint;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import utils.CrudUtil;
import utils.EmptyUtil;
import utils.Excel.ExcelObj;
import utils.exception.CustomException;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.ExpressionList;
import com.avaje.ebean.SqlRow;

/** 
* <p>Project: CloudWMS</p> 
* <p>Title: MaterialVo.java</p> 
* <p>Description: </p> 
* <p>Copyright (c) 2013 Wilmar Consultancy Services</p>
* <p>All Rights Reserved.</p>
* @author <a href="mailto:wangyang6@wcs-global.com">Wang Yang</a> 
*/
@JsonIgnoreProperties(ignoreUnknown = true)
public class MaterialVo extends ExcelObj {
   //baseuomPoint id
	public String id;
	// template
	public String matCode;
	// template
	public String matDesc;
	// template
	public BigDecimal convertRate;
	// template
	public String fromuom;
	// template
	public String touom;
	public BigDecimal uomCap;
	// public String active;  
	public String uomId;
	public String customId;
	public String customName;
	public Timestamp lastUpdateDate;
	public Timestamp matLastUpdateDate;
	public String capType;

	/**
	 * 
	 * @param matUom
	 * @return
	 */
	public static List<MaterialVo> getMaterialVo(MaterialVo materialVo) {
		List<MaterialVo> matVos = new ArrayList<MaterialVo>();
		ExpressionList<UomCapacityPoint> el = Ebean.find(UomCapacityPoint.class)
			.where().join("materialUom","uomCode")
			.where().join("materialUom.material","materialCode,materialName,updatedAt")
			.where().join("materialUom.material.owner","id,ownerName")
//			.fetch("materialUom","uomCode")
//			.fetch("materialUom.material","materialCode,materialName,updatedAt")
//			.fetch("materialUom.material.owner","id,ownerName")
			.where()
			.eq("materialUom.material.deleted", false).eq("materialUom.baseUom", true);
		if (materialVo.customId != null && !"".equals(materialVo.customId.trim())) {
			el.contains("materialUom.material.owner.id", materialVo.customId);
		}

		if (materialVo.matCode != null && !"".equals(materialVo.matCode.trim())) {
			el.contains("materialUom.material.materialCode",materialVo.matCode);
		}
		if (materialVo.matDesc != null && !"".equals(materialVo.matDesc.trim())) {
			el.contains("materialUom.material.materialName",  materialVo.matDesc);
		}
		List<UomCapacityPoint> uomCapacityPoints = el.order("updatedAt descending").findList();
		if (uomCapacityPoints != null && !uomCapacityPoints.isEmpty()) {
			for (UomCapacityPoint uomPonint : uomCapacityPoints) {
				MaterialVo matVo = new MaterialVo();
				matVo.id = String.valueOf(uomPonint.id);
				matVo.customId = uomPonint.materialUom.material.owner.id.toString();
				matVo.customName = uomPonint.materialUom.material.owner.ownerName;
				matVo.matCode = uomPonint.materialUom.material.materialCode;
				matVo.matDesc = uomPonint.materialUom.material.materialName;
				matVo.fromuom = uomPonint.materialUom.uomCode;
				matVo.uomCap = uomPonint.capacityPoint;
				matVo.capType = uomPonint.capacityType;
				matVo.matLastUpdateDate = uomPonint.materialUom.material.updatedAt;
				matVos.add(matVo);
			}
		}
		return matVos;

	}

	/**
	 * 
	 * @param material
	 * @return
	 */
	public static List<MaterialVo> getMaterialUomVo(Material material) {
		List<MaterialVo> matUomVos = new ArrayList<MaterialVo>();
		List<UomCapacityPoint> matPointEntitys = Ebean.find(UomCapacityPoint.class).fetch("materialUom").where().eq("deleted", false)
			.eq("materialUom.deleted", false).eq("materialUom.material", material).findList();
		UomCapacityPoint baseUomPoint = null;
		if (matPointEntitys != null && !matPointEntitys.isEmpty()) {
			for (UomCapacityPoint uomPoint : matPointEntitys) {
				if (uomPoint.materialUom.baseUom) {
					baseUomPoint = uomPoint;
				}
			}
			for (UomCapacityPoint uomPoint : matPointEntitys) {
				if (!uomPoint.materialUom.baseUom) {
					MaterialVo matVo = new MaterialVo();
					matVo.uomId = String.valueOf(uomPoint.id);
					matVo.fromuom = baseUomPoint.materialUom.uomCode;
					matVo.touom = uomPoint.materialUom.uomCode;
					matVo.convertRate = uomPoint.materialUom.qtyOfBaseNum;
					matVo.uomCap = uomPoint.capacityPoint;
					matVo.lastUpdateDate = uomPoint.materialUom.updatedAt;
					matUomVos.add(matVo);
				}
			}
		}
		return matUomVos;

	}

	/**
	 * 
	 * @param materialVo
	 * @return
	 * @throws Exception 
	 */
	public static void setMaterial(MaterialVo materialVo) throws CustomException {
		Material material = null;
		MaterialUom matUom = null;
		UomCapacityPoint uomCapacityPoint = null;
		List<Material> mats = null;
		ExpressionList<Material> el = Ebean.find(Material.class).where().eq("materialCode", materialVo.matCode).eq("deleted", false);
		// true 为保存 false为修改
		boolean flag = true;
		if (materialVo.id != null && !"".equals(materialVo.id.trim())) {
			uomCapacityPoint = Ebean.find(UomCapacityPoint.class).where().eq("id", materialVo.id).findUnique();
			List<OrderItem> orderItemList = Ebean.find(OrderItem.class).where().eq("material.id", uomCapacityPoint.materialUom.material.id).eq("deleted", false).findList();
			isExcepiton(orderItemList,"Edit fail,this material has been used by PI!");
			matUom = uomCapacityPoint.materialUom;
			material = matUom.material;
			flag = false;
			mats = el.ne("id", uomCapacityPoint.materialUom.material.id).findList();
			isExcepiton(mats," Database have the same material code");
		} else {
			mats = el.findList();
			isExcepiton(mats," Database have the same material code");
			material = new Material();
			matUom = new MaterialUom();
			uomCapacityPoint = new UomCapacityPoint();
		}
		Owner owner = Ebean.find(Owner.class).where().eq("deleted", false).eq("id", materialVo.customId).findUnique();
		BigDecimal bigOne = new BigDecimal("1");
		// Material
		material.owner = owner;
		material.materialCode = materialVo.matCode;
		material.materialName = materialVo.matDesc;
		material.weightUnitCode = null;
		material.grossWeight = null;
		material.netWeight = bigOne;
		material.volumnUnitCode = null;
		material.volumn = null;
		material.sourceType = null;
		// MaterialUom
		matUom.warehouse = owner.warehouse;
		matUom.material = material;
		matUom.uomCode = materialVo.fromuom;
		matUom.baseUom = true;
		matUom.qtyOfBaseNum = bigOne;
		matUom.qtyOfBaseDenom = bigOne;

		// uomCapacityPoint
		uomCapacityPoint.materialUom = matUom;
		uomCapacityPoint.capacityType = "Kg";
		uomCapacityPoint.capacityPoint = materialVo.uomCap;
		if (flag) {
			CrudUtil.save(material);
			CrudUtil.save(matUom);
			CrudUtil.save(uomCapacityPoint);
		} else {
			CrudUtil.update(material);
			CrudUtil.update(matUom);
			CrudUtil.update(uomCapacityPoint);
			// 如果更新material 对应的uom 和 point 也做更新
			List<UomCapacityPoint> uomPonints = Ebean.find(UomCapacityPoint.class).fetch("materialUom").where().eq("materialUom.baseUom", false)
				.eq("materialUom.material", material).findList();
			if (uomPonints != null && !uomPonints.isEmpty()) {
				for (UomCapacityPoint uomPonint : uomPonints) {
					uomPonint.capacityPoint = uomPonint.materialUom.qtyOfBaseNum.multiply(uomCapacityPoint.capacityPoint);
					CrudUtil.update(uomPonint);
				}
			}
		}

	}

	/**
	 * 
	 * @param materialVo
	 * @throws Exception 
	 */
	public static void setUom(MaterialVo materialVo) throws CustomException {
		//throw new Exception(" Database have the same material code");
		MaterialUom matUom = null;
		UomCapacityPoint uomCapacityPoint = null;
		UomCapacityPoint baseUomPoint = null;
		List<MaterialUom> uoms = null;
		baseUomPoint = Ebean.find(UomCapacityPoint.class).where().eq("id", materialVo.id).findUnique();
		ExpressionList<MaterialUom> el = Ebean.find(MaterialUom.class).where().eq("deleted", false).eq("material.id", baseUomPoint.materialUom.material.id).eq("uomCode", materialVo.touom);
		//true为新增false为修改
		boolean flag = true;
		if (materialVo.uomId != null && !"".equals(materialVo.uomId.trim())) {
			uomCapacityPoint = Ebean.find(UomCapacityPoint.class).where().eq("id", materialVo.uomId).findUnique();
			checkUomUsed(uomCapacityPoint.materialUom.id.toString());
			matUom = uomCapacityPoint.materialUom;
			uoms = el.ne("id", uomCapacityPoint.materialUom.id).findList();
			isExcepiton(uoms, " This material has the same uom code");
			flag = false;
		} else {
			uoms = el.findList();
			isExcepiton(uoms, " This material has the same uom code");
			matUom = new MaterialUom();
			uomCapacityPoint = new UomCapacityPoint();
		}

		// MaterialUom
		BigDecimal bigOne = new BigDecimal("1");
		matUom.warehouse = baseUomPoint.materialUom.warehouse;
		matUom.material = baseUomPoint.materialUom.material;
		matUom.uomCode = materialVo.touom;
		matUom.baseUom = false;
		matUom.qtyOfBaseNum = materialVo.convertRate;
		matUom.qtyOfBaseDenom = bigOne;

		// uomCapacityPoint
		uomCapacityPoint.materialUom = matUom;
		uomCapacityPoint.capacityType = "Kg";
		uomCapacityPoint.capacityPoint = materialVo.convertRate.multiply(baseUomPoint.capacityPoint);
		if (flag) {
			CrudUtil.save(matUom);
			CrudUtil.save(uomCapacityPoint);
		} else {
			CrudUtil.update(matUom);
			CrudUtil.update(uomCapacityPoint);
		}

	}
    
	public  static <T> void isExcepiton(List<T> list,String message) throws CustomException{
		if (list != null && !list.isEmpty()) {
			throw new CustomException(message);
		}
	}
	
	   /**
     * 检查这个物料 单位是否被用过
     * @param id
     * @throws CustomException
     */
	public static void checkUomUsed(String uomId) throws CustomException {
		String sql  = "select id from t_order_item oi where oi.deleted=false and (oi.ext::hstore ->'qtyPerPalletUom' = '"+uomId+"' or oi.settlement_uom_id ='"+uomId+"')";
		 List<SqlRow> sqlRows = Ebean.createSqlQuery(sql).findList();
		 if(EmptyUtil.isNotEmptyList(sqlRows)){
			 throw new CustomException("delete fail,this UOM has been used by PI!");
		 }
	}
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getUomId() {
		return uomId;
	}

	public void setUomId(String uomId) {
		this.uomId = uomId;
	}

	public String getCustomId() {
		return customId;
	}

	public void setCustomId(String customId) {
		this.customId = customId;
	}

	public String getCustomName() {
		return customName;
	}

	public void setCustomName(String customName) {
		this.customName = customName;
	}

	public String getMatCode() {
		return matCode;
	}

	public void setMatCode(String matCode) {
		this.matCode = matCode;
	}

	public String getMatDesc() {
		return matDesc;
	}

	public void setMatDesc(String matDesc) {
		this.matDesc = matDesc;
	}

	public String getFromuom() {
		return fromuom;
	}

	public void setFromuom(String fromuom) {
		this.fromuom = fromuom;
	}

	public BigDecimal getUomCap() {
		return uomCap;
	}

	public void setUomCap(BigDecimal uomCap) {
		this.uomCap = uomCap;
	}

	public String getTouom() {
		return touom;
	}

	public void setTouom(String touom) {
		this.touom = touom;
	}

	public BigDecimal getConvertRate() {
		return convertRate;
	}

	public void setConvertRate(BigDecimal convertRate) {
		this.convertRate = convertRate;
	}

	public Timestamp getLastUpdateDate() {
		return lastUpdateDate;
	}

	public void setLastUpdateDate(Timestamp lastUpdateDate) {
		this.lastUpdateDate = lastUpdateDate;
	}

	public String getCapType() {
		return capType;
	}

	public void setCapType(String capType) {
		this.capType = capType;
	}

	
}

 
 
