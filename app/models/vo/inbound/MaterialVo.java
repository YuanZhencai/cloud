/** * MaterialVo.java 
* Created on 2013-4-16 下午5:00:54 
*/

package models.vo.inbound;

import models.Material;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

/** 
 * 
* <p>Project: CloudWMS</p> 
* <p>Title: MaterialVo.java</p> 
* <p>Description: </p> 
* <p>Copyright (c) 2013 Wilmar Consultancy Services</p>
* <p>All Rights Reserved.</p>
* @author <a href="mailto:chenaihong@wcs-global.com">Chen Aihong</a>
 */

@JsonIgnoreProperties(ignoreUnknown = true)
public class MaterialVo {

	public String materialId;
	public String materialCode;
	public String materialName;
	public String netWeight;
	public String codeAndDesc = "";

	public void initMaterial(Material material) {
		materialId = material.id.toString();
		materialCode = material.materialCode;
		materialName = material.materialName;
		if(material.netWeight!=null)
		netWeight = String.valueOf(material.netWeight.setScale(2));
		if (material.materialName != null && !"".equals(material.materialName) && material.materialCode != null && !"".equals(material.materialCode)) {
			codeAndDesc = material.materialCode + "|" + material.materialName;
		}
	}

}
