package models.vo.outbound;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TempContainerVo {
	public TempTruckVo TruckVo;
	public String container;
	public String SealNo;
	public List<TempStuffingVo> batchs=new ArrayList<TempStuffingVo>();
	public void initContainer(String Container,String SealNo){
		this.container=Container;
		this.SealNo=SealNo;
	}
}
