package it.eng.spagobi.functions.dao;

import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.commons.dao.ISpagoBIDao;
import it.eng.spagobi.functions.metadata.SbiCatalogFunction;
import it.eng.spagobi.utilities.CatalogFunction;
import it.eng.spagobi.utilities.CatalogFunctionInputFile;

import java.util.List;
import java.util.Map;

public interface ICatalogFunctionDAO extends ISpagoBIDao {

	public int insertCatalogFunction(CatalogFunction catalogFunction, List<String> inputDatasets, Map<String, String> inputVariables,
			Map<String, String> outputs, List<CatalogFunctionInputFile> inputFiles) throws EMFUserError;

	public List<SbiCatalogFunction> loadAllCatalogFunctions();

	public int updateCatalogFunction(CatalogFunction updatedCatalogFunction, int id);

	public void deleteCatalogFunction(int id);

	public SbiCatalogFunction getCatalogFunctionById(int id);

	public SbiCatalogFunction getCatalogFunctionByLabel(String label);

}