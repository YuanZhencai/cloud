/** * OrderOutBound.java 
* Created on 2013-3-20 上午10:07:34 
*/

package models.vo.transfer;

import java.util.Date;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

/** 
 * <p>Project: CloudWMS</p> 
 * <p>Title: OrderOutBound.java</p> 
 * <p>Description: </p> 
 * <p>Copyright (c) 2013 Wilmar Consultancy Services</p>
 * <p>All Rights Reserved.</p>
 * @author <a href="mailto:liyong@wcs-global.com">liyong</a>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class TPSearch {
   public String planItemId;
   public String piNO;
   public String sgPiNO;
   public String piStatus;
   public Date  transferFromDate;
   public Date  transferToDate;
   public String  planStatus;
   public String  transferType;
public TPSearch() {
	super();
	// TODO Auto-generated constructor stub
}
   

   
}
