/** * UserSearchVo.java 
* Created on 2013-5-15 下午3:54:33 
*/

package models.vo.setup;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

/** 
 * 
* <p>Project: CloudWMS</p> 
* <p>Title: UserSearchVo.java</p> 
* <p>Description: </p> 
* <p>Copyright (c) 2013 Wilmar Consultancy Services</p>
* <p>All Rights Reserved.</p>
* @author <a href="mailto:chenaihong@wcs-global.com">Chen Aihong</a>
 */

@JsonIgnoreProperties(ignoreUnknown = true)
public class UserSearchVo {
	public String name;
	public RoleVo roleVo = new RoleVo();

}
