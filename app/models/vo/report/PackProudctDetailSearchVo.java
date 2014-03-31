/** * PackProudctDetailSearchVo.java 
* Created on 2013-8-5 下午2:50:10 
*/

package models.vo.report;

import java.util.Date;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

import utils.DateUtil;

/** 
 * <p>Project: CloudWMS</p> 
 * <p>Title: PackProudctDetailSearchVo.java</p> 
 * <p>Description: </p> 
 * <p>Copyright (c) 2013 Wilmar Consultancy Services</p>
 * <p>All Rights Reserved.</p>
 * @author <a href="mailto:liyong@wcs-global.com">liyong</a>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class PackProudctDetailSearchVo {
	public Date dateTimeFrom;
	public Date dateTimeTo;
	public String localOrExport;
	public String piNo;
	@JsonProperty("dateTimeFrom")
	public void getDateTimeFrom(String time){
		this.dateTimeFrom=DateUtil.getDate(time);
	}
	@JsonProperty("dateTimeTo")
	public void getDateTimeTo(String time){
		this.dateTimeTo=DateUtil.getDate(time);
	}
}
