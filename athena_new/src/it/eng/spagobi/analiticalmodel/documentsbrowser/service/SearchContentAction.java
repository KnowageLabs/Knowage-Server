/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.analiticalmodel.documentsbrowser.service;

import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.commons.SingletonConfig;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.serializer.DocumentsJSONSerializer;
import it.eng.spagobi.commons.serializer.SerializerFactory;
import it.eng.spagobi.commons.services.AbstractSpagoBIAction;
import it.eng.spagobi.commons.utilities.ObjectsAccessVerifier;
import it.eng.spagobi.commons.utilities.SpagoBIUtilities;
import it.eng.spagobi.commons.utilities.indexing.IndexingConstants;
import it.eng.spagobi.commons.utilities.indexing.LuceneSearcher;
import it.eng.spagobi.commons.utilities.messages.MessageBuilder;
import it.eng.spagobi.utilities.exceptions.SpagoBIException;
import it.eng.spagobi.utilities.service.JSONSuccess;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.store.FSDirectory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Antonella Giachino (antonella.giachino@eng.it)
 * 
 */
public class SearchContentAction extends AbstractSpagoBIAction {

	// REQUEST PARAMETERS
	public static final String ATTRIBUTES = "attributes";
	public static final String SIMILAR = "similar";

	// logger component
	private static Logger logger = Logger.getLogger(SearchContentAction.class);

	@Override
	public void doService() {

		List objects;

		logger.debug("IN");

		try {
			UserProfile profile = (UserProfile) getUserProfile();

			Vector<String> fieldsToSearch = new Vector<String>();
			String valueFilter = getAttributeAsString(SpagoBIConstants.VALUE_FILTER);

			String attributes = getAttributeAsString(ATTRIBUTES);
			String metaDataToSearch = null;
			if (attributes != null) {
				if (attributes.equalsIgnoreCase("ALL")) {// SEARCH IN ALL FIELDS
					fieldsToSearch.add(IndexingConstants.BIOBJ_LABEL);
					fieldsToSearch.add(IndexingConstants.BIOBJ_NAME);
					fieldsToSearch.add(IndexingConstants.BIOBJ_DESCR);
					fieldsToSearch.add(IndexingConstants.METADATA);
					// search metadata binary content
					fieldsToSearch.add(IndexingConstants.CONTENTS);
					// search subobject fields
					fieldsToSearch.add(IndexingConstants.SUBOBJ_DESCR);
					fieldsToSearch.add(IndexingConstants.SUBOBJ_NAME);
				} else if (attributes.equalsIgnoreCase("LABEL")) {// SEARCH IN
																	// LABEL DOC
					fieldsToSearch.add(IndexingConstants.BIOBJ_LABEL);
				} else if (attributes.equalsIgnoreCase("NAME")) {// SEARCH IN
																	// NAME DOC
					fieldsToSearch.add(IndexingConstants.BIOBJ_NAME);
				} else if (attributes.equalsIgnoreCase("DESCRIPTION")) {// SEARCH
																		// IN
																		// DESCRIPTION
																		// DOC
					fieldsToSearch.add(IndexingConstants.BIOBJ_DESCR);
				} else {// SEARCH IN CATEGORIES DOC
						// get categories name
					metaDataToSearch = attributes;
					// fieldsToSearch.add(IndexingConstants.METADATA);
					fieldsToSearch.add(IndexingConstants.CONTENTS);
				}

			}

			boolean similar = getAttributeAsBoolean(SIMILAR);

			logger.debug("Parameter [" + SpagoBIConstants.VALUE_FILTER + "] is equal to [" + valueFilter + "]");
			String indexBasePath = "";
			String jndiBean = SingletonConfig.getInstance().getConfigValue("SPAGOBI.RESOURCE_PATH_JNDI_NAME");
			if (jndiBean != null) {
				indexBasePath = SpagoBIUtilities.readJndiResource(jndiBean);
			}
			String index = indexBasePath + "/idx";
			IndexReader reader;
			HashMap returned = null;
			try {
				reader = IndexReader.open(FSDirectory.open(new File(index)), true);
				// read-only=true
				IndexSearcher searcher = new IndexSearcher(reader);

				String[] fields = new String[fieldsToSearch.size()];
				fieldsToSearch.toArray(fields);

				// getting documents

				if (similar) {
					returned = LuceneSearcher.searchIndexFuzzy(searcher, valueFilter, index, fields, metaDataToSearch);
				} else {
					returned = LuceneSearcher.searchIndex(searcher, valueFilter, index, fields, metaDataToSearch);
				}
				ScoreDoc[] hits = (ScoreDoc[]) returned.get("hits");

				objects = new ArrayList();
				if (hits != null) {
					for (int i = 0; i < hits.length; i++) {
						ScoreDoc hit = hits[i];
						Document doc = searcher.doc(hit.doc);
						String biobjId = doc.get(IndexingConstants.BIOBJ_ID);

						BIObject obj = DAOFactory.getBIObjectDAO().loadBIObjectForDetail(Integer.valueOf(biobjId));
						if (obj != null) {
							boolean canSee = ObjectsAccessVerifier.canSee(obj, profile);
							if (canSee) {
								objects.add(obj);
							}
						}
					}
				}
				searcher.close();
			} catch (CorruptIndexException e) {
				logger.error(e.getMessage(), e);
				throw new SpagoBIException("Index corrupted", e);

			} catch (IOException e) {
				logger.error(e.getMessage(), e);
				throw new SpagoBIException("Unable to read index", e);

			} // only searching, so
			catch (ParseException e) {
				logger.error(e.getMessage(), e);
				throw new SpagoBIException("Wrong query syntax", e);

			}

			JSONArray documentsJSON = (JSONArray) SerializerFactory.getSerializer("application/json").serialize(objects, null);
			for (int i = 0; i < documentsJSON.length(); i++) {
				JSONObject jsonobj = documentsJSON.getJSONObject(i);
				String biobjid = jsonobj.getString("id");
				String summary = (String) returned.get(biobjid);
				jsonobj.put("summary", summary);
				String views = (String) returned.get(biobjid + "-views");
				jsonobj.put("views", views);
			}
			Collection func = profile.getFunctionalities();

			if (func.contains("SeeMetadataFunctionality")) {
				JSONObject showmetadataAction = new JSONObject();
				showmetadataAction.put("name", "showmetadata");
				showmetadataAction.put("description", "Show Metadata");
				for (int i = 0; i < documentsJSON.length(); i++) {
					JSONObject documentJSON = documentsJSON.getJSONObject(i);
					documentJSON.getJSONArray(DocumentsJSONSerializer.ACTIONS).put(showmetadataAction);
				}
			}
			// add detail functionality when user has
			if (func.contains("DocumentDetailManagement")) {
				JSONObject detailAction = new JSONObject();
				MessageBuilder msgBuild = new MessageBuilder();
				detailAction.put("name", "detail");
				detailAction.put("description", msgBuild.getMessage("sbiobjects.actions.detail.description", getLocale()));
				for (int i = 0; i < documentsJSON.length(); i++) {
					JSONObject documentJSON = documentsJSON.getJSONObject(i);
					// check user can dev on this object
					if (ObjectsAccessVerifier.canDevBIObject(documentJSON.getInt(DocumentsJSONSerializer.ID), profile)) {
						documentJSON.getJSONArray(DocumentsJSONSerializer.ACTIONS).put(detailAction);
					}
				}
			}
			JSONObject documentsResponseJSON = createJSONResponseDocuments(documentsJSON);

			try {
				writeBackToClient(new JSONSuccess(createJSONResponse(documentsResponseJSON)));
			} catch (IOException e) {
				logger.error(e.getMessage(), e);
				throw new SpagoBIException("Impossible to write back the responce to the client", e);
			}

		} catch (Exception e) {
			logger.error("Excepiton", e);
		} finally {
			logger.debug("OUT");
		}
	}

	/**
	 * Creates a json array with children document informations
	 * 
	 * @param rows
	 * @return
	 * @throws JSONException
	 */
	private JSONObject createJSONResponseDocuments(JSONArray rows) throws JSONException {
		JSONObject results;

		results = new JSONObject();
		results.put("title", "Documents");
		results.put("icon", "document.png");
		results.put("samples", rows);
		return results;
	}

	/**
	 * Creates a json array with children document informations
	 * 
	 * @param rows
	 * @return
	 * @throws JSONException
	 */
	private JSONObject createJSONResponse(JSONObject documents) throws JSONException {
		JSONObject results = new JSONObject();
		JSONArray folderContent = new JSONArray();

		folderContent.put(documents);
		results.put("folderContent", folderContent);

		return results;
	}

}
