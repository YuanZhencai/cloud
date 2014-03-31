package models.vo.inbound;

import java.sql.Timestamp;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PlanItemSearchVo {
	public String piNo;
	public String sgPiNo;
	public String piStatus;
	public String productionLine;
	public Timestamp fromDate;
	public Timestamp toDate;
	public String referenceNo;
}
