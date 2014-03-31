/** * PackProductStockSearchVo.java 
* Created on 2013-6-5 下午4:18:34 
*/

package models.vo.report;

import java.util.Date;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

import utils.DateUtil;

/** 
 * <p>Project: CloudWMS</p> 
 * <p>Title: PackProductStockSearchVo.java</p> 
 * <p>Description: </p> 
 * <p>Copyright (c) 2013 Wilmar Consultancy Services</p>
 * <p>All Rights Reserved.</p>
 * @author <a href="mailto:chenaihong@wcs-global.com">ChenAi hong</a>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class PackProductStockSearchVo {
	public Date dateTimeFrom;
	public Date dateTimeTo;
	public String localOrExport;
	public String piNo;
	public boolean isNca;
	@JsonProperty("dateTimeFrom")
	public void getDateTimeFrom(String time){
		this.dateTimeFrom=DateUtil.getDate(time);
	}
	@JsonProperty("dateTimeTo")
	public void getDateTimeTo(String time){
		this.dateTimeTo=DateUtil.getDate(time);
	}
}
