package it.eng.spagobi.kpi.bo;

import java.util.Date;

public class KpiValueExecLog implements Cloneable {

	private int id;
	private int schedulerId;
	private Date timeRun;
	private String output;
	private int errorCount;
	private int successCount;
	private int totalCount;
	private boolean outputPresent;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getSchedulerId() {
		return schedulerId;
	}

	public void setSchedulerId(int schedulerId) {
		this.schedulerId = schedulerId;
	}

	public Date getTimeRun() {
		return timeRun;
	}

	public void setTimeRun(Date timeRun) {
		this.timeRun = timeRun;
	}

	public String getOutput() {
		return output;
	}

	public void setOutput(String output) {
		this.output = output;
	}

	public int getErrorCount() {
		return errorCount;
	}

	public void setErrorCount(int errorCount) {
		this.errorCount = errorCount;
	}

	public int getSuccessCount() {
		return successCount;
	}

	public void setSuccessCount(int successCount) {
		this.successCount = successCount;
	}

	public int getTotalCount() {
		return totalCount;
	}

	public void setTotalCount(int totalCount) {
		this.totalCount = totalCount;
	}

	public boolean isOutputPresent() {
		return outputPresent;
	}

	public void setOutputPresent(boolean outputPresent) {
		this.outputPresent = outputPresent;
	}
}
