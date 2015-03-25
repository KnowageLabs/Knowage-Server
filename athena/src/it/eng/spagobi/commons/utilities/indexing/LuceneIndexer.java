/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.commons.utilities.indexing;

import it.eng.spago.base.SourceBean;
import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.analiticalmodel.document.bo.DocumentMetadataProperty;
import it.eng.spagobi.analiticalmodel.document.bo.SubObject;
import it.eng.spagobi.commons.SingletonConfig;
import it.eng.spagobi.commons.bo.Domain;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.dao.IBinContentDAO;
import it.eng.spagobi.commons.utilities.JTidyHTMLHandler;
import it.eng.spagobi.commons.utilities.SpagoBIUtilities;
import it.eng.spagobi.tenant.TenantManager;
import it.eng.spagobi.tools.objmetadata.bo.ObjMetacontent;
import it.eng.spagobi.tools.objmetadata.bo.ObjMetadata;
import it.eng.spagobi.tools.objmetadata.dao.IObjMetacontentDAO;
import it.eng.spagobi.tools.objmetadata.dao.IObjMetadataDAO;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

/**Indexing class.
 * @author franceschini
 *
 */
public class LuceneIndexer {

	private static IndexReader reader; // existing index
	private static IndexWriter writer; // new index being built

	private static final String LONG_TEXT = "LONG_TEXT";// html
	private static final String SHORT_TEXT = "SHORT_TEXT";// simple text
	
	

	static private Logger logger = Logger.getLogger(LuceneIndexer.class);
	
	
	/**Method to add biObj input to lucene index (no metadata included) 
	 * @param biObj
	 */
	public static void addBiobjToIndex(BIObject biObj){
		logger.debug("IN");
		try {
			String indexBasePath = "";
			String jndiBean = SingletonConfig.getInstance().getConfigValue("SPAGOBI.RESOURCE_PATH_JNDI_NAME");
			if (jndiBean != null) {	
				indexBasePath = SpagoBIUtilities.readJndiResource(jndiBean);
			}
			String index = indexBasePath+"/idx";
			Date start = new Date();
			
			writer = new IndexWriter(FSDirectory.open(new File(index)),
					new StandardAnalyzer(Version.LUCENE_CURRENT), false,
					new IndexWriter.MaxFieldLength(1000000));
			Document doc = new Document();
			String uid = createUidDocument(null, String.valueOf(biObj.getId().intValue()));
			doc.add(new Field(IndexingConstants.UID, uid , Field.Store.YES,
					Field.Index.NOT_ANALYZED));
			doc.add(new Field(IndexingConstants.BIOBJ_ID, String.valueOf(biObj.getId().intValue()), Field.Store.YES,
					Field.Index.NOT_ANALYZED));
			doc.add(new Field(IndexingConstants.BIOBJ_NAME, biObj.getName(),
					Field.Store.NO, Field.Index.ANALYZED));
			if(biObj.getDescription() != null){
				doc.add(new Field(IndexingConstants.BIOBJ_DESCR, biObj.getDescription(),
					Field.Store.NO, Field.Index.ANALYZED));
			}
			doc.add(new Field(IndexingConstants.BIOBJ_LABEL, biObj.getLabel(),
					Field.Store.NO, Field.Index.ANALYZED));
			
			doc.add(new Field(IndexingConstants.TENANT, getTenant(biObj), Field.Store.YES,
					Field.Index.NOT_ANALYZED));
			
			writer.addDocument(doc);
			writer.optimize();
			writer.close();

			Date end = new Date();

			logger.info("Indexing time:: " + (end.getTime() - start.getTime())
					+ " milliseconds");
			logger.debug("OUT");

		} catch (Exception e) {
			logger.error(e.getMessage(),e);
		}
		
	}
	/**Method to update a lucene document based on biObj input parameter
	 * @param biObj
	 */
	public static void updateBiobjInIndex(BIObject biObj, boolean delete){
		logger.debug("IN");
		try {
			String indexBasePath = "";
			String jndiBean = SingletonConfig.getInstance().getConfigValue("SPAGOBI.RESOURCE_PATH_JNDI_NAME");
			if (jndiBean != null) {
				indexBasePath = SpagoBIUtilities.readJndiResource(jndiBean);
			}
			String index = indexBasePath+"/idx";
			Date start = new Date();
			
			writer = new IndexWriter(FSDirectory.open(new File(index)),
					new StandardAnalyzer(Version.LUCENE_CURRENT), false,
					new IndexWriter.MaxFieldLength(1000000));
			
			ArrayList<String> uids = new ArrayList<String>();
			//checks whether biobj has metadata content
			List metadata = DAOFactory.getObjMetadataDAO().loadAllObjMetadata();
			if (metadata != null && !metadata.isEmpty()) {
				ByteArrayInputStream bais = null;
				Iterator it = metadata.iterator();
				while (it.hasNext()) {
					ObjMetadata objMetadata = (ObjMetadata) it.next();
					ObjMetacontent objMetacontent = (ObjMetacontent) DAOFactory.getObjMetacontentDAO().loadObjMetacontent(objMetadata.getObjMetaId(), biObj.getId(), null);
					if(objMetacontent != null){
						Integer binId = objMetacontent.getBinaryContentId();
						String uid = createUidDocument(String.valueOf(binId.intValue()), String.valueOf(biObj.getId().intValue()));
						Integer idDomain = objMetadata.getDataType();
						Domain domain = DAOFactory.getDomainDAO().loadDomainById(idDomain);
						String binIdString = String.valueOf(binId.intValue());
						
						byte[] content = objMetacontent.getContent();
						String htmlContent = null;
						if (domain.getValueCd().equalsIgnoreCase(LONG_TEXT)) {
							bais = new ByteArrayInputStream(content);
							JTidyHTMLHandler htmlHandler = new JTidyHTMLHandler();
							htmlContent = htmlHandler.getContent(bais);
							bais.close();
						}
						uids.add(uid);
						
						//delete document 
						writer.deleteDocuments(new Term(IndexingConstants.UID, uid));
						if(!delete){
							logger.debug("metadata-->re-add doc to index::"+biObj.getId().intValue());
							//re-add document to index
							Document doc = new Document();
							addSubobjFieldsToDocument(doc, biObj.getId());
							addFieldsToDocument(doc, String.valueOf(binId.intValue()), biObj.getId(),objMetadata.getName(),domain,htmlContent, content);
							doc.add(new Field(IndexingConstants.TENANT, getTenant(biObj), Field.Store.YES,
									Field.Index.NOT_ANALYZED));
							writer.addDocument(doc);
						}
					}
				}
			}
			if(uids.isEmpty()){
				//document with no metadata
				String uid = String.valueOf(biObj.getId().intValue());
				writer.deleteDocuments(new Term(IndexingConstants.UID, uid));
				if(!delete){
					logger.debug("NO metadata-->re-add doc to index::"+biObj.getId().intValue());
					Document doc = new Document();
					doc.add(new Field(IndexingConstants.UID, uid , Field.Store.YES,
							Field.Index.NOT_ANALYZED));
					doc.add(new Field(IndexingConstants.BIOBJ_ID, String.valueOf(biObj.getId().intValue()), Field.Store.YES,
							Field.Index.NOT_ANALYZED));
					doc.add(new Field(IndexingConstants.BIOBJ_NAME, biObj.getName(),
							Field.Store.NO, Field.Index.ANALYZED));
					if(biObj.getDescription() != null){
						doc.add(new Field(IndexingConstants.BIOBJ_DESCR, biObj.getDescription(),
								Field.Store.NO, Field.Index.ANALYZED));
					}
					
					doc.add(new Field(IndexingConstants.TENANT, getTenant(biObj), Field.Store.YES,
							Field.Index.NOT_ANALYZED));
					
					doc.add(new Field(IndexingConstants.BIOBJ_LABEL, biObj.getLabel(),
							Field.Store.NO, Field.Index.ANALYZED));
					addSubobjFieldsToDocument(doc, biObj.getId());
					writer.addDocument(doc);
				}
			}

			writer.optimize();
			//writer.close();

			Date end = new Date();

			logger.info("Indexing time:: " + (end.getTime() - start.getTime())
					+ " milliseconds");
			logger.debug("OUT");

		} catch (Exception e) {
			if(writer != null){
				try {
					writer.rollback();
				} catch (IOException e1) {
					logger.error(e.getMessage());
				}
			}
			logger.error(e.getMessage());
		}finally{
			try {
				writer.commit();
				writer.close();
			} catch (CorruptIndexException e) {
				logger.error(e.getMessage(), e);
			} catch (IOException e) {
				logger.error(e.getMessage(), e);
			}
			
		}
		
	}
	/**Method called to create or increment Lucene index created over metadata binary contents.
	 * @param index  index file
	 * @param create indicating whether index is to be created or updated
	 * @throws IOException 
	 * @throws CorruptIndexException 
	 */
	public void createIndex(File index) throws CorruptIndexException, IOException {
		logger.debug("IN");
		try {
			Date start = new Date();
			writer = new IndexWriter(FSDirectory.open(index),
					new StandardAnalyzer(Version.LUCENE_CURRENT), true,
					new IndexWriter.MaxFieldLength(1000000));
			indexDocs();

			writer.optimize();
			writer.close();

			Date end = new Date();

			logger.info("Indexing time:: " + (end.getTime() - start.getTime())
					+ " milliseconds");
			logger.debug("OUT");

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}finally{
			writer.optimize();
			writer.close();
		}
	}


	/**Method which indexes metadata binary contents. The logic is: if uid exists do not add to Document 
	 * otherwise add it. Binary contents can be either html or simple text.
	 * @throws Exception
	 */
	private static void indexDocs() throws Exception {
		logger.debug("IN");
		ByteArrayInputStream bais = null;
		try{
			//loads all metadata
			IObjMetadataDAO metadataDAO = DAOFactory.getObjMetadataDAO();
			List<ObjMetadata> metadatas = metadataDAO.loadAllObjMetadata();

			//call dao to get biobjects to index
			List<BIObject> biobjects = DAOFactory.getBIObjectDAO().loadAllBIObjects();
			if(biobjects != null){				
				for(int k=0; k<biobjects.size(); k++){

					boolean hasMetacontent = false;
					Integer biObjId = biobjects.get(k).getId();
					//checks if biobject has metadata 
					// loop over list of metadata to get metacontent
					if (metadatas != null) {	
						for (int i = 0; i < metadatas.size(); i++) {
							// look for binary content mimetype
							ObjMetadata metadata = metadatas.get(i);
							Integer metaId = metadata.getObjMetaId();	
							String metaName = metadata.getName();
							IObjMetacontentDAO metacontentDAO = DAOFactory.getObjMetacontentDAO();
							ObjMetacontent metacontent = metacontentDAO.loadObjMetacontent(metaId, biObjId, null);
							//indexes biobject+metadata -->document uid is of type biObjId+"_"+binId
							if(metacontent != null){
								hasMetacontent = true;
								Integer idDomain = metadata.getDataType();
								Domain domain = DAOFactory.getDomainDAO().loadDomainById(idDomain);
								
								Integer binId = metacontent.getBinaryContentId();
								Integer biobjId = metacontent.getBiobjId();
								
								String binIdString = String.valueOf(binId.intValue());
		
								byte[] content = metacontent.getContent();
								String htmlContent = null;
								if (domain.getValueCd().equalsIgnoreCase(LONG_TEXT)) {
									bais = new ByteArrayInputStream(content);
									JTidyHTMLHandler htmlHandler = new JTidyHTMLHandler();
									htmlContent = htmlHandler.getContent(bais);
									bais.close();
								}
								String uid = createUidDocument(binIdString, String.valueOf(biobjId.intValue()));
								Document doc = new Document();
								addFieldsToDocument(doc, binIdString, biobjId, metaName, domain, htmlContent, content);		
								addSubobjFieldsToDocument(doc, biobjId);
								writer.addDocument(doc);

							}
						}
					}
					//else if biobj has no metacontent associated
					//than indexing only biobect
					if(!hasMetacontent){
						String uid = String.valueOf(biObjId.intValue());

						Document doc = new Document();
						doc.add(new Field(IndexingConstants.UID, uid , Field.Store.YES,
								Field.Index.NOT_ANALYZED));
						//biobjid = uid
						doc.add(new Field(IndexingConstants.BIOBJ_ID, uid, Field.Store.YES,
								Field.Index.NOT_ANALYZED));
						doc.add(new Field(IndexingConstants.BIOBJ_NAME, biobjects.get(k).getName(),
								Field.Store.NO, Field.Index.ANALYZED));
						if(biobjects.get(k).getDescription() != null){
							doc.add(new Field(IndexingConstants.BIOBJ_DESCR, biobjects.get(k).getDescription(),
								Field.Store.NO, Field.Index.ANALYZED));
						}
						doc.add(new Field(IndexingConstants.BIOBJ_LABEL, biobjects.get(k).getLabel(),
								Field.Store.NO, Field.Index.ANALYZED));
						doc.add(new Field(IndexingConstants.TENANT, getTenant(biobjects.get(k)), Field.Store.YES,
								Field.Index.NOT_ANALYZED));
						
						addSubobjFieldsToDocument(doc, biobjects.get(k).getId());
						writer.addDocument(doc);
					}
				}
			}
		}catch(Exception e){
			logger.error(e.getMessage(), e);
		}finally{
			if(bais != null){
				bais.close();
			}
			logger.debug("OUT");
		}
			
	}
	private static String createUidDocument(String binId, String biobjId){
		logger.debug("IN");
		String uid=biobjId;
		if(binId != null){
			uid+= "_"+binId;
		}
		logger.debug("OUT");
		return uid;
	}
	
	private static void addFieldsToDocument(Document doc, String binId, Integer biobjId, String metaName,Domain domain, String htmlContent, byte[] content) throws UnsupportedEncodingException{
		logger.debug("IN");
		String uid = createUidDocument(binId, String.valueOf(biobjId.intValue()));
		doc.add(new Field(IndexingConstants.UID, uid , Field.Store.YES,
				Field.Index.NOT_ANALYZED));
		doc.add(new Field(IndexingConstants.BIOBJ_ID, String.valueOf(biobjId.intValue()), Field.Store.YES,
				Field.Index.NOT_ANALYZED));
		if(metaName != null){
			doc.add(new Field(IndexingConstants.METADATA, metaName,
				Field.Store.YES,
				Field.Index.NOT_ANALYZED));
		}
		if (domain.getValueCd().equalsIgnoreCase(LONG_TEXT)) { // index
																// html
																// binary
																// content
			if(htmlContent != null){
				doc.add(new Field(IndexingConstants.CONTENTS, htmlContent,
						Field.Store.NO, Field.Index.ANALYZED));
				logger.info("adding html binary content " + doc.get(IndexingConstants.UID));
				logger.info("-> " + htmlContent);
			}
		} else if (domain.getValueCd().equalsIgnoreCase(
				SHORT_TEXT)) {// index simple text binary
								// content
			if(content != null){
				doc.add(new Field(IndexingConstants.CONTENTS, new String(content, "UTF-8"),
						Field.Store.NO, Field.Index.ANALYZED));
				logger.info("adding simple text binary content " + doc.get(IndexingConstants.UID));
				logger.info("-> " + new String(content, "UTF-8"));
			}
		}
		addBiobjFieldsToDocument(doc, biobjId);
		logger.debug("OUT");
	}
	
	private static void addBiobjFieldsToDocument(Document doc, Integer biObjectID){
		logger.debug("IN");
		try {
			BIObject biObj = DAOFactory.getBIObjectDAO().loadBIObjectById(biObjectID);
			doc.add(new Field(IndexingConstants.BIOBJ_NAME, biObj.getName(),
					Field.Store.NO, Field.Index.ANALYZED));
			if(biObj.getDescription() != null){
				doc.add(new Field(IndexingConstants.BIOBJ_DESCR, biObj.getDescription(),
					Field.Store.NO, Field.Index.ANALYZED));
			}
			if(biObj.getLabel() != null){
				doc.add(new Field(IndexingConstants.BIOBJ_LABEL, biObj.getLabel(),
					Field.Store.NO, Field.Index.ANALYZED));
			}
			
		} catch (EMFUserError e) {
			logger.error(e.getMessage(), e);
		}
		logger.debug("OUT");
	}
	private static void addSubobjFieldsToDocument(Document doc, Integer biObjectID){
		logger.debug("IN");
		try {
			List<SubObject> subobjects= DAOFactory.getSubObjectDAO().getSubObjects(biObjectID);
			if(subobjects != null){
				for(int i =0; i<subobjects.size(); i++){
					SubObject subObj = subobjects.get(i);
					if(subObj.getName() != null){
						doc.add(new Field(IndexingConstants.SUBOBJ_NAME, subObj.getName(),
							Field.Store.YES, Field.Index.ANALYZED));
					}
					if(subObj.getDescription() != null){
						doc.add(new Field(IndexingConstants.SUBOBJ_DESCR, subObj.getDescription(),
							Field.Store.YES, Field.Index.ANALYZED));
					}
				}
			}
		} catch (EMFUserError e) {
			logger.error(e.getMessage(), e);
		}
		logger.debug("OUT");
	}

	
	private static String getTenant (BIObject biobj) {
		// looks in document's info
		if (biobj.getTenant() != null) {
			return biobj.getTenant();
		}
		// looks in thread
		return TenantManager.getTenant().getName();
	}
}
