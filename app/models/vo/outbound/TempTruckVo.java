package models.vo.outbound;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TempTruckVo {
	public String orderId;
	public String Truck;
	public Date StuffingDate;
	public boolean show=true;
	public boolean haslive=true;
	public boolean Canedit=true;
	public List<TempContainerVo> containers=new ArrayList<TempContainerVo>();
	public void initTruck(String Truck,Date StuffingDate,boolean Canedit,String orderId){
		this.Truck=Truck;
		this.StuffingDate=StuffingDate;
		this.Canedit=Canedit;
		this.orderId=orderId;
	}
}
