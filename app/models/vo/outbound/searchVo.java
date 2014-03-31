package models.vo.outbound;

import java.util.Date;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class searchVo {
	public String 	piNo;
	public String 	sgPiNo;
	public String 	piStatus;
	public Date 	fromDate;
	public Date 	toDate;
	public Date 	transferDateTime;
}
