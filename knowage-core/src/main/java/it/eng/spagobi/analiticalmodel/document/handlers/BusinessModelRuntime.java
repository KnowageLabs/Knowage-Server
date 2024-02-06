package it.eng.spagobi.analiticalmodel.document.handlers;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.AbstractDriver;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.BIMetaModelParameter;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.MetaModelParuse;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.ParameterUse;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.dao.IMetaModelParuseDAO;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.dao.IParameterUseDAO;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

public class BusinessModelRuntime extends AbstractBIResourceRuntime<BIMetaModelParameter> {

	public BusinessModelRuntime(IEngUserProfile userProfile, Locale locale) {
		super(userProfile, locale);
	}

	@Override
	public List<BusinessModelDriverRuntime> getDrivers() {
		return null;
	}

	@Override
	public List<MetaModelParuse> getDependencies(AbstractDriver driver, String role) {

		List<MetaModelParuse> biParameterExecDependencies = new ArrayList<>();
		try {
			IParameterUseDAO parusedao = DAOFactory.getParameterUseDAO();
			ParameterUse biParameterExecModality = parusedao.loadByParameterIdandRole(driver.getParID(), role);
			IMetaModelParuseDAO metaModelParuseDAO = DAOFactory.getMetaModelParuseDao();
			biParameterExecDependencies
					.addAll(metaModelParuseDAO.loadMetaModelParuse(driver.getId(), biParameterExecModality.getUseID()));
		} catch (Exception e) {
			throw new SpagoBIRuntimeException("Impossible to get dependencies", e);
		}
		return biParameterExecDependencies;
	}

}
