/** * UserVo.java 
* Created on 2013-5-9 上午9:34:05 
*/

package models.vo.setup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import models.User;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import utils.ExtUtil;

/** 
 * 
* <p>Project: CloudWMS</p> 
* <p>Title: UserVo.java</p> 
* <p>Description: </p> 
* <p>Copyright (c) 2013 Wilmar Consultancy Services</p>
* <p>All Rights Reserved.</p>
* @author <a href="mailto:chenaihong@wcs-global.com">Chen Aihong</a>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserVo {
	public String id;
	public String name;
	public String password;
	public String rePassword;
	public String email;
	public String telNumber;
	public String updateAt;
	public boolean show = true;
	public List<RoleVo> roles = new ArrayList<RoleVo>();
	public String roleNameKeys = "";
	public WarehouseVo warehouseVo = new WarehouseVo();
	public CompanyVo companyVo = new CompanyVo();

	public void setExt(String ext) {
		HashMap<String, String> Ext = ExtUtil.unapply(ext);
		email = Ext.get("email");
		telNumber = Ext.get("telNumber");
	}

	public String returnExt() {
		HashMap<String, String> Ext = new HashMap<String, String>();
		Ext.put("email", email != null ? email : "");
		Ext.put("telNumber", telNumber != null ? telNumber : "");
		return ExtUtil.apply(Ext);
	}

	public void setUser(User user) {
		id = user.id.toString();
		name = user.name;
		password = user.passwordHash;
		rePassword = user.passwordHash;
		setExt(user.ext);
	}

}
