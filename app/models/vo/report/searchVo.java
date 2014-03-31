package models.vo.report;

import java.util.Date;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class searchVo {
	public String 	piNo;
	public String 	batchNo;
	public String 	palletNo;
	public Date 	fromDate;
	public Date 	toDate;
}
