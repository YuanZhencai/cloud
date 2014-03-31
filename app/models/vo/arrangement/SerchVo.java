/** * serchVo.java 
* Created on 2013-4-22 上午11:15:59 
*/

package models.vo.arrangement;

import java.util.Date;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

/** 
 * <p>Project: CloudWMS</p> 
 * <p>Title: serchVo.java</p> 
 * <p>Description: </p> 
 * <p>Copyright (c) 2013 Wilmar Consultancy Services</p>
 * <p>All Rights Reserved.</p>
 * @author <a href="mailto:liyong@wcs-global.com">liyong</a>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class SerchVo {
	public String planItemId;
	public String piNo;
	public String sgPiNo;
	public String piStatus;
	public Date beginDate;
	public Date endDate;

	public SerchVo() {
		super();
		// TODO Auto-generated constructor stub
	}

}
