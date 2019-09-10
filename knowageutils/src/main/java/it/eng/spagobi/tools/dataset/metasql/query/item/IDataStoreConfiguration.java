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

	List<Projection> getProjections();

	Filter getFilter();

	List<Projection> getGroups();

	List<Sorting> getSortings();

	List<Projection> getSummaryRowProjections();

	Integer getOffset();

	Integer getFetchSize();

	Integer getMaxRowCount();

}
