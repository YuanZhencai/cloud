/** * SearchVo.java 
* Created on 2013-6-22 上午10:56:39 
*/

package models.vo.adjustment;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

/** 
 * <p>Project: CloudWMS</p> 
 * <p>Title: SearchVo.java</p> 
 * <p>Description: </p> 
 * <p>Copyright (c) 2013 Wilmar Consultancy Services</p>
 * <p>All Rights Reserved.</p>
 * @author <a href="mailto:liyong@wcs-global.com">liyong</a>
 */

@JsonIgnoreProperties(ignoreUnknown = true)
public class SearchVo {
	
		  public String storageType;
		  public String area;
		  public String bin;
		  public String piNo;
		  public String batchNo;
		  public String date;
}
