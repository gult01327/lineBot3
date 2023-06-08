package test.com.linebot;

public class TestParamsDto {
	
	public TestParamsDto() {}
	
	public TestParamsDto(int num1,int num2) {
		this.num1 = num1;
		this.num2 = num2;
	}
	
	private String str1;
	private int num1;
	private int num2;
	
	public String getStr1() {
		return str1;
	}
	public void setStr1(String str1) {
		this.str1 = str1;
	}
	public int getNum1() {
		return num1;
	}
	public void setNum1(int num1) {
		this.num1 = num1;
	}
	public int getNum2() {
		return num2;
	}
	public void setNum2(int num2) {
		this.num2 = num2;
	}
	
	public int plus() {
		return this.num1 + this.num2;
	}
	
	public String printParam() {
		return "str1:"+str1 +"; num1:"+num1 +"; num2:"+num2 ;
	}
}
