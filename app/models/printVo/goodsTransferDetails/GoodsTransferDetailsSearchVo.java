/** * GoodsTransferDetailsSearchVo.java 
* Created on 2013-8-12 下午4:00:16 
*/

package models.printVo.goodsTransferDetails;

import java.util.Date;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

import utils.DateUtil;

/** 
 * <p>Project: CloudWMS</p> 
 * <p>Title: GoodsTransferDetailsSearchVo.java</p> 
 * <p>Description: </p> 
 * <p>Copyright (c) 2013 Wilmar Consultancy Services</p>
 * <p>All Rights Reserved.</p>
 * @author <a href="mailto:liyong@wcs-global.com">liyong</a>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class GoodsTransferDetailsSearchVo {
	public Date dateTimeFrom;
	public Date dateTimeTo;
 
 
	 

}
