package it.eng.spagobi.functions.dao;

import it.eng.spago.error.EMFUserError;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.commons.bo.Domain;
import it.eng.spagobi.commons.dao.AbstractHibernateDAO;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.dao.SpagoBIDAOException;
import it.eng.spagobi.functions.metadata.SbiCatalogFunction;
import it.eng.spagobi.functions.metadata.SbiFunctionInputDataset;
import it.eng.spagobi.functions.metadata.SbiFunctionInputDatasetId;
import it.eng.spagobi.functions.metadata.SbiFunctionInputFile;
import it.eng.spagobi.functions.metadata.SbiFunctionInputFileId;
import it.eng.spagobi.functions.metadata.SbiFunctionInputVariable;
import it.eng.spagobi.functions.metadata.SbiFunctionInputVariableId;
import it.eng.spagobi.functions.metadata.SbiFunctionOutput;
import it.eng.spagobi.functions.metadata.SbiFunctionOutputId;
import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.dao.IDataSetDAO;
import it.eng.spagobi.utilities.CatalogFunction;
import it.eng.spagobi.utilities.CatalogFunctionInputFile;
import it.eng.spagobi.utilities.assertion.Assert;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

public class CatalogFunctionDAOImpl extends AbstractHibernateDAO implements ICatalogFunctionDAO {

	static private Logger logger = Logger.getLogger(CatalogFunctionDAOImpl.class);

	@Override
	public List<SbiCatalogFunction> loadAllCatalogFunctions() {
		logger.debug("IN");
		Session session = null;
		List<SbiCatalogFunction> sbiCatalogFunctions = null;
		try {
			session = getSession();
			Assert.assertNotNull(session, "session cannot be null");
			Query hibQuery = session.createQuery("from SbiCatalogFunction");
			sbiCatalogFunctions = hibQuery.list();
		} catch (Throwable t) {
			throw new SpagoBIDAOException("An error occured while reading Catalog Functions from DB", t);
		} finally {
			if (session != null && session.isOpen()) {
				session.close();
			}
			logger.debug("OUT");
		}
		return sbiCatalogFunctions;
	}

	@Override
	public int insertCatalogFunction(CatalogFunction catalogFunction, List<String> inputDatasets, Map<String, String> inputVariables,
			Map<String, String> outputs, List<CatalogFunctionInputFile> inputFiles) {

		int catalogFunctionId = -1;
		Session session;
		Transaction transaction;

		logger.debug("IN");

		session = null;
		transaction = null;
		try {
			try {
				session = getSession();
				Assert.assertNotNull(session, "session cannot be null");
				transaction = session.beginTransaction();
				Assert.assertNotNull(transaction, "transaction cannot be null");
			} catch (Throwable t) {
				throw new SpagoBIDAOException("An error occured while creating the new transaction", t);
			}
			SbiCatalogFunction hibMap = toSbiFunctionCatalog(catalogFunction);
			updateSbiCommonInfo4Insert(hibMap);
			catalogFunctionId = (Integer) session.save(hibMap);

			SbiCatalogFunction hibCatFunction = (SbiCatalogFunction) session.load(SbiCatalogFunction.class, catalogFunctionId);

			hibCatFunction.setSbiFunctionInputVariables(getSbiFunctionInputVariablesSet(inputVariables, hibCatFunction));
			hibCatFunction.setSbiFunctionOutputs(getSbiFunctionOutputSet(outputs, hibCatFunction));
			hibCatFunction.setSbiFunctionInputDatasets(getSbiFunctionInputDatasetSet(inputDatasets, hibCatFunction));
			hibCatFunction.setSbiFunctionInputFiles(getSbiFunctionInputFileSet(inputFiles, hibCatFunction));
			session.save(hibCatFunction);

			transaction.commit();

		} catch (Throwable t) {
			if (transaction != null && transaction.isActive()) {
				transaction.rollback();
			}

			throw new SpagoBIDAOException("An unexpected error occured while inserting catalog function", t);
		} finally {
			if (session != null && session.isOpen()) {
				session.close();
			}
			logger.debug("OUT");
		}

		return catalogFunctionId;
	}

	private Set getSbiFunctionInputFileSet(List<CatalogFunctionInputFile> inputFiles, SbiCatalogFunction sbiCatalogFunction) {

		Set<SbiFunctionInputFile> inputFileSet = new HashSet<SbiFunctionInputFile>();
		SbiFunctionInputFile file = null;

		for (CatalogFunctionInputFile inputFile : inputFiles) {
			String fileName = inputFile.getFileName();
			String alias = inputFile.getAlias();
			byte[] content = inputFile.getContent();
			file = new SbiFunctionInputFile(new SbiFunctionInputFileId(sbiCatalogFunction.getFunctionId(), fileName), sbiCatalogFunction, content, alias);
			updateSbiCommonInfo4Insert(file);
			inputFileSet.add(file);
		}

		return inputFileSet;

	}

	private Set<SbiFunctionInputDataset> getSbiFunctionInputDatasetSet(List<String> inputDatasets, SbiCatalogFunction sbiCatalogFunction) {

		Set<SbiFunctionInputDataset> inputDatasetSet = new HashSet<SbiFunctionInputDataset>();
		SbiFunctionInputDataset dataset = null;
		IEngUserProfile profile = getUserProfile();

		for (String datasetLabel : inputDatasets) {
			// Create and Insert Dataset
			IDataSetDAO dataSetDAO = null;
			try {
				dataSetDAO = DAOFactory.getDataSetDAO();
			} catch (EMFUserError e) {
				throw new SpagoBIDAOException("An error occured while getting the dataset DAO", e);
			}
			dataSetDAO.setUserProfile(profile);

			logger.debug("check if dataset with label " + datasetLabel + " is already present");

			// check label is already present; insert or modify dependently
			IDataSet iDataSet = dataSetDAO.loadDataSetByLabel(datasetLabel);
			int dsId = iDataSet.getId();
			dataset = new SbiFunctionInputDataset(new SbiFunctionInputDatasetId(sbiCatalogFunction.getFunctionId(), dsId));
			updateSbiCommonInfo4Insert(dataset);
			inputDatasetSet.add(dataset);
		}

		return inputDatasetSet;

	}

	private Set<SbiFunctionInputVariable> getSbiFunctionInputVariablesSet(Map<String, String> inputVariables, SbiCatalogFunction sbiCatalogFunction) {

		Set<SbiFunctionInputVariable> inputVarSet = new HashSet<SbiFunctionInputVariable>();
		SbiFunctionInputVariable var = null;

		for (String varName : inputVariables.keySet()) {
			String value = inputVariables.get(varName);
			var = new SbiFunctionInputVariable(new SbiFunctionInputVariableId(sbiCatalogFunction.getFunctionId(), varName), sbiCatalogFunction, value);
			updateSbiCommonInfo4Insert(var);
			inputVarSet.add(var);
		}

		return inputVarSet;
	}

	private Set<SbiFunctionOutput> getSbiFunctionOutputSet(Map<String, String> outputs, SbiCatalogFunction sbiCatalogFunction) {

		Set<SbiFunctionOutput> outputVariablesSet = new HashSet<SbiFunctionOutput>();
		SbiFunctionOutput var = null;
		Domain domain = null;

		for (String varLabel : outputs.keySet()) {
			String outType = outputs.get(varLabel);
			try {
				domain = DAOFactory.getDomainDAO().loadDomainByCodeAndValue("FUNCTION_OUTPUT", outType);
				Integer outTypeSbiDomainId = domain.getValueId();
				var = new SbiFunctionOutput(new SbiFunctionOutputId(sbiCatalogFunction.getFunctionId(), varLabel), sbiCatalogFunction, outTypeSbiDomainId);
				updateSbiCommonInfo4Insert(var);
				outputVariablesSet.add(var);
			} catch (EMFUserError e) {
				throw new SpagoBIDAOException("An error occured while getting domain by code [FUNCTION_OUTPUT] and value [" + outType + "]", e);
			}

		}

		return outputVariablesSet;
	}

	private SbiCatalogFunction toSbiFunctionCatalog(CatalogFunction functionItem) {

		SbiCatalogFunction hibFunctionCatalogItem = new SbiCatalogFunction();
		hibFunctionCatalogItem.setFunctionId(functionItem.getFunctionId());
		hibFunctionCatalogItem.setLanguage(functionItem.getLanguage());
		hibFunctionCatalogItem.setName(functionItem.getName());
		hibFunctionCatalogItem.setDescription(functionItem.getDescription());
		hibFunctionCatalogItem.setScript(functionItem.getScript());
		hibFunctionCatalogItem.setOwner(functionItem.getOwner());
		hibFunctionCatalogItem.setLabel(functionItem.getLabel());
		hibFunctionCatalogItem.setType(functionItem.getType());
		hibFunctionCatalogItem.setUrl(functionItem.getUrl());
		hibFunctionCatalogItem.setRemote(functionItem.getRemote());

		List<String> keywords = functionItem.getKeywords();
		String keywordsString = StringUtils.join(keywords.iterator(), ",");
		hibFunctionCatalogItem.setKeywords(keywordsString);

		return hibFunctionCatalogItem;
	}

	@Override
	public int updateCatalogFunction(CatalogFunction updatedCatalogFunction, int id) {

		Session session;
		Transaction transaction;

		logger.debug("IN");

		session = null;
		transaction = null;
		try {
			try {
				session = getSession();
				Assert.assertNotNull(session, "session cannot be null");
				transaction = session.beginTransaction();
				Assert.assertNotNull(transaction, "transaction cannot be null");
			} catch (Throwable t) {
				throw new SpagoBIDAOException("An error occured while creating the new transaction", t);
			}

			SbiCatalogFunction hibCatFunction = (SbiCatalogFunction) session.get(SbiCatalogFunction.class, id);

			updateOutputs(hibCatFunction, session, updatedCatalogFunction);
			updateInputVariables(hibCatFunction, session, updatedCatalogFunction);
			updateInputFiles(hibCatFunction, session, updatedCatalogFunction);
			updateInputDatasets(hibCatFunction, session, updatedCatalogFunction);

			hibCatFunction.setLanguage(updatedCatalogFunction.getLanguage());
			hibCatFunction.setName(updatedCatalogFunction.getName());
			hibCatFunction.setDescription(updatedCatalogFunction.getDescription());
			hibCatFunction.setScript(updatedCatalogFunction.getScript());
			hibCatFunction.setOwner(updatedCatalogFunction.getOwner());
			hibCatFunction.setKeywords(StringUtils.join(updatedCatalogFunction.getKeywords().iterator(), ","));
			hibCatFunction.setLabel(updatedCatalogFunction.getLabel());
			hibCatFunction.setType(updatedCatalogFunction.getType());
			hibCatFunction.setUrl(updatedCatalogFunction.getUrl());
			hibCatFunction.setRemote(updatedCatalogFunction.getRemote());

			updateSbiCommonInfo4Update(hibCatFunction);
			session.update(hibCatFunction);
			transaction.commit();

		} catch (Throwable t) {
			if (transaction != null && transaction.isActive()) {
				transaction.rollback();
			}

			throw new SpagoBIDAOException("An unexpected error occured while updating catalog function", t);
		} finally {
			if (session != null && session.isOpen()) {
				session.close();
			}
			logger.debug("OUT");
		}

		return id;

	}

	private void updateInputDatasets(SbiCatalogFunction hibCatFunction, Session session, CatalogFunction updatedCatalogFunction) throws Throwable {

		// delete dataset not reinserted and update datasets that are already presents (and delete them from updatedatasetsList in updatedCatalogFunction)
		List<SbiFunctionInputDataset> dsToRemove = new ArrayList<SbiFunctionInputDataset>();
		for (Object o : hibCatFunction.getSbiFunctionInputDatasets()) {
			SbiFunctionInputDataset di = (SbiFunctionInputDataset) o;
			boolean delete = true;
			if (updatedFunctionContainsDatasets(updatedCatalogFunction.getInputDatasets(), di.getId().getDsId())) {
				delete = false;
				updateSbiCommonInfo4Update(di);
				IDataSet datasetHib = DAOFactory.getDataSetDAO().loadDataSetById(di.getId().getDsId());
				updatedCatalogFunction.getInputDatasets().remove(datasetHib.getLabel());
			}

			if (delete) {
				dsToRemove.add(di);
			}
		}

		for (SbiFunctionInputDataset remDs : dsToRemove) {
			SbiFunctionInputDataset dsToRem = (SbiFunctionInputDataset) session.get(SbiFunctionInputDataset.class, remDs.getId());
			hibCatFunction.getSbiFunctionInputDatasets().remove(dsToRem);
			session.delete(dsToRem);
			session.flush();
		}

		// insert datasets that are not presents
		Set<SbiFunctionInputDataset> hibDatasetSet = hibCatFunction.getSbiFunctionInputDatasets();
		for (String dsLabel : updatedCatalogFunction.getInputDatasets()) {
			IDataSet dataset = DAOFactory.getDataSetDAO().loadDataSetByLabel(dsLabel);
			SbiFunctionInputDataset inputDataset = new SbiFunctionInputDataset(new SbiFunctionInputDatasetId(hibCatFunction.getFunctionId(), dataset.getId()));
			updateSbiCommonInfo4Insert(inputDataset);
			hibDatasetSet.add(inputDataset);
		}

	}

	private void updateInputFiles(SbiCatalogFunction hibCatFunction, Session session, CatalogFunction updatedCatalogFunction) {

		// delete files not reinserted and update files that are already presents (and delete them from updateFilesList in updatedCatalogFunction)
		List<SbiFunctionInputFile> filesToRemove = new ArrayList<SbiFunctionInputFile>();
		for (Object o : hibCatFunction.getSbiFunctionInputFiles()) {
			SbiFunctionInputFile fi = (SbiFunctionInputFile) o;
			boolean delete = true;
			if (updatedFunctionContainsFile(updatedCatalogFunction.getInputFiles(), fi.getId().getFileName())) {
				delete = false;
				updateSbiCommonInfo4Update(fi);
				fi.setContent(getBytesFromFileName(fi.getId().getFileName(), updatedCatalogFunction.getInputFiles()));
				removeFileWithName(fi.getId().getFileName(), updatedCatalogFunction.getInputFiles());
			}
			if (delete) {
				filesToRemove.add(fi);
			}
		}

		for (SbiFunctionInputFile remFile : filesToRemove) {
			SbiFunctionInputFile fileToRem = (SbiFunctionInputFile) session.get(SbiFunctionInputFile.class, remFile.getId());
			hibCatFunction.getSbiFunctionInputFiles().remove(fileToRem);
			session.delete(fileToRem);
			session.flush();
		}

		// insert files that are not presents
		Set<SbiFunctionInputFile> hibFilesSet = hibCatFunction.getSbiFunctionInputFiles();
		for (CatalogFunctionInputFile file : updatedCatalogFunction.getInputFiles()) {
			byte[] fileContent = file.getContent();
			SbiFunctionInputFile inputFile = new SbiFunctionInputFile(new SbiFunctionInputFileId(hibCatFunction.getFunctionId(), file.getFileName()),
					hibCatFunction, fileContent, file.getAlias());
			updateSbiCommonInfo4Insert(inputFile);
			hibFilesSet.add(inputFile);
		}

	}

	private void updateInputVariables(SbiCatalogFunction hibCatFunction, Session session, CatalogFunction updatedCatalogFunction) {

		// delete vars not reinserted and update variables that are already presents (and delete them from updateVariablesList in updatedCatalogFunction)
		List<SbiFunctionInputVariable> varToRemove = new ArrayList<SbiFunctionInputVariable>();
		for (Object o : hibCatFunction.getSbiFunctionInputVariables()) {
			SbiFunctionInputVariable vi = (SbiFunctionInputVariable) o;
			boolean delete = true;
			if (updatedCatalogFunction.getInputVariables().keySet().contains(vi.getId().getVarName())) {
				delete = false;
				updateSbiCommonInfo4Update(vi);
				vi.setVarValue(updatedCatalogFunction.getInputVariables().get(vi.getId().getVarName()));
				updatedCatalogFunction.getInputVariables().remove(vi.getId().getVarName());
			}

			if (delete) {
				varToRemove.add(vi);
			}
		}
		for (SbiFunctionInputVariable remVar : varToRemove) {
			SbiFunctionInputVariable varToRem = (SbiFunctionInputVariable) session.get(SbiFunctionInputVariable.class, remVar.getId());
			hibCatFunction.getSbiFunctionInputVariables().remove(varToRem);
			session.delete(varToRem);
			session.flush();
		}

		// insert vars that are not presents
		Set<SbiFunctionInputVariable> hibVarsSet = hibCatFunction.getSbiFunctionInputVariables();
		for (String varName : updatedCatalogFunction.getInputVariables().keySet()) {
			String varValue = updatedCatalogFunction.getInputVariables().get(varName);
			SbiFunctionInputVariable var = new SbiFunctionInputVariable(new SbiFunctionInputVariableId(hibCatFunction.getFunctionId(), varName),
					hibCatFunction, varValue);
			updateSbiCommonInfo4Insert(var);
			hibVarsSet.add(var);
		}

	}

	private void updateOutputs(SbiCatalogFunction hibCatFunction, Session session, CatalogFunction updatedCatalogFunction) throws Exception {

		// delete outs not reinserted and update outputs that are already presents
		List<SbiFunctionOutput> outToRemove = new ArrayList<SbiFunctionOutput>();
		for (Object o : hibCatFunction.getSbiFunctionOutputs()) {
			SbiFunctionOutput oi = (SbiFunctionOutput) o;
			boolean delete = true;
			if (updatedCatalogFunction.getOutputs().keySet().contains(oi.getId().getLabel())) {
				delete = false;
				updateSbiCommonInfo4Update(oi);
				Domain domain = DAOFactory.getDomainDAO().loadDomainByCodeAndValue("FUNCTION_OUTPUT",
						updatedCatalogFunction.getOutputs().get(oi.getId().getLabel()));
				Integer outTypeSbiDomainId = domain.getValueId();

				oi.setOutType(outTypeSbiDomainId);
				updatedCatalogFunction.getOutputs().remove(oi.getId().getLabel());
			}

			if (delete) {
				outToRemove.add(oi);
			}
		}

		for (SbiFunctionOutput otr : outToRemove) {
			SbiFunctionOutput outToRem = (SbiFunctionOutput) session.get(SbiFunctionOutput.class, otr.getId());
			hibCatFunction.getSbiFunctionOutputs().remove(outToRem);
			session.delete(outToRem);
			session.flush();
		}

		// insert outs that are not presents
		Set<SbiFunctionOutput> hibOutSet = hibCatFunction.getSbiFunctionOutputs();
		for (String outLabel : updatedCatalogFunction.getOutputs().keySet()) {
			// String outType = updatedCatalogFunction.getOutputs().get(outLabel);
			Domain domain = DAOFactory.getDomainDAO().loadDomainByCodeAndValue("FUNCTION_OUTPUT", updatedCatalogFunction.getOutputs().get(outLabel));
			Integer outTypeSbiDomainId = domain.getValueId();
			SbiFunctionOutput functionOutput = new SbiFunctionOutput(new SbiFunctionOutputId(hibCatFunction.getFunctionId(), outLabel), hibCatFunction,
					outTypeSbiDomainId);
			updateSbiCommonInfo4Insert(functionOutput);
			hibOutSet.add(functionOutput);
		}

	}

	private boolean updatedFunctionContainsDatasets(List<String> inputDatasets, int dsId) throws Exception {
		IDataSetDAO dataSetDAO = DAOFactory.getDataSetDAO();
		for (String dsLab : inputDatasets) {
			IDataSet dataset = dataSetDAO.loadDataSetById(dsId);
			if (dataset.getLabel().equals(dsLab)) {
				return true;
			}
		}
		return false;
	}

	private void removeFileWithName(String fileName, List<CatalogFunctionInputFile> inputFiles) {
		CatalogFunctionInputFile fileToRemove = new CatalogFunctionInputFile();
		for (CatalogFunctionInputFile file : inputFiles) {
			if (file.getFileName().equals(fileName)) {
				fileToRemove = file;
			}
		}
		inputFiles.remove(fileToRemove);
	}

	private byte[] getBytesFromFileName(String fileName, List<CatalogFunctionInputFile> inputFiles) {
		for (CatalogFunctionInputFile file : inputFiles) {
			if (file.getFileName().equals(fileName)) {
				return file.getContent();
			}
		}
		return new byte[0];

	}

	private boolean updatedFunctionContainsFile(List<CatalogFunctionInputFile> inputFiles, String fileName) {

		for (CatalogFunctionInputFile file : inputFiles) {
			if (file.getFileName().equals(fileName)) {
				return true;
			}
		}
		return false;
	}

	private boolean hibCatFunctionContainsVar(Set<SbiFunctionInputVariable> hibVarsSet, String updatedKey, Map<String, String> updatedInputVariables) {
		for (SbiFunctionInputVariable hibVar : hibVarsSet) {
			String hibVarName = hibVar.getId().getVarName();
			if (updatedInputVariables.containsKey(hibVarName)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void deleteCatalogFunction(int id) {

		Session session;
		Transaction transaction;

		logger.debug("IN");

		session = null;
		transaction = null;
		try {
			session = getSession();
			Assert.assertNotNull(session, "session cannot be null");
			transaction = session.beginTransaction();
			Assert.assertNotNull(transaction, "transaction cannot be null");

			SbiCatalogFunction hibCatFunction = (SbiCatalogFunction) session.get(SbiCatalogFunction.class, id);
			session.delete(hibCatFunction);

			transaction.commit();

		} catch (Throwable t) {
			if (transaction != null && transaction.isActive()) {
				transaction.rollback();
			}

			throw new SpagoBIDAOException("An unexpected error occured while updating catalog function", t);
		} finally {
			if (session != null && session.isOpen()) {
				session.close();
			}
			logger.debug("OUT");
		}

	}

	@Override
	public SbiCatalogFunction getCatalogFunctionById(int id) {

		Session session;
		Transaction transaction = null;
		SbiCatalogFunction sbiCatalogFunction = null;

		logger.debug("IN");

		session = null;
		try {

			session = getSession();
			Assert.assertNotNull(session, "session cannot be null");
			transaction = session.beginTransaction();
			sbiCatalogFunction = (SbiCatalogFunction) session.get(SbiCatalogFunction.class, id);
			transaction.commit();

		} catch (Throwable t) {
			if (transaction != null && transaction.isActive()) {
				transaction.rollback();
			}
			throw new SpagoBIDAOException("An error occured while reading Catalog Functions from DB", t);

		} finally {
			if (session != null && session.isOpen()) {
				session.close();
			}
			logger.debug("OUT");
		}
		return sbiCatalogFunction;

	}

	@Override
	public SbiCatalogFunction getCatalogFunctionByLabel(String label) {

		Session session;
		Transaction transaction = null;
		SbiCatalogFunction sbiCatalogFunction = null;

		logger.debug("IN");

		session = null;
		try {

			session = getSession();
			Assert.assertNotNull(session, "session cannot be null");
			transaction = session.beginTransaction();

			String hql = "FROM SbiCatalogFunction F WHERE F.label = ?";
			Query query = session.createQuery(hql);
			query.setString(0, label);
			sbiCatalogFunction = (SbiCatalogFunction) query.uniqueResult();

			transaction.commit();

		} catch (Throwable t) {
			if (transaction != null && transaction.isActive()) {
				transaction.rollback();
			}
			throw new SpagoBIDAOException("An error occured while reading Catalog Functions from DB", t);

		} finally {
			if (session != null && session.isOpen()) {
				session.close();
			}
			logger.debug("OUT");
		}
		return sbiCatalogFunction;

	}
}
