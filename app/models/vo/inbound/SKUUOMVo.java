package models.vo.inbound;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import models.MaterialUom;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SKUUOMVo {
	public String id;
	public String uomCode;

	public void inMaterialUom(MaterialUom materialUom) {
		id = materialUom.id.toString();
		uomCode = materialUom.uomCode;
	}
}
