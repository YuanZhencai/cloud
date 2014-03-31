package models.vo.inbound;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import models.MaterialUom;
/**
 * 
* <p>Project: CloudWMS</p> 
* <p>Title: UOMVo.java</p> 
* <p>Description: </p> 
* <p>Copyright (c) 2013 Wilmar Consultancy Services</p>
* <p>All Rights Reserved.</p>
* @author <a href="mailto:chenaihong@wcs-global.com">Chen Aihong</a>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class UOMVo {
	public String id;
	public String uomCode;

	public void inMaterialUom(MaterialUom materialUom) {
		id = materialUom.id.toString();
		uomCode = materialUom.uomCode;
	}
}
