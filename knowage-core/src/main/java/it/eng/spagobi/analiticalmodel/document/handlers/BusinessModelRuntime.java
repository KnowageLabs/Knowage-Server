package it.eng.spagobi.analiticalmodel.document.handlers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.AbstractDriver;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.BIMetaModelParameter;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.MetaModelParuse;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.ParameterUse;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.dao.IMetaModelParuseDAO;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

public class BusinessModelRuntime extends AbstractBIResourceRuntime<BIMetaModelParameter> {
	private final Map<List<Object>, List<MetaModelParuse>> dependencies = new HashMap<>();

	public BusinessModelRuntime(IEngUserProfile userProfile, Locale locale) {
		super(userProfile, locale);
	}

	@Override
	public List<BusinessModelDriverRuntime> getDrivers() {
		return null;
	}

	@Override
	public List<MetaModelParuse> getDependencies(AbstractDriver driver, String role) {

		try {
			ParameterUse biParameterExecModality = getParameterUse(driver, role);
			List<Object> key = Arrays.asList(driver.getId(), biParameterExecModality.getUseID());
			List<MetaModelParuse> biParameterExecDependencies = dependencies.get(key);
			if (biParameterExecDependencies == null) {
				IMetaModelParuseDAO metaModelParuseDAO = DAOFactory.getMetaModelParuseDao();
				biParameterExecDependencies = new ArrayList<>(
						metaModelParuseDAO.loadMetaModelParuse(driver.getId(), biParameterExecModality.getUseID()));
				dependencies.put(key, biParameterExecDependencies);
			}
			return biParameterExecDependencies;
		} catch (Exception e) {
			throw new SpagoBIRuntimeException("Impossible to get dependencies", e);
		}
	}

	@Override
	protected void clearMetadataCaches() {
		super.clearMetadataCaches();
		dependencies.clear();
	}

}
