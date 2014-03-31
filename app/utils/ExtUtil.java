//ExtUtil.java
package utils;

import java.util.*;

import org.apache.commons.lang3.StringUtils;

public class ExtUtil {
    public static String apply(HashMap<String, String> map) {
        StringBuilder sb = new StringBuilder();
        try {
            for(String k : map.keySet()) {
                sb.append("\"" + k + "\"=>\"" + encodeSpecialChar(map.get(k)) + "\",");
            }
            if (sb.toString().endsWith(",")) sb.deleteCharAt(sb.length() - 1);
        } catch(Exception ex) {
            // IGNORED
        }
        return sb.toString();
    }
    public static HashMap<String, String> unapply(String str) {
        HashMap<String, String> res = new HashMap<String, String>();
        try {
            String[] arr = str.split(",");
            for(String seg : arr) {
                if (!"".equals(seg.trim())) {
                    String[] kv = (seg + " ").split("=>");
                    String k = kv[0].trim();
                    if (k.startsWith("\"") && (k.endsWith("\"")) && k.length() > 2) k = k.substring(1, k.length() - 1);
                    String v = kv[1].trim();
                    if (v.startsWith("\"") && (v.endsWith("\"")) && v.length() > 1) v = v.substring(1, v.length() - 1);
                    res.put(k, decodeSpecialChar(v));
                }
            }
        } catch(Exception ex) {
            // IGNORED
        }
        return res;
    }
    
    public static String updateExt(String ext,Map<String,String> map){
        HashMap<String,String> unapply = ExtUtil.unapply(ext);
        Set<String> keySet = map.keySet();
        for (String string : keySet) {
            unapply.put(string, map.get(string));
        }
        return ExtUtil.apply(unapply);
    }
    
    /**
     * 编码
     * ## -> ,
     * @@ -> "
     * $$ -> '
     * */
    public static String encodeSpecialChar(String str){
    	if(StringUtils.isNotEmpty(str)){
    		return str.replaceAll(",","##").replaceAll("\"","@@").replaceAll("\'", "%%");
    	}else{
    		return str;
    	}
    }
    
    /**
     * 解码
     * ## -> ,
     * @@ -> "
     * $$ -> '
     * */
    public static String decodeSpecialChar(String str){
    	if(StringUtils.isNotEmpty(str)){
    		return str.replaceAll("##",",").replaceAll("@@","\"").replaceAll("%%", "\'");
    	}else{
    		return str;
    	}
    }
}