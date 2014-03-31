package models.printVo;

import utils.Excel.ExcelObj;
@action.Cell(name = "line")
public class Line extends ExcelObj{
	public String value;
	public int x;
	public int y;
	public boolean isValue=false;
	public Line(){
		
	}
	public Line(String value,int x,int y){
		this.value=value;
		this.x=x;
		this.y=y;
	}
	public Line(String value,int x,int y,boolean isValue){
		this.value=value;
		this.x=x;
		this.y=y;
		this.isValue=isValue;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	
	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}
	public void setY(int y) {
		this.y = y;
	}
	public boolean getIsValue() {
		return isValue;
	}
	public void setIsValue(boolean isValue) {
		this.isValue = isValue;
	}
	
}
