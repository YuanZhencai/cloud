/** * TemperingRoomVo.java 
* Created on 2013-6-4 上午10:04:55 
*/

package models.vo.report;

import java.sql.Timestamp;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

/** 
 * <p>Project: cloudwms</p> 
 * <p>Title: TemperingRoomVo.java</p> 
 * <p>Description: </p> 
 * <p>Copyright (c) 2013 Wilmar Consultancy Services</p>
 * <p>All Rights Reserved.</p>
 * @author <a href="mailto:liyong@wcs-global.com">liyong</a>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class TemperingRoomVo {
	public String inOrOut;
	public Timestamp executeDate;
}
