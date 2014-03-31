/** * RoleVo.java 
* Created on 2013-5-9 上午9:34:20 
*/

package models.vo.setup;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import models.Role;

/** 
 * 
* <p>Project: CloudWMS</p> 
* <p>Title: RoleVo.java</p> 
* <p>Description: </p> 
* <p>Copyright (c) 2013 Wilmar Consultancy Services</p>
* <p>All Rights Reserved.</p>
* @author <a href="mailto:chenaihong@wcs-global.com">Chen Aihong</a>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class RoleVo {
	public String id;
	public String nameKey;
	public boolean show = true;
	public boolean ifSelect = false;

	public void initRole(Role role) {
		id = role.id.toString();
		nameKey = role.nameKey;
	}

}
