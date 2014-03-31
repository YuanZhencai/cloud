/** * SortUtil.java 
* Created on 2013-7-23 下午5:17:04 
*/

package models.vo.transfer;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import utils.EmptyUtil;
import utils.exception.CustomException;

/** 
 * <p>Project: CloudWMS</p> 
 * <p>Title: SortUtil.java</p> 
 * <p>Description: </p> 
 * <p>Copyright (c) 2013 Wilmar Consultancy Services</p>
 * <p>All Rights Reserved.</p>
 * @author <a href="mailto:liyong@wcs-global.com">liyong</a>
 */

public class SortUtil {
	private static final Pattern pd = Pattern.compile("\\d+");
	private static final Pattern pw = Pattern.compile("[a-zA-Z]+");
	private static final Logger logger = LoggerFactory.getLogger(SortUtil.class);

	public static <T extends PalletNoVo> List<T> sortList(List<T> ts) throws CustomException {
	    Long start = System.currentTimeMillis();
		if (!EmptyUtil.isNotEmptyList(ts)) {
			throw new CustomException("List is null");
		}
		List<T> afterSortList = new ArrayList<T>();
		while(ts.size()>0){
			int minIndex = 0;
			int minPalletNo = changeStrToInt(ts.get(0).palletNo);
			for (int i = 0; i < ts.size(); i++) {
				int palletNo = changeStrToInt(ts.get(i).palletNo);
				if (palletNo < minPalletNo) {
					minPalletNo = palletNo;
					minIndex = i;
				}
			}
			afterSortList.add(ts.get(minIndex));
			ts.remove(minIndex);
		}
	logger.info("^^^^^^^Calling sortList cost time :"+(System.currentTimeMillis()-start)+"  Millis^^^^^^^^^^^");
	return	afterSortList;
	}

	/**
	 * 把字符串转换为数字
	 * @param str
	 * @return
	 */
	private static int changeStrToInt(String str) {
		int number = 0;
		if(str==null||"".equals(str)){
			number = (int) (30000+Math.random()*100);
			return number;
		}
		try {
			String[] ints = pw.split(str,2);
			String[] chars = pd.split(str,2);
			if (chars != null && chars.length > 0) {
				for (int i = 0; i < chars[0].length(); i++) {
					number += number + chars[0].charAt(i);
				}
				number=number*number;
			}
			if (ints != null) {
				if (ints.length == 1) {
					number = number + Integer.parseInt(ints[0]);
				} else if (ints.length == 2) {
					number = number + Integer.parseInt(ints[1]);
				}
			}
		} catch (Exception e) {
			 number=  10000+str.charAt(0);
		}
		return number;
	}

	 
}
