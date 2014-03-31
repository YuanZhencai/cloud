package models.vo.auxilliary;

import java.util.HashMap;

import utils.DateUtil;

public class Request {
	public String id;
	public String piNo;
	public String area;
	public String bin;
	 public HashMap<String,String> getHashMap(){
	        HashMap<String, String> hashMap = new HashMap<String,String>();
	        hashMap.put("pi", piNo==null?"":piNo);
	        hashMap.put("lot", "");
	        hashMap.put("datefrom", "");
	        hashMap.put("dateto", "");
	        hashMap.put("line", "");
	        return hashMap;
	    }
}
