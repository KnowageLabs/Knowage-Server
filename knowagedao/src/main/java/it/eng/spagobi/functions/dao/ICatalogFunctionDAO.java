package it.eng.spagobi.functions.dao;

import java.util.List;
import java.util.Map;

import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.commons.dao.ISpagoBIDao;
import it.eng.spagobi.functions.metadata.IInputVariable;
import it.eng.spagobi.functions.metadata.IOutputColumn;
import it.eng.spagobi.functions.metadata.SbiCatalogFunction;
import it.eng.spagobi.utilities.CatalogFunction;

public interface ICatalogFunctionDAO extends ISpagoBIDao {

	public String insertCatalogFunction(CatalogFunction catalogFunction, Map<String, String> inputColumns, Map<String, ? extends IInputVariable> inputVariables,
			Map<String, ? extends IOutputColumn> outputColumns) throws EMFUserError;

	public List<SbiCatalogFunction> loadAllCatalogFunctions();

	public String updateCatalogFunction(CatalogFunction updatedCatalogFunction, String uuid);

	public void deleteCatalogFunction(String uuid);

	public SbiCatalogFunction getCatalogFunctionByUuid(String uuid);

	public String getCatalogFunctionScriptByUuidAndOrganization(String uuid, String organization);

	public SbiCatalogFunction getCatalogFunctionByLabel(String label);

	/**
	 * @param id
	 * @return List<SbiCatalogFunction>
	 */
	public List<SbiCatalogFunction> loadAllCatalogFunctionsByBiobjId(Integer id);

}