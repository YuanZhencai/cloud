/** * MyException.java 
* Created on 2013-6-28 上午11:16:50 
*/

package utils.exception;

/** 
 * <p>Project: CloudWMS</p> 
 * <p>Title: MyException.java</p> 
 * <p>Description: </p> 
 * <p>Copyright (c) 2013 Wilmar Consultancy Services</p>
 * <p>All Rights Reserved.</p>
 * @author <a href="mailto:liyong@wcs-global.com">liyong</a>
 */

public class CustomException extends Exception{

	public CustomException() {
		super();
		// TODO Auto-generated constructor stub
	}

	public CustomException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

	public CustomException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

	public CustomException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}

	 
}
