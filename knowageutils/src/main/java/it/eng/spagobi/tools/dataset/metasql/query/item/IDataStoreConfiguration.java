/**
 *
 */
package it.eng.spagobi.tools.dataset.metasql.query.item;

import java.util.List;
import java.util.Map;

import it.eng.spagobi.tools.dataset.bo.IDataSet;

/**
 * @author Dragan Pirkovic
 *
 */
public interface IDataStoreConfiguration {

	IDataSet getDataset();

	boolean isRealtime();

	Map<String, String> getParameters();

	List<AbstractSelectionField> getProjections();

	Filter getFilter();

	List<AbstractSelectionField> getGroups();

	List<Sorting> getSortings();

	List<List<AbstractSelectionField>> getSummaryRowProjections();

	Integer getOffset();

	Integer getFetchSize();

	Integer getMaxRowCount();

}
