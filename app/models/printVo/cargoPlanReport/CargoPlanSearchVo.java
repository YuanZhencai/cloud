package models.printVo.cargoPlanReport;

import java.util.Date;
import java.util.HashMap;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import utils.DateUtil;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CargoPlanSearchVo {
	public String piNo;
	public Date stuffingDateFrom;
	public Date stuffingDateTo;
	public Date closingDateFrom;
	public Date closingDateTo;

	public HashMap<String, String> getHashMap() {
		HashMap<String, String> hashMap = new HashMap<String, String>();
		hashMap.put("piNo", piNo == null ? "" : piNo);
		hashMap.put("stuffingDateFrom", stuffingDateFrom == null ? "" : DateUtil.dateToStrShort(stuffingDateFrom));
		hashMap.put("stuffingDateTo", stuffingDateTo == null ? "" : DateUtil.dateToStrShort(stuffingDateTo));
		hashMap.put("closingDateFrom", closingDateFrom == null ? "" : DateUtil.dateToStrShort(closingDateFrom));
		hashMap.put("closingDateTo", closingDateTo == null ? "" : DateUtil.dateToStrShort(closingDateTo));
		return hashMap;
	}
}
