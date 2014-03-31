package models.vo.outbound;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

import utils.DateUtil;

@JsonIgnoreProperties(ignoreUnknown = true)
public class cargoSearchVo {
	public String piNo;
	public Date fromDate;
	public Date toDate;
	public Date fromCloseDate;
	public Date toCloseDate;
	public Date fromCreateDate;
	public Date toCreateDate;
	public Date fromUpdateDate;
	public Date toUpdateDate;
	@JsonProperty("fromCreateDate")
	public void setFromCreateDate(String fromCreateDate) {
		this.fromCreateDate=getDate(fromCreateDate);
	}
	@JsonProperty("fromUpdateDate")
	public void setFromUpdateDate(String fromUpateDate) {
		this.fromUpdateDate=getDate(fromUpateDate);
	}
	@JsonProperty("toCreateDate")
	public void setToCreateDate(String toCreateDate) {
		this.toCreateDate=getDate(toCreateDate);
	}
	@JsonProperty("toUpdateDate")
	public void setToUpdateDate(String toUpateDate) {
		this.toUpdateDate=getDate(toUpateDate);
	}
	public Date getDate(String Time){
		if(StringUtils.isNotBlank(Time)){
			Time = Time.replaceAll("T"," ");
			return utils.DateUtil.strToDate(Time,"yyyy-MM-dd HH:mm");
		}else{
			return null;
		}
	}
}
