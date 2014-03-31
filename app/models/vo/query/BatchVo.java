package models.vo.query;

import java.util.Date;
import java.util.HashMap;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import utils.DateUtil;
@JsonIgnoreProperties(ignoreUnknown = true)
public class BatchVo {
    public String piNo;
    public String lotNo;
    public String prodLine;
    public Date prodDateFrom;
    public Date prodDateTo;
    public HashMap<String,String> getHashMap(){
        HashMap<String, String> hashMap = new HashMap<String,String>();
        hashMap.put("pi", piNo==null?"":piNo);
        hashMap.put("lot", lotNo==null?"":lotNo);
        hashMap.put("datefrom", prodDateFrom==null?"":DateUtil.dateToStrShort(prodDateFrom));
        hashMap.put("dateto", prodDateTo==null?"":DateUtil.dateToStrShort(prodDateTo));
        hashMap.put("line", prodLine==null?"":prodLine);
        return hashMap;
    }
}
