/** * RequestUtil.java 
* Created on 2013-7-17 下午2:45:20 
*/

package utils;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;

import models.Area;
import models.Bin;
import models.Execution;
import models.Stock;
import models.vo.arrangement.StockVo;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.avaje.ebean.Ebean;

import play.mvc.Http.RequestBody;
import utils.enums.CodeKey;
import utils.exception.CustomException;

/** 
 * <p>Project: CloudWMS</p> 
 * <p>Title: RequestUtil.java</p> 
 * <p>Description: </p> 
 * <p>Copyright (c) 2013 Wilmar Consultancy Services</p>
 * <p>All Rights Reserved.</p>
 * @author <a href="mailto:liyong@wcs-global.com">liyong</a>
 */

public class RequestUtil<T> {

	private static final Logger logger = LoggerFactory.getLogger(RequestUtil.class);

	public  List<T> viaRequestToMultObject(RequestBody body) throws CustomException, JsonParseException, JsonMappingException, IOException {
		logger.info("^^^^^^^^^^^^^you have in viaRequestToMultObject  method ^^^^^^^^^^^^^");
		if (body == null) {
			throw new CustomException("RequestBody is null");
		}
		ObjectMapper mapper = new ObjectMapper();
		if (body.asJson() == null) {
			throw new CustomException("RequestBody asJson is null");
		}
		List<T> tList = mapper.readValue(body.asJson(), new TypeReference<List<T>>() {
		});
		return tList;

	}
}
