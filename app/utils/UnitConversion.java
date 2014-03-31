package utils;

import java.math.BigDecimal;

import models.MaterialUom;

/** 
* <p>Project: CloudWMS</p> 
* <p>Title: UnitConversion.java</p> 
* <p>Description: </p> 
* <p>Copyright (c) 2013 Wilmar Consultancy Services</p>
* <p>All Rights Reserved.</p>
* @author <a href="mailto:wangyang6@wcs-global.com">Wang Yang</a> 
*/
public class UnitConversion {

	public static BigDecimal SkuToQuantity(String quantityUOMId, String skuUOMId) {
		// 1box = X kg,返回X
		MaterialUom quantityUom = MaterialUom.find().byId(quantityUOMId);
		MaterialUom skuUom = MaterialUom.find().byId(skuUOMId);
		BigDecimal amount = new BigDecimal(0);
		BigDecimal amount2 = new BigDecimal(0);
		amount = quantityUom.qtyOfBaseDenom.multiply(skuUom.qtyOfBaseNum);
		amount2 = quantityUom.qtyOfBaseNum.multiply(skuUom.qtyOfBaseDenom);
		return amount.divide(amount2, 2, BigDecimal.ROUND_CEILING);// 没有小数位2->4
	}

	public static BigDecimal SkuToQuantity(MaterialUom quantityUom, MaterialUom skuUom) {
		// 1box = X kg,返回X
		BigDecimal amount = new BigDecimal(0);
		BigDecimal amount2 = new BigDecimal(0);
		amount = quantityUom.qtyOfBaseDenom.multiply(skuUom.qtyOfBaseNum);
		amount2 = quantityUom.qtyOfBaseNum.multiply(skuUom.qtyOfBaseDenom);
		return amount.divide(amount2, 2, BigDecimal.ROUND_CEILING);// 没有小数位2->4
	}

	public static double quantityToSku(String quantityUOMId, String skuUOMId) {
		// 1Kg = X box;返回box
		MaterialUom quantityUom = MaterialUom.find().byId(quantityUOMId);
		MaterialUom skuUom = MaterialUom.find().byId(skuUOMId);
		double amount = ((quantityUom.qtyOfBaseDenom.doubleValue() * skuUom.qtyOfBaseNum.doubleValue()))
				/ ((skuUom.qtyOfBaseDenom.doubleValue() * quantityUom.qtyOfBaseNum.doubleValue()));
		return amount;
	}

	/*
	 * 计算物料单位换算比例
	 */
	public static double returnComparing(String uom, String sku, String materialId) {
		MaterialUom materialUom = MaterialUom.find().where().eq("uomCode", uom).eq("material.id", materialId)
				.eq("material.owner.warehouse.id", SessionSearchUtil.searchWarehouse().id.toString()).eq("material.deleted", false)
				.eq("material.owner.warehouse.deleted", false).eq("deleted", false).findUnique();
		MaterialUom materialUomSku = MaterialUom.find().where().eq("uomCode", sku).eq("material.id", materialId)
				.eq("material.owner.warehouse.id", SessionSearchUtil.searchWarehouse().id.toString()).eq("material.deleted", false)
				.eq("material.owner.warehouse.deleted", false).eq("deleted", false).findUnique();
		if (materialUom != null && materialUomSku != null) {
			double comparing = (materialUom.qtyOfBaseDenom.doubleValue() / materialUom.qtyOfBaseNum.doubleValue())
					/ (materialUomSku.qtyOfBaseDenom.doubleValue() / materialUomSku.qtyOfBaseNum.doubleValue());
			return comparing;
		} else {
			return 1;
		}
	}
}
