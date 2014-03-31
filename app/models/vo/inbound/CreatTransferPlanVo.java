/** * CreatTransferPlanVo.java 
* Created on 2013-5-20 上午9:56:28 
*/

package models.vo.inbound;

import java.math.BigDecimal;

import javax.persistence.Entity;

import com.avaje.ebean.annotation.Sql;

/** 
 * <p>Project: CloudWMS</p> 
 * <p>Title: CreatTransferPlanVo.java</p> 
 * <p>Description: </p> 
 * <p>Copyright (c) 2013 Wilmar Consultancy Services</p>
 * <p>All Rights Reserved.</p>
 * @author <a href="mailto:yourname@wcs-global.com">Your Name</a>
 */
@Entity
@Sql
public class CreatTransferPlanVo {
	public String planItemId;
	public BigDecimal qtySum;
}
