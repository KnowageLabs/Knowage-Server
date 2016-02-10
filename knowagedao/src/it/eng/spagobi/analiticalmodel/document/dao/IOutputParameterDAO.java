package it.eng.spagobi.analiticalmodel.document.dao;

import it.eng.spagobi.analiticalmodel.document.bo.OutputParameter;
import it.eng.spagobi.commons.dao.ISpagoBIDao;

import java.util.List;

public interface IOutputParameterDAO extends ISpagoBIDao {

	/**
	 * List SbiOutputParameter by SbiObject id
	 * 
	 * @param id
	 *            of SbiObject
	 * @return list of OutputParameter
	 */
	public List<OutputParameter> getOutputParametersByObjId(Integer id);

	public void removeParameter(Integer id);

	public void saveParameter(OutputParameter outputParameter);

	public OutputParameter getOutputParameter(Integer id);

	/**
	 * 
	 * @param list
	 *            of OutputParameter
	 */
	// public void saveParameterList(List<OutputParameter> list, Integer biobjId) throws EMFUserError;

}
