/** * ChainList.java 
* Created on 2013-6-3 下午8:56:42 
*/

package utils;

import java.util.ArrayList;
import java.util.List;

/** 
 * <p>Project: cloudwms</p> 
 * <p>Title: ChainList.java</p> 
 * <p>Description: </p> 
 * <p>Copyright (c) 2013 Wilmar Consultancy Services</p>
 * <p>All Rights Reserved.</p>
 * @author <a href="mailto:liyong@wcs-global.com">liyong</a>
 */

public class ChainList<E> extends ArrayList<E>{

	public  ChainList<E> chainAdd(E e){
		 this.add(e);
		 return this;
	}
	
	

}
