package it.eng.spagobi.behaviouralmodel.analyticaldriver.dao;

import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Session;

import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.BIMetaModelParameter;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.metadata.SbiMetaModelParameter;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.metadata.SbiParameters;
import it.eng.spagobi.commons.dao.ISpagoBIDao;

public interface IBIMetaModelParameterDAO extends ISpagoBIDao {

	public SbiMetaModelParameter loadById(Integer id) throws HibernateException;

	public BIMetaModelParameter loadBIMetaModelParameterById(Integer id);

	public void modifyBIMetaModelParameter(BIMetaModelParameter aBIMetaModelParameter);

	public Integer insertBIMetaModelParameter(BIMetaModelParameter aBIMetaModelParameter);

	public void eraseBIMetaModelParameter(BIMetaModelParameter aBIMetaModelParameter);

	public List loadBIMetaModelParameterByMetaModelId(Integer metaModelID);

	public void eraseBIMetaModelParametersByMetaModelId(Integer MetaModelId);

	public SbiParameters getParameterByModelAndDriverName(String modelName, String name);

	public void eraseBIMetaModelParameterDependencies(BIMetaModelParameter aBIMetaModelParameter, Session aSession);

}
