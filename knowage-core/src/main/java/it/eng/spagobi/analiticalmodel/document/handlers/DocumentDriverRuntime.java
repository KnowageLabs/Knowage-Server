package it.eng.spagobi.analiticalmodel.document.handlers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.HibernateException;

import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.AbstractParuse;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.AbstractParview;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.BIObjectParameter;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.dao.IBIObjectParameterDAO;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.dao.IObjParuseDAO;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.dao.IObjParviewDAO;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;

public class DocumentDriverRuntime extends AbstractDriverRuntime<BIObjectParameter> {

	private static final Logger LOGGER = LogManager.getLogger(DocumentDriverRuntime.class);

	// DAOs
	private IObjParuseDAO dataDependenciesDocDao;
	private IObjParviewDAO visualDependenciesDocDao;
	private IBIObjectParameterDAO driverDocDao;

	public DocumentDriverRuntime(BIObjectParameter biParam, String exeRole, Locale loc, BIObject doc,
			DocumentRuntime dum, List<BIObjectParameter> objParameters) {
		super(biParam, exeRole, loc, doc, dum, objParameters);
	}

	public DocumentDriverRuntime(BIObjectParameter biParam, String exeRole, Locale loc, BIObject doc,
			boolean isFromCross, boolean loadAdmissible, DocumentRuntime dum, List<BIObjectParameter> objParameters) {
		super(biParam, exeRole, loc, doc, isFromCross, loadAdmissible, dum, objParameters);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void initDAO() {
		super.initDAO();
		dataDependenciesDocDao = DAOFactory.getObjParuseDAO();
		visualDependenciesDocDao = DAOFactory.getObjParviewDAO();

		try {
			driverDocDao = DAOFactory.getBIObjectParameterDAO();
		} catch (HibernateException e) {
			throw new SpagoBIServiceException(
					"An error occurred while retrieving DAO [" + driverDocDao.getClass().getName() + "]", e);
		}
	}

	@Override
	public void initVisualDependencies(BIObjectParameter driver) {
		if (dependencies == null) {
			dependencies = new HashMap<>();
		}
		try {
			visualDependencies = visualDependenciesDocDao.loadObjParviews(driver.getId());
		} catch (HibernateException e) {
			throw new SpagoBIServiceException(
					"An error occurred while loading parameter visual dependecies for parameter [" + id + "]", e);
		}
		Iterator<AbstractParview> it = visualDependencies.iterator();
		while (it.hasNext()) {
			AbstractParview dependency = it.next();
			Integer objParFatherId = dependency.getParFatherId();
			try {
				BIObjectParameter objParFather = driverDocDao.loadForDetailByObjParId(objParFatherId);
				VisualDependencyRuntime visualDependency = new VisualDependencyRuntime();
				visualDependency.urlName = objParFather.getParameterUrlName();
				visualDependency.condition = dependency;
				if (!dependencies.containsKey(visualDependency.urlName)) {
					dependencies.put(visualDependency.urlName, new ArrayList<>());
				}
				List<DriverDependencyRuntime> depList = dependencies.get(visualDependency.urlName);
				depList.add(visualDependency);
			} catch (EMFUserError e) {
				throw new SpagoBIServiceException("An error occurred while loading parameter [" + objParFatherId + "]",
						e);
			}
		}
	}

	@Override
	public void initDataDependencies(BIObjectParameter driver) {
		if (dependencies == null) {
			dependencies = new HashMap<>();
		}
		try {
			dataDependencies = dataDependenciesDocDao.loadObjParuse(driver.getId(),
					analyticalDriverExecModality.getUseID());
		} catch (EMFUserError e) {
			throw new SpagoBIServiceException(
					"An error occurred while loading parameter data dependecies for parameter [" + id + "]", e);
		}
		Iterator<? extends AbstractParuse> it = dataDependencies.iterator();
		while (it.hasNext()) {
			AbstractParuse dependency = it.next();
			Integer objParFatherId = dependency.getParFatherId();
			try {
				BIObjectParameter objParFather = driverDocDao.loadForDetailByObjParId(objParFatherId);
				DataDependencyRuntime dataDependency = new DataDependencyRuntime();
				dataDependency.urlName = objParFather.getParameterUrlName();
				if (!dependencies.containsKey(dataDependency.urlName)) {
					dependencies.put(dataDependency.urlName, new ArrayList<>());
				}
				List<DriverDependencyRuntime> depList = dependencies.get(dataDependency.urlName);
				depList.add(dataDependency);
			} catch (EMFUserError e) {
				throw new SpagoBIServiceException("An error occurred while loading parameter [" + objParFatherId + "]",
						e);
			}
		}
	}

	public void loadAdmissibleValues(BIObjectParameter driver, DocumentRuntime dum) {
		super.loadAdmissibleValues(driver, dum);
	}

	@Override
	public BIObjectParameter getDriver() {
		return driver;
	}

}
