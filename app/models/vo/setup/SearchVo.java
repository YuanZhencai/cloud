/** * SerchVo.java 
* Created on 2013-5-7 下午4:40:47 
*/

package models.vo.setup;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

/** 
 * <p>Project: CloudWMS</p> 
 * <p>Title: SerchVo.java</p> 
 * <p>Description: </p> 
 * <p>Copyright (c) 2013 Wilmar Consultancy Services</p>
 * <p>All Rights Reserved.</p>
 * @author <a href="mailto:liyong@wcs-global.com">liyong</a>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class SearchVo {
   public String  codeCat;
   public String  abbr;
   public String  desc;
   public String  parent;
   public String  magic;
}
