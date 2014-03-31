/** * TransferPlanVo.java 
* Created on 2013-6-3 下午4:12:42 
*/

package models.vo.transfer;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

/** 
 * <p>Project: cloudwms</p> 
 * <p>Title: TransferPlanVo.java</p> 
 * <p>Description: </p> 
 * <p>Copyright (c) 2013 Wilmar Consultancy Services</p>
 * <p>All Rights Reserved.</p>
 * @author <a href="mailto:liyong@wcs-global.com">liyong</a>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class TransferPlanVo {
	public String id;
	public String pino;
	public List<String> batchId;
	public BigDecimal transferQty;
	public Date transferDate;
	public String proDate;
}
