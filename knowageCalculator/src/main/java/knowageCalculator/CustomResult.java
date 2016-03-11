package knowageCalculator;

public class CustomResult {

	double goldPrice;
	double silverPrice;

	public CustomResult(double goldPrice, double silverPrice) {
		this.goldPrice=goldPrice;
		this.silverPrice=silverPrice;
	}
	public double getGoldPrice() {
		return goldPrice;
	}
	public void setGoldPrice(double goldPrice) {
		this.goldPrice = goldPrice;
	}
	public double getSilverPrice() {
		return silverPrice;
	}
	public void setSilverPrice(double silverPrice) {
		this.silverPrice = silverPrice;
	}

}
