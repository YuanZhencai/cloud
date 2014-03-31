package models.vo.auxilliary;

import java.util.List;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ExectionVo {
	public String issueType;
	public double Qty;
	public String remarks;
	public List<StockVo> stocks;
	public int  type;
}
