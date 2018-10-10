package it.eng.spagobi.engines.datamining.compute;

public class CreateDatasetResult {
	private int pythonExecutionError = 0; // -1 =error
	private String datasetlabel = null;
	private String rExecutionError = null;
	private String ptyhonExecutionCodeWithError = null;

	public CreateDatasetResult(int pythonExecutionError, String datasetlabel) {
		this.pythonExecutionError = pythonExecutionError;
		this.datasetlabel = datasetlabel;
	}

	public CreateDatasetResult() {

	}

	public int getPythonExecutionError() {
		return pythonExecutionError;
	}

	public void setPythonExecutionError(int executionError) {
		this.pythonExecutionError = executionError;
	}

	public String getPythonExecutionCodeWithError() {
		return ptyhonExecutionCodeWithError;
	}

	public void setPythonExecutionCodeWithError(String ptyhonExecutionCodeWithError) {
		this.ptyhonExecutionCodeWithError = ptyhonExecutionCodeWithError;
	}

	public String getDatasetlabel() {
		return datasetlabel;
	}

	public void setDatasetlabel(String datasetlabel) {
		this.datasetlabel = datasetlabel;
	}

	public String getRExecutionError() {
		return rExecutionError;
	}

	public void setRExecutionError(String rExecutionError) {
		this.rExecutionError = rExecutionError;
	}

}
