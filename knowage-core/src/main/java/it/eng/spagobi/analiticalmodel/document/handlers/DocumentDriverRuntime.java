package it.eng.spagobi.analiticalmodel.document.handlers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;

import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.BIObjectParameter;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.ObjParuse;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.ObjParview;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.dao.IBIObjectParameterDAO;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.dao.IObjParuseDAO;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.dao.IObjParviewDAO;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.dao.IParameterDAO;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.dao.IParameterUseDAO;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;

public class DocumentDriverRuntime extends AbstractDriverRuntime<BIObjectParameter> {

	private static Logger logger = Logger.getLogger(DocumentDriverRuntime.class);

	// DAOs
	private IParameterUseDAO ANALYTICAL_DRIVER_USE_MODALITY_DAO;
	private IObjParuseDAO DATA_DEPENDENCIES_DOC_DAO;
	private IObjParviewDAO VISUAL_DEPENDENCIES_DOC_DAO;
	private IBIObjectParameterDAO DRIVER_DOC_DAO;
	private IParameterDAO ANALYTICAL_DRIVER_DAO;

	public DocumentDriverRuntime(BIObjectParameter biParam, String exeRole, Locale loc, BIObject doc, DocumentRuntime dum,
			List<BIObjectParameter> objParameters) {
		super(biParam, exeRole, loc, doc, dum, objParameters);
		// TODO Auto-generated constructor stub
	}

	public DocumentDriverRuntime(BIObjectParameter biParam, String exeRole, Locale loc, BIObject doc, boolean isFromCross, boolean loadAdmissible,
			DocumentRuntime dum, List<BIObjectParameter> objParameters) {
		super(biParam, exeRole, loc, doc, isFromCross, loadAdmissible, dum, objParameters);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void initDAO() {
		super.initDAO();
		DATA_DEPENDENCIES_DOC_DAO = DAOFactory.getObjParuseDAO();
		VISUAL_DEPENDENCIES_DOC_DAO = DAOFactory.getObjParviewDAO();

		try {
			DRIVER_DOC_DAO = DAOFactory.getBIObjectParameterDAO();
		} catch (HibernateException e) {
			throw new SpagoBIServiceException("An error occurred while retrieving DAO [" + DRIVER_DOC_DAO.getClass().getName() + "]", e);
		}
	}

	void initAttributes(BIObjectParameter driver) {
		super.initAttributes(driver);
	}

	@Override
	public void initVisualDependencies(BIObjectParameter driver) {
		if (dependencies == null) {
			dependencies = new HashMap<String, List<DriverDependencyRuntime>>();
		}
		try {
			visualDependencies = VISUAL_DEPENDENCIES_DOC_DAO.loadObjParviews(driver.getId());
		} catch (HibernateException e) {
			throw new SpagoBIServiceException("An error occurred while loading parameter visual dependecies for parameter [" + id + "]", e);
		}
		Iterator it = visualDependencies.iterator();
		while (it.hasNext()) {
			ObjParview dependency = (ObjParview) it.next();
			Integer objParFatherId = dependency.getParFatherId();
			try {
				BIObjectParameter objParFather = DRIVER_DOC_DAO.loadForDetailByObjParId(objParFatherId);
				VisualDependencyRuntime visualDependency = new VisualDependencyRuntime();
				visualDependency.urlName = objParFather.getParameterUrlName();
				visualDependency.condition = dependency;
				if (!dependencies.containsKey(visualDependency.urlName)) {
					dependencies.put(visualDependency.urlName, new ArrayList<DriverDependencyRuntime>());
				}
				List<DriverDependencyRuntime> depList = dependencies.get(visualDependency.urlName);
				depList.add(visualDependency);
			} catch (EMFUserError e) {
				throw new SpagoBIServiceException("An error occurred while loading parameter [" + objParFatherId + "]", e);
			}
		}
	}

	@Override
	public void initDataDependencies(BIObjectParameter driver) {
		if (dependencies == null) {
			dependencies = new HashMap<String, List<DriverDependencyRuntime>>();
		}
		try {
			dataDependencies = DATA_DEPENDENCIES_DOC_DAO.loadObjParuse(driver.getId(), analyticalDriverExecModality.getUseID());
		} catch (EMFUserError e) {
			throw new SpagoBIServiceException("An error occurred while loading parameter data dependecies for parameter [" + id + "]", e);
		}
		Iterator it = dataDependencies.iterator();
		while (it.hasNext()) {
			ObjParuse dependency = (ObjParuse) it.next();
			Integer objParFatherId = dependency.getParFatherId();
			try {
				BIObjectParameter objParFather = DRIVER_DOC_DAO.loadForDetailByObjParId(objParFatherId);
				DataDependencyRuntime dataDependency = new DataDependencyRuntime();
				dataDependency.urlName = objParFather.getParameterUrlName();
				if (!dependencies.containsKey(dataDependency.urlName)) {
					dependencies.put(dataDependency.urlName, new ArrayList<DriverDependencyRuntime>());
				}
				List<DriverDependencyRuntime> depList = dependencies.get(dataDependency.urlName);
				depList.add(dataDependency);
			} catch (EMFUserError e) {
				throw new SpagoBIServiceException("An error occurred while loading parameter [" + objParFatherId + "]", e);
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
