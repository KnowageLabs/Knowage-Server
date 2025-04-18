/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.
 *
 * Knowage is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Knowage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package it.eng.spagobi.tools.scheduler.to;

import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.quartz.JobExecutionContext;

import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.tools.dataset.common.datastore.IDataStore;

public class DispatchContext implements Serializable {

	private static final long serialVersionUID = 1L;

	// stored properties
	private boolean snapshootDispatchChannelEnabled = false;
	private boolean functionalityTreeDispatchChannelEnabled = false;
	private boolean mailDispatchChannelEnabled = false;
	private boolean distributionListDispatchChannelEnabled = false;
	private boolean javaClassDispatchChannelEnabled = false;
	private boolean fileSystemDisptachChannelEnabled = false;

	// -------------------------------------------------------------------------
	// file dispatcher context properties
	// -------------------------------------------------------------------------

	/*
	 * the path of the folder to which the generated files will be disptached
	 */
	private String destinationFolder = "";

	/*
	 * True if the destination folder path properties is relative to the resource folder. If so the the dispatch path will be generated by the following code
	 * ... File destFolder = new File(resourceFolder, destinationFolder);
	 */
	private boolean destinationFolderRelativeToResourceFolder = false;

	private String functionalityTreeFolderLabel = "";
	private String owner = "";
	private boolean processMonitoringEnabled = true;

	// -------------------------------------------------------------------------
	// snapshot dispatcher context properties
	// -------------------------------------------------------------------------

	private String snapshotName = "";
	private String snapshotDescription = "";
	private String snapshotHistoryLength = "";
	private String documentName = "";
	private String documentDescription = "";
	private String documentHistoryLength = "";
	private boolean useFixedFolder = false;
	private String foldersTo = "";
	private boolean useFolderDataSet = false;
	private String dataSetFolderLabel = null;
	private String dataSetFolderParameterLabel = null;

	private boolean useFixedRecipients = false;
	private String mailTos = "";
	private boolean useDataSet = false;
	private String dataSetLabel = null;
	private String dataSetParameterLabel = null;
	private boolean useExpression = false;
	private String expression = "";
	private String functionalityIds = "";
	private String mailSubj = "";
	private String mailTxt = "";
	private String javaClassPath = "";
	private int biobjId = 0;
	private List dlIds = new ArrayList();
	private boolean reportNameInSubject;

	// injected properties
	private IEngUserProfile userProfile;
	private String nameSuffix = "";
	private String descriptionSuffix = "";

	private JobExecutionContext jobExecutionContext;
	private String fileExtension;
	private IDataStore folderDispatchDataSotre;

	private String contentType;
	private IDataStore emailDispatchDataStore;
	private Map<String, String> parametersMap;
	private int totalNumberOfDocumentsToDispatch;
	private int indexNumberOfDocumentToDispatch;

	private String documentLabels;

	// indicate if this dispatch context establish settings for unique mail
	private boolean uniqueMail;
	// indicate if in present scheduling there is a unique mail logic
	private boolean globalUniqueMail;

	// folder to pass inc ase of unique mail case
	private String tempFolderName;
	private String tempFolderPath;

	// property set to zip document in case of file
	private boolean zipFileDocument = false;
	// property to set name of file
	private String fileName;
	private String zipFileName;

	// property set to zip document in case of e-mail
	private boolean zipMailDocument = false;
	// property to set name of zip file and of containedFile
	private String zipMailName;
	private String containedFileName;

	private long schedulationStartDate;

	private int sequence;

	private String outputTypeTrigger;

	public boolean isZipMailDocument() {
		return zipMailDocument;
	}

	public void setZipMailDocument(boolean zipMailDocument) {
		this.zipMailDocument = zipMailDocument;
	}

	public boolean isDistributionListDispatchChannelEnabled() {
		return distributionListDispatchChannelEnabled;
	}

	public void setDistributionListDispatchChannelEnabled(boolean enabled) {
		this.distributionListDispatchChannelEnabled = enabled;
	}

	public boolean isFileSystemDispatchChannelEnabled() {
		return fileSystemDisptachChannelEnabled;
	}

	public void setFileSystemDisptachChannelEnabled(boolean enabled) {
		fileSystemDisptachChannelEnabled = enabled;
	}

	public boolean isFunctionalityTreeDispatchChannelEnabled() {
		return functionalityTreeDispatchChannelEnabled;
	}

	public void setFunctionalityTreeDispatchChannelEnabled(boolean enabled) {
		this.functionalityTreeDispatchChannelEnabled = enabled;
	}

	public boolean isSnapshootDispatchChannelEnabled() {
		return snapshootDispatchChannelEnabled;
	}

	public void setSnapshootDispatchChannelEnabled(boolean enabled) {
		this.snapshootDispatchChannelEnabled = enabled;
	}

	public boolean isMailDispatchChannelEnabled() {
		return mailDispatchChannelEnabled;
	}

	public void setMailDispatchChannelEnabled(boolean enabled) {
		this.mailDispatchChannelEnabled = enabled;
	}

	public boolean isJavaClassDispatchChannelEnabled() {
		return javaClassDispatchChannelEnabled;
	}

	public void setJavaClassDispatchChannelEnabled(boolean enabled) {
		this.javaClassDispatchChannelEnabled = enabled;
	}

	public boolean isReportNameInSubject() {
		return reportNameInSubject;
	}

	public void setReportNameInSubject(boolean reportNameInSubject) {
		this.reportNameInSubject = reportNameInSubject;
	}

	public boolean isUniqueMail() {
		return uniqueMail;
	}

	public void setUniqueMail(boolean uniqueMail) {
		this.uniqueMail = uniqueMail;
	}

	/**
	 * @return the useFixedFolder
	 */
	public boolean isUseFixedFolder() {
		return useFixedFolder;
	}

	/**
	 * @param useFixedFolder
	 *            the useFixedFolder to set
	 */
	public void setUseFixedFolder(boolean useFixedFolder) {
		this.useFixedFolder = useFixedFolder;
	}

	/**
	 * @return the foldersTo
	 */
	public String getFoldersTo() {
		return foldersTo;
	}

	/**
	 * @param foldersTo
	 *            the foldersTo to set
	 */
	public void setFoldersTo(String foldersTo) {
		this.foldersTo = foldersTo;
	}

	/**
	 * @return the useFolderDataSet
	 */
	public boolean isUseFolderDataSet() {
		return useFolderDataSet;
	}

	/**
	 * @param useFolderDataSet
	 *            the useFolderDataSet to set
	 */
	public void setUseFolderDataSet(boolean useFolderDataSet) {
		this.useFolderDataSet = useFolderDataSet;
	}

	/**
	 * @return the dataSetFolderLabel
	 */
	public String getDataSetFolderLabel() {
		return dataSetFolderLabel;
	}

	/**
	 * @param dataSetFolderLabel
	 *            the dataSetFolderLabel to set
	 */
	public void setDataSetFolderLabel(String dataSetFolderLabel) {
		this.dataSetFolderLabel = dataSetFolderLabel;
	}

	/**
	 * @return the dataSetFolderParameterLabel
	 */
	public String getDataSetFolderParameterLabel() {
		return dataSetFolderParameterLabel;
	}

	/**
	 * @param dataSetFolderParameterLabel
	 *            the dataSetFolderParameterLabel to set
	 */
	public void setDataSetFolderParameterLabel(String dataSetFolderParameterLabel) {
		this.dataSetFolderParameterLabel = dataSetFolderParameterLabel;
	}

	/**
	 * Removes the dl id.
	 *
	 * @param dlId
	 *            the dl id
	 */
	public void removeDlId(Integer dlId) {
		this.dlIds.remove(dlId);
	}

	/**
	 * Adds the dl id.
	 *
	 * @param dlId
	 *            the dl id
	 */
	public void addDlId(Integer dlId) {
		this.dlIds.add(dlId);
	}

	/**
	 * Gets the document description.
	 *
	 * @return the document description
	 */
	public String getDocumentDescription() {
		return documentDescription;
	}

	/**
	 * Sets the document description.
	 *
	 * @param documentDescription
	 *            the new document description
	 */
	public void setDocumentDescription(String documentDescription) {
		this.documentDescription = documentDescription;
	}

	/**
	 * Gets the document history length.
	 *
	 * @return the document history length
	 */
	public String getDocumentHistoryLength() {
		return documentHistoryLength;
	}

	/**
	 * Sets the document history length.
	 *
	 * @param documentHistoryLength
	 *            the new document history length
	 */
	public void setDocumentHistoryLength(String documentHistoryLength) {
		this.documentHistoryLength = documentHistoryLength;
	}

	/**
	 * Gets the document name.
	 *
	 * @return the document name
	 */
	public String getDocumentName() {
		return documentName;
	}

	/**
	 * Sets the document name.
	 *
	 * @param documentName
	 *            the new document name
	 */
	public void setDocumentName(String documentName) {
		this.documentName = documentName;
	}

	/**
	 * Gets the mail tos.
	 *
	 * @return the mail tos
	 */
	public String getMailTos() {
		return mailTos;
	}

	/**
	 * Sets the mail tos.
	 *
	 * @param mailTos
	 *            the new mail tos
	 */
	public void setMailTos(String mailTos) {
		this.mailTos = mailTos;
	}

	/**
	 * Gets the mail subj.
	 *
	 * @return the mail subj
	 */
	public String getMailSubj() {
		return mailSubj;
	}

	/**
	 * Sets the mail subj.
	 *
	 * @param mailSubj
	 *            the new mail subj
	 */
	public void setMailSubj(String mailSubj) {
		this.mailSubj = mailSubj;
	}

	/**
	 * Gets the mail txt.
	 *
	 * @return the mail txt
	 */
	public String getMailTxt() {
		return mailTxt;
	}

	/**
	 * Sets the mail txt.
	 *
	 * @param mailTxt
	 *            the new mail txt
	 */
	public void setMailTxt(String mailTxt) {
		this.mailTxt = mailTxt;
	}

	/**
	 * Gets the snapshot description.
	 *
	 * @return the snapshot description
	 */
	public String getSnapshotDescription() {
		return snapshotDescription;
	}

	/**
	 * Sets the snapshot description.
	 *
	 * @param snapshotDescription
	 *            the new snapshot description
	 */
	public void setSnapshotDescription(String snapshotDescription) {
		this.snapshotDescription = snapshotDescription;
	}

	/**
	 * Gets the snapshot history length.
	 *
	 * @return the snapshot history length
	 */
	public String getSnapshotHistoryLength() {
		return snapshotHistoryLength;
	}

	/**
	 * Sets the snapshot history length.
	 *
	 * @param snapshotHistoryLength
	 *            the new snapshot history length
	 */
	public void setSnapshotHistoryLength(String snapshotHistoryLength) {
		this.snapshotHistoryLength = snapshotHistoryLength;
	}

	/**
	 * Gets the snapshot name.
	 *
	 * @return the snapshot name
	 */
	public String getSnapshotName() {
		return snapshotName;
	}

	/**
	 * Sets the snapshot name.
	 *
	 * @param snapshotName
	 *            the new snapshot name
	 */
	public void setSnapshotName(String snapshotName) {
		this.snapshotName = snapshotName;
	}

	/**
	 * Gets the functionality ids.
	 *
	 * @return the functionality ids
	 */
	public String getFunctionalityIds() {
		return functionalityIds;
	}

	/**
	 * Sets the functionality ids.
	 *
	 * @param functionalityIds
	 *            the new functionality ids
	 */
	public void setFunctionalityIds(String functionalityIds) {
		this.functionalityIds = functionalityIds;
	}

	/**
	 * Gets the dl ids.
	 *
	 * @return the dl ids
	 */
	public List getDlIds() {
		return dlIds;
	}

	/**
	 * Sets the dl ids.
	 *
	 * @param dlIds
	 *            the new dl ids
	 */
	public void setDlIds(List dlIds) {
		this.dlIds = dlIds;
	}

	/**
	 * Gets the biobj id.
	 *
	 * @return the biobj id
	 */
	public int getBiobjId() {
		return biobjId;
	}

	/**
	 * Sets the biobj id.
	 *
	 * @param biobjId
	 *            the new biobj id
	 */
	public void setBiobjId(int biobjId) {
		this.biobjId = biobjId;
	}

	public boolean isUseDataSet() {
		return useDataSet;
	}

	public void setUseDataSet(boolean useDataSet) {
		this.useDataSet = useDataSet;
	}

	public String getDataSetLabel() {
		return dataSetLabel;
	}

	public void setDataSetLabel(String dataSetLabel) {
		this.dataSetLabel = dataSetLabel;
	}

	public boolean isUseExpression() {
		return useExpression;
	}

	public void setUseExpression(boolean useExpression) {
		this.useExpression = useExpression;
	}

	public String getExpression() {
		return expression;
	}

	public void setExpression(String expression) {
		this.expression = expression;
	}

	public String getDataSetParameterLabel() {
		return dataSetParameterLabel;
	}

	public void setDataSetParameterLabel(String dataSetParameterLabel) {
		this.dataSetParameterLabel = dataSetParameterLabel;
	}

	public boolean isUseFixedRecipients() {
		return useFixedRecipients;
	}

	public void setUseFixedRecipients(boolean useFixedRecipients) {
		this.useFixedRecipients = useFixedRecipients;
	}

	public String getJavaClassPath() {
		return javaClassPath;
	}

	public void setJavaClassPath(String javaClassPath) {
		this.javaClassPath = javaClassPath;
	}

	public String getNameSuffix() {
		return nameSuffix;
	}

	public void setNameSuffix(String nameSuffix) {
		this.nameSuffix = nameSuffix;
	}

	public String getDescriptionSuffix() {
		return descriptionSuffix;
	}

	public void setDescriptionSuffix(String descriptionSuffix) {
		this.descriptionSuffix = descriptionSuffix;
	}

	public IEngUserProfile getUserProfile() {
		return userProfile;
	}

	public void setUserProfile(IEngUserProfile userProfile) {
		this.userProfile = userProfile;
	}

	public JobExecutionContext getJobExecutionContext() {
		return jobExecutionContext;
	}

	public void setJobExecutionContext(JobExecutionContext jobExecutionContext) {
		this.jobExecutionContext = jobExecutionContext;
	}

	public String getFileExtension() {
		return fileExtension;
	}

	public void setFileExtension(String fileExtension) {
		this.fileExtension = fileExtension;
	}

	public IDataStore getFolderDispatchDataSotre() {
		return folderDispatchDataSotre;
	}

	public void setFolderDispatchDataSotre(IDataStore folderDispatchDataSotre) {
		this.folderDispatchDataSotre = folderDispatchDataSotre;
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public IDataStore getEmailDispatchDataStore() {
		return emailDispatchDataStore;
	}

	public void setEmailDispatchDataStore(IDataStore emailDispatchDataStore) {
		this.emailDispatchDataStore = emailDispatchDataStore;
	}

	public Map<String, String> getParametersMap() {
		return parametersMap;
	}

	public void setParametersMap(Map<String, String> parametersMap) {
		this.parametersMap = parametersMap;
	}

	public boolean isProcessMonitoringEnabled() {
		return processMonitoringEnabled;
	}

	public void setProcessMonitoringEnabled(boolean processMonitoringEnabled) {
		this.processMonitoringEnabled = processMonitoringEnabled;
	}

	public boolean isFileSystemDisptachChannelEnabled() {
		return fileSystemDisptachChannelEnabled;
	}

	public String getDestinationFolder() {
		return destinationFolder;
	}

	public void setDestinationFolder(String destinationFolder) {
		this.destinationFolder = destinationFolder;
	}

	public boolean isDestinationFolderRelativeToResourceFolder() {
		return destinationFolderRelativeToResourceFolder;
	}

	public void setDestinationFolderRelativeToResourceFolder(boolean destinationFolderRelativeToResourceFolder) {
		this.destinationFolderRelativeToResourceFolder = destinationFolderRelativeToResourceFolder;
	}

	public String getFunctionalityTreeFolderLabel() {
		return functionalityTreeFolderLabel;
	}

	public void setFunctionalityTreeFolderLabel(String functionalityTreeFolderLabel) {
		this.functionalityTreeFolderLabel = functionalityTreeFolderLabel;
	}

	public String getOwner() {
		return owner;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}

	public int getTotalNumberOfDocumentsToDispatch() {
		return totalNumberOfDocumentsToDispatch;
	}

	public void setTotalNumberOfDocumentsToDispatch(int totalNumberOfDocumentsToDispatch) {
		this.totalNumberOfDocumentsToDispatch = totalNumberOfDocumentsToDispatch;
	}

	public int getIndexNumberOfDocumentToDispatch() {
		return indexNumberOfDocumentToDispatch;
	}

	public void setIndexNumberOfDocumentToDispatch(int indexNumberOfDocumentToDispatch) {
		this.indexNumberOfDocumentToDispatch = indexNumberOfDocumentToDispatch;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String zippedFileName) {
		this.fileName = zippedFileName;
	}

	public String getZipFileName() {
		return zipFileName;
	}

	public void setZipFileName(String zipFileName) {
		this.zipFileName = zipFileName;
	}

	public String getContainedFileName() {
		return containedFileName;
	}

	public void setContainedFileName(String containedFileName) {
		this.containedFileName = containedFileName;
	}

	public boolean isZipFileDocument() {
		return zipFileDocument;
	}

	public void setZipFileDocument(boolean zipFileDocument) {
		this.zipFileDocument = zipFileDocument;
	}

	public String getZipMailName() {
		return zipMailName;
	}

	public void setZipMailName(String zipMailName) {
		this.zipMailName = zipMailName;
	}

	public boolean isGlobalUniqueMail() {
		return globalUniqueMail;
	}

	public void setGlobalUniqueMail(boolean globalUniqueMail) {
		this.globalUniqueMail = globalUniqueMail;
	}

	public String getTempFolderName() {
		return tempFolderName;
	}

	public void setTempFolderName(String tempFolderPath) {
		this.tempFolderName = tempFolderPath;
	}

	public String getTempFolderPath() {
		return tempFolderPath;
	}

	public void setTempFolderPath(String tempFolderPath) {
		this.tempFolderPath = tempFolderPath;
	}

	public String getDocumentLabels() {
		return documentLabels;
	}

	public void setDocumentLabels(String documentLabels) {
		this.documentLabels = documentLabels;
	}

	public long getSchedulationStartDate() {
		return schedulationStartDate;
	}

	public void setSchedulationStartDate(long schedulationStartDate) {
		this.schedulationStartDate = schedulationStartDate;
	}
	public int getSequence() {
		return sequence;
	}

	public void setSequence(int sequence) {
		this.sequence = sequence;
	}


	private final void writeObject(ObjectOutputStream aOutputStream) {
		  throw new UnsupportedOperationException("Security violation : cannot serialize object to a stream");
	}

	public String getOutputTypeTrigger() {
		return outputTypeTrigger;
	}

	public void setOutputTypeTrigger(String outputTypeTrigger) {
		this.outputTypeTrigger = outputTypeTrigger;
	}

}
