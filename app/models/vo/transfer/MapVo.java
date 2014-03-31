/** * MapVo.java 
* Created on 2013-4-17 上午9:00:45 
*/

package models.vo.transfer;

import java.math.BigDecimal;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import com.avaje.ebean.annotation.Sql;

/** 
 * <p>Project: CloudWMS</p> 
 * <p>Title: MapVo.java</p> 
 * <p>Description: </p> 
 * <p>Copyright (c) 2013 Wilmar Consultancy Services</p>
 * <p>All Rights Reserved.</p>
 * @author <a href="mailto:liyong@wcs-global.com">liyong</a>
 */
 
@JsonIgnoreProperties(ignoreUnknown = true)
public class MapVo {
  public String key;
  public String descripton;   
  public BigDecimal qty;
  
}
