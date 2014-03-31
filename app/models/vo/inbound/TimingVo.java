package models.vo.inbound;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
/**
 * 
* <p>Project: CloudWMS</p> 
* <p>Title: TimingVo.java</p> 
* <p>Description: </p> 
* <p>Copyright (c) 2013 Wilmar Consultancy Services</p>
* <p>All Rights Reserved.</p>
* @author <a href="mailto:chenaihong@wcs-global.com">Chen Aihong</a>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class TimingVo {
	
	public String id;
	public String timingId;
	public boolean haslive = false;
	public double timingDay;
	public StorageTypeVo storageTypeVo;
	public boolean hasExecution = false;
}
