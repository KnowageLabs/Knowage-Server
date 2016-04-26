package it.eng.spagobi.tools.alert.listener;

public interface IAlertListener {

	public void execute(String jsonParameters);

	public void saveResult(Integer idListener);
}
