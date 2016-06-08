package it.eng.spagobi.engines.datamining.compute;

public class CreateDatasetResult {
	private int pythonExecutionError = 0; // -1 =error
	private String datasetlabel = null;

	public CreateDatasetResult(int pythonExecutionError, String datasetlabel) {
		this.pythonExecutionError = pythonExecutionError;
		this.datasetlabel = datasetlabel;
	}

	public CreateDatasetResult() {

	}

	public int getPythonExecutionError() {
		return pythonExecutionError;
	}

	public void setPythonExecutionError(int pythonExecutionError) {
		this.pythonExecutionError = pythonExecutionError;
	}

	public String getDatasetlabel() {
		return datasetlabel;
	}

	public void setDatasetlabel(String datasetlabel) {
		this.datasetlabel = datasetlabel;
	}
}
