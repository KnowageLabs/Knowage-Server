package it.eng.knowage.engine.cockpit.api.export.excel.exporters;

public interface IWidgetExporter {

	/**
	 * @author mbalestri
	 *
	 *         Export the widget to excel
	 *
	 * @return the number of sheets created (one for each dataset)
	 */
	public int export();

}
