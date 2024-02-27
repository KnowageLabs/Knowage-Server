package it.eng.spagobi.analiticalmodel.document.handlers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.hibernate.HibernateException;

import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.AbstractParuse;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.AbstractParview;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.BIMetaModelParameter;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.dao.IBIMetaModelParameterDAO;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.dao.IMetaModelParuseDAO;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.dao.IMetaModelParviewDAO;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.dao.IParameterDAO;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.dao.IParameterUseDAO;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.tools.catalogue.bo.MetaModel;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;

public class BusinessModelDriverRuntime extends AbstractDriverRuntime<BIMetaModelParameter> {

	// DAOs
	private IParameterUseDAO ANALYTICAL_DRIVER_USE_MODALITY_DAO;
	private IMetaModelParuseDAO DATA_DEPENDENCIES_BM_DAO;
	private IMetaModelParviewDAO VISUAL_DEPENDENCIES_BM_DAO;
	private IBIMetaModelParameterDAO DRIVER_BM_DAO;
	private IParameterDAO ANALYTICAL_DRIVER_DAO;

	public BusinessModelDriverRuntime(BIMetaModelParameter biParam, String exeRole, Locale loc, MetaModel doc,
			BusinessModelRuntime dum, List<BIMetaModelParameter> objParameters) {
		super(biParam, exeRole, loc, doc, dum, objParameters);
	}

	public BusinessModelDriverRuntime() {
	}

	@Override
	public void initDAO() {
		super.initDAO();
		DATA_DEPENDENCIES_BM_DAO = DAOFactory.getMetaModelParuseDao();
		VISUAL_DEPENDENCIES_BM_DAO = DAOFactory.getMetaModelParviewDao();

		try {
			DRIVER_BM_DAO = DAOFactory.getBIMetaModelParameterDAO();
		} catch (HibernateException e) {
			throw new SpagoBIServiceException(
					"An error occurred while retrieving DAO [" + DRIVER_BM_DAO.getClass().getName() + "]", e);
		}
	}

	@Override
	public void initVisualDependencies(BIMetaModelParameter driver) {
		if (dependencies == null) {
			dependencies = new HashMap<>();
		}
		try {
			visualDependencies = VISUAL_DEPENDENCIES_BM_DAO.loadMetaModelParviews(driver.getId());
		} catch (HibernateException e) {
			throw new SpagoBIServiceException(
					"An error occurred while loading parameter visual dependecies for parameter [" + id + "]", e);
		}
		Iterator<? extends AbstractParview> it = visualDependencies.iterator();
		while (it.hasNext()) {
			AbstractParview dependency = it.next();
			Integer metaModelParFatherId = dependency.getParFatherId();
			try {
				BIMetaModelParameter metaModelParFather = DRIVER_BM_DAO
						.loadBIMetaModelParameterById(metaModelParFatherId);
				VisualDependencyRuntime visualDependency = new VisualDependencyRuntime();
				visualDependency.urlName = metaModelParFather.getParameterUrlName();
				visualDependency.condition = dependency;
				if (!dependencies.containsKey(visualDependency.urlName)) {
					dependencies.put(visualDependency.urlName, new ArrayList<>());
				}
				List<DriverDependencyRuntime> depList = dependencies.get(visualDependency.urlName);
				depList.add(visualDependency);
			} catch (Exception e) {
				throw new SpagoBIServiceException(
						"An error occurred while loading parameter [" + metaModelParFatherId + "]", e);
			}
		}
	}

	@Override
	public void initDataDependencies(BIMetaModelParameter driver) {
		if (dependencies == null) {
			dependencies = new HashMap<>();
		}
		try {
			dataDependencies = DATA_DEPENDENCIES_BM_DAO.loadMetaModelParuseById(driver.getId());
		} catch (Exception e) {
			throw new SpagoBIServiceException(
					"An error occurred while loading parameter data dependecies for parameter [" + id + "]", e);
		}
		Iterator<? extends AbstractParuse> it = dataDependencies.iterator();
		while (it.hasNext()) {
			AbstractParuse dependency = it.next();
			Integer objParFatherId = dependency.getParFatherId();
			try {
				BIMetaModelParameter objParFather = DRIVER_BM_DAO.loadBIMetaModelParameterById(objParFatherId);
				DataDependencyRuntime dataDependency = new DataDependencyRuntime();
				dataDependency.urlName = objParFather.getParameterUrlName();
				if (!dependencies.containsKey(dataDependency.urlName)) {
					dependencies.put(dataDependency.urlName, new ArrayList<>());
				}
				List<DriverDependencyRuntime> depList = dependencies.get(dataDependency.urlName);
				depList.add(dataDependency);
			} catch (Exception e) {
				throw new SpagoBIServiceException("An error occurred while loading parameter [" + objParFatherId + "]",
						e);
			}
		}
	}

	public void loadAdmissibleValues(BIMetaModelParameter driver, BusinessModelRuntime dum) {
		super.loadAdmissibleValues(driver, dum);
	}

	@Override
	public BIMetaModelParameter getDriver() {
		return driver;
	}

}
