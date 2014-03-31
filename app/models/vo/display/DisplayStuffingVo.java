/** * DisplayStuffingVo.java 
* Created on 2013-5-30 下午5:41:15 
*/

package models.vo.display;

import java.sql.Timestamp;
import java.util.Date;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

/** 
 * 
* <p>Project: CloudWMS</p> 
* <p>Title: DisplayStuffingVo.java</p> 
* <p>Description: </p> 
* <p>Copyright (c) 2013 Wilmar Consultancy Services</p>
* <p>All Rights Reserved.</p>
* @author <a href="mailto:chenaihong@wcs-global.com">Chen Aihong</a>
 */

@JsonIgnoreProperties(ignoreUnknown = true)
public class DisplayStuffingVo {
	public String loadingBay;
	public String piNo;
	public String piQty;
	public String transp;
	public String commodityName;
	public String feeder;
	public String stuffingPlanQty;
	public Timestamp prodDate;
	public String containerNo;
	public String sealNo;
	public Date closedDate;
	public Date closeTime;
	public String remarks;
	public String sgRemarks;
	public boolean atLoadingBay = false;
	public double Qty;
}
