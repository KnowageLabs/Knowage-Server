package it.eng.knowage.slimerjs.wrapper.beans;

public class SlimerJSExecutionResponse {

	private int exitCode;
	private String stdOut;
	private String stdErr;
	
	public SlimerJSExecutionResponse(int exitCode, String stdOut, String stdErr) {
		this.exitCode = exitCode;
		this.stdOut = stdOut;
		this.stdErr = stdErr;
	}
	
	public int getExitCode() {
		return exitCode;
	}
	public void setExitCode(int exitCode) {
		this.exitCode = exitCode;
	}
	public String getStdOut() {
		return stdOut;
	}
	public void setStdOut(String stdOut) {
		this.stdOut = stdOut;
	}
	public String getStdErr() {
		return stdErr;
	}
	public void setStdErr(String stdErr) {
		this.stdErr = stdErr;
	}
}
