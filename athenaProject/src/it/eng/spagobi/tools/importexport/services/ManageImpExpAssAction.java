/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.tools.importexport.services;

import it.eng.spago.base.RequestContainer;
import it.eng.spago.base.SourceBean;
import it.eng.spago.dispatching.action.AbstractHttpAction;
import it.eng.spagobi.commons.utilities.ChannelUtilities;
import it.eng.spagobi.commons.utilities.GeneralUtilities;
import it.eng.spagobi.commons.utilities.messages.IMessageBuilder;
import it.eng.spagobi.commons.utilities.messages.MessageBuilderFactory;
import it.eng.spagobi.commons.utilities.urls.WebUrlBuilder;
import it.eng.spagobi.tools.importexport.ImportUtilities;
import it.eng.spagobi.tools.importexport.bo.AssociationFile;
import it.eng.spagobi.tools.importexport.dao.AssociationFileDAO;
import it.eng.spagobi.tools.importexport.dao.IAssociationFileDAO;
import it.eng.spagobi.utilities.themes.ThemesManager;

import java.io.File;
import java.io.FileInputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.log4j.Logger;

public class ManageImpExpAssAction extends AbstractHttpAction {

	private HttpServletRequest httpRequest = null;
	private HttpServletResponse httpResponse = null;
	protected RequestContainer reqCont = null;
	IMessageBuilder msgBuild = null;
	WebUrlBuilder urlBuilder = null;
	private Locale locale = null;
	protected String currTheme="";

	static private Logger logger = Logger.getLogger(ManageImpExpAssAction.class);

	/* (non-Javadoc)
	 * @see it.eng.spagobi.commons.services.BaseProfileAction#service(it.eng.spago.base.SourceBean, it.eng.spago.base.SourceBean)
	 */
	public void service(SourceBean request, SourceBean response) throws Exception {
		logger.debug("IN");
		try {
			freezeHttpResponse();
			httpRequest = getHttpRequest();
			httpResponse = getHttpResponse();
			reqCont = ChannelUtilities.getRequestContainer(httpRequest);
			msgBuild = MessageBuilderFactory.getMessageBuilder();
			urlBuilder = new WebUrlBuilder();

			currTheme=ThemesManager.getCurrentTheme(reqCont);
			if(currTheme==null)currTheme=ThemesManager.getDefaultTheme();

			String language = httpRequest.getParameter("language");
			String country = httpRequest.getParameter("country");
			try {
				locale = new Locale(language, country);
			} catch (Exception e) {
				// ignore, the defualt locale will be considered
			}
			String message = (String) request.getAttribute("MESSAGE");
			if ((message != null) && (message.equalsIgnoreCase("SAVE_ASSOCIATION_FILE"))) {
				saveAssHandler();
			} else if ((message != null) && (message.equalsIgnoreCase("GET_ASSOCIATION_FILE_LIST"))) {
				getAssListHandler(request);
			} else if ((message != null) && (message.equalsIgnoreCase("DELETE_ASSOCIATION_FILE"))) {
				deleteAssHandler(request);
			} else if ((message != null) && (message.equalsIgnoreCase("UPLOAD_ASSOCIATION_FILE"))) {
				uploadAssHandler(request);
			} else if ((message != null) && (message.equalsIgnoreCase("DOWNLOAD_ASSOCIATION_FILE"))) {
				downloadAssHandler(request);
			} else if ((message!=null) && (message.equalsIgnoreCase("CHECK_IF_EXISTS"))) {
				checkIfExistsHandler(request);
			}
		} 
		catch (Throwable t) {
			logger.error("error during service method", t);
		}
		finally {
			logger.debug("OUT");
		}
	}

	private void checkIfExistsHandler(SourceBean sbrequest) {
		logger.debug("IN");
		String id = (String) sbrequest.getAttribute("ID");
		IAssociationFileDAO assfiledao = new AssociationFileDAO();
		String htmlResp = Boolean.toString(assfiledao.exists(id));
		try {
			httpResponse.getOutputStream().write(htmlResp.getBytes());
			httpResponse.getOutputStream().flush();	
		} catch (Exception e){
			logger.error("Error while sending response to the client, " + e);
		} finally {
			logger.debug("OUT");
		}
	}

	private void downloadAssHandler(SourceBean sbrequest) {
		logger.debug("IN");
		try {
			String idass = (String) sbrequest.getAttribute("ID");
			IAssociationFileDAO assfiledao = new AssociationFileDAO();
			AssociationFile assFile = assfiledao.loadFromID(idass);
			byte[] content = assfiledao.getContent(assFile);
			httpResponse.setHeader("Content-Disposition", "attachment; filename=\"associations.xml \";");
			httpResponse.setContentLength(content.length);
			httpResponse.getOutputStream().write(content);
			httpResponse.getOutputStream().flush();
		} catch (Exception e) {
			logger.error("Error while filling response with the association file, ", e);
		} finally {
			logger.debug("OUT");
		}
	}

	private void uploadAssHandler(SourceBean sbrequest) {
		logger.debug("IN");
		try {
			String modality = "MANAGE";
			String name = (String)sbrequest.getAttribute("NAME");
			if (name == null || name.trim().equals("")) {
				String msg = msgBuild.getMessage("Sbi.saving.nameNotSpecified", "component_impexp_messages", locale);
				httpResponse.getOutputStream().write(msg.getBytes());
				httpResponse.getOutputStream().flush();
				return;
			}
			String description = (String) sbrequest.getAttribute("DESCRIPTION");
			if (description == null) description = "";
//			UploadedFile uplFile = (UploadedFile)sbrequest.getAttribute("UPLOADED_FILE");
			FileItem uplFile = (FileItem)sbrequest.getAttribute("UPLOADED_FILE");
			byte[] content = null;
			if (uplFile == null || uplFile.getName().trim().equals("")) {
				String msg = msgBuild.getMessage("Sbi.saving.associationFileNotSpecified", "component_impexp_messages", locale);
				httpResponse.getOutputStream().write(msg.getBytes());
				httpResponse.getOutputStream().flush();
				return;
			} else {
				if (uplFile.getSize() == 0) {
					String msg = msgBuild.getMessage("201", "component_impexp_messages", locale);
					httpResponse.getOutputStream().write(msg.getBytes());
					httpResponse.getOutputStream().flush();
					return;
				}
				int maxSize = GeneralUtilities.getTemplateMaxSize();
				if (uplFile.getSize() > maxSize) {
					String msg = msgBuild.getMessage("202", "component_impexp_messages", locale);
					httpResponse.getOutputStream().write(msg.getBytes());
					httpResponse.getOutputStream().flush();
					return;
				}
				content = uplFile.get();
				if (!AssociationFile.isValidContent(content)) {
					String msg = msgBuild.getMessage("Sbi.saving.associationFileNotValid", "component_impexp_messages", locale);
					httpResponse.getOutputStream().write(msg.getBytes());
					httpResponse.getOutputStream().flush();
					return;
				}
			}
			String overwriteStr = (String)sbrequest.getAttribute("OVERWRITE");
			boolean overwrite = (overwriteStr == null || overwriteStr.trim().equals("")) ? false : Boolean.parseBoolean(overwriteStr);
			AssociationFile assFile = new AssociationFile();
			assFile.setDescription(description);
			assFile.setName(name);
			assFile.setDateCreation(new Date().getTime());
			assFile.setId(name);
			IAssociationFileDAO assfiledao = new AssociationFileDAO();
			if (assfiledao.exists(assFile.getId())) {
				if (overwrite) {
					assfiledao.deleteAssociationFile(assFile);
					assfiledao.saveAssociationFile(assFile, content);
				} else {
					logger.warn("Overwrite parameter is false: association file with id=[" + assFile.getId() + "] " +
							"and name=[" + assFile.getName() + "] will not be saved.");
				}
			} else {
				assfiledao.saveAssociationFile(assFile, content);
			}
			List assFiles = assfiledao.getAssociationFiles();
			String html = generateHtmlJsCss();
			html += "<br/>";
			html += generateHtmlForInsertNewForm();
			html += "<br/>";
			html += generateHtmlForList(assFiles, modality);
			httpResponse.getOutputStream().write(html.getBytes());
			httpResponse.getOutputStream().flush();	
		} catch (Exception e) {
			logger.error("Error while saving the association file, ", e);
		} finally {
			logger.debug("OUT");
		}
	}

	private void deleteAssHandler(SourceBean sbrequest) {
		logger.debug("IN");
		try {
			String modality = "MANAGE";
			String idass = (String) sbrequest.getAttribute("ID");
			IAssociationFileDAO assfiledao = new AssociationFileDAO();
			AssociationFile assFile = assfiledao.loadFromID(idass);
			assfiledao.deleteAssociationFile(assFile);
			List assFiles = assfiledao.getAssociationFiles();
			String html = generateHtmlJsCss();
			html += "<br/>";
			html += generateHtmlForInsertNewForm();
			html += "<br/>";
			html += generateHtmlForList(assFiles, modality);
			httpResponse.getOutputStream().write(html.getBytes());
			httpResponse.getOutputStream().flush();
		} catch (Exception e) {
			logger.error("Error while deleting the association file, ", e);
		} finally {
			logger.debug("OUT");
		}
	}

	private void getAssListHandler(SourceBean sbrequest) {
		logger.debug("IN");
		try {
			String modality = (String) sbrequest.getAttribute("MODALITY");
			IAssociationFileDAO assfiledao = new AssociationFileDAO();
			List assFiles = assfiledao.getAssociationFiles();
			String html="<HTML>";

			html += generateHtmlJsCss();
			if (modality.equals("MANAGE")) {
				html += "<br/>";
				html += generateHtmlForInsertNewForm();
			}
			html += "<br/>";
			html += generateHtmlForList(assFiles, modality);
			

			html+="</HTML>";
			httpResponse.getOutputStream().write(html.getBytes());
			httpResponse.getOutputStream().flush();
		} catch (Exception e) {
			logger.error("Error while getting the list of association files, ", e);
		} finally {
			logger.debug("OUT");
		}
	}

	private String generateHtmlJsCss() {
		String html = "<LINK rel='StyleSheet' type='text/css' " + "      href='"
		+ urlBuilder.getResourceLinkByTheme(httpRequest, "css/spagobi_shared.css",currTheme) + "' />";
		html += "<LINK rel='StyleSheet' type='text/css' " + "      href='"
		+ urlBuilder.getResourceLinkByTheme(httpRequest, "css/jsr168.css",currTheme) + "' />";
		html +=  "<script type=\"text/javascript\"		src=\"" 
			+ urlBuilder.getResourceLinkByTheme(httpRequest, "/js/prototype/javascripts/prototype.js",currTheme) + "\"></script>";
		return html;
	}

	private String generateHtmlForInsertNewForm() {
		String html = "<div width='100%' class='portlet-section-header'>";
		html += "			&nbsp;&nbsp;&nbsp;<a style='color:#FFFFFF;' href='javascript:openclosenewform()'>" 
			+ msgBuild.getMessage("Sbi.saving.insertNew", "component_impexp_messages", locale) 
			+ "</a>";
		html += "	   </div>";

		String action = httpRequest.getContextPath(); 
		action += "/servlet/AdapterHTTP";	
		html += "		<div id='divFormNewAss' style='display:none;'>";
		html += "			<form action='"+action+"' name='formNewAss' id='formNewAss' method='post' enctype='multipart/form-data'>  ";
		html += "				<input type='hidden' name='ACTION_NAME' value='MANAGE_IMPEXP_ASS_ACTION' >";
		html += "				<input type='hidden' name='MESSAGE' value='UPLOAD_ASSOCIATION_FILE' >";
		html += "				<input type='hidden' name='OVERWRITE' id='OVERWRITE' value='' >";


		html += "<div class='div_form_container' >\n";
		html += "	<div class='div_form_margin' >\n";
		html += "		<div class='div_form_row' >\n";
		html += "			<div class='div_form_label'>\n";
		html += "				<span class='portlet-form-field-label'>\n";
		html += "					" + msgBuild.getMessage("impexp.name", "component_impexp_messages", locale);
		html += "				</span>\n";
		html += "			</div>\n";
		html += "			<div class='div_form_field'>\n";
		html += "				<input class='portlet-form-input-field' type='text' name='NAME' \n"; 
		html += "	      	   		   id='nameNewAssToSave' />";
		html += "			</div>\n";
		html += "		</div>\n";
		html += "		<div class='div_form_row' >\n";
		html += "			<div class='div_form_label'>\n";
		html += "				<span class='portlet-form-field-label'>\n";
		html += "					" + msgBuild.getMessage("impexp.description", "component_impexp_messages", locale);
		html += "				</span>\n";
		html += "			</div>\n";
		html += "			<div class='div_form_field'>\n";
		html += "				<input class='portlet-form-input-field' type='text' name='DESCRIPTION' \n"; 
		html += "	      	   		   id='descriptionNewAssToSave' />";
		html += "			</div>\n";
		html += "		</div>\n";
		html += "		<div class='div_form_row' >\n";
		html += "			<div class='div_form_label'>\n";
		html += "				<span class='portlet-form-field-label'>\n";
		html += "					" + msgBuild.getMessage("impexp.file", "component_impexp_messages", locale);
		html += "				</span>\n";
		html += "			</div>\n";
		html += "			<div class='div_form_field'>\n";
		html += "				<input class='portlet-form-input-field' type='file' name='FILE' \n"; 
		html += "	      	   		   id='fileNewAssToSave' />";
		html += "			</div>\n";
		html += "		</div>\n";
		html += "		<div class='div_form_row' >\n";
		html += "			<div class='div_form_label'>\n";
		html += "				<span class='portlet-form-field-label'>\n";
		html += "					&nbsp;\n" ;
		html += "				</span>\n";
		html += "			</div>\n";
		html += "			<div class='div_form_field'>\n";
		html += "				<a class='link_without_dec' href=\"javascript:checkIfExists()\">\n";
		html += "					<img src= '"+urlBuilder.getResourceLinkByTheme(httpRequest, "/img/Save.gif",currTheme)+"' " +
		"                    	title='"+msgBuild.getMessage("impexp.save", "component_impexp_messages", locale)+"' " + 
		" 					 	alt='"+msgBuild.getMessage("impexp.save", "component_impexp_messages", locale)+"' />\n";
		html += "				</a>\n";	
		html += "			</div>\n";
		html += "		</div>\n";
		html += "	</div>\n";
		html += "</div>\n";
		html += "<div style='clear:left;'>&nbsp;</div>\n";

		html += "			</form>";
		html += "		</div>";

		html += "		<script>";
		html += "			function openclosenewform() {";
		html += "				divfna = document.getElementById('divFormNewAss');";
		html += "				if(divfna.style.display=='none') {";
		html += "					divfna.style.display='inline';";
		html += "				} else {";
		html += "					divfna.style.display='none';";
		html += "				}";
		html += "			}";
		html += "		</script>";

		// check if the association already exists
		html += "		<script>\n";
		html += "		function checkIfExists() {\n";
		html += "			nameass = document.getElementById('nameNewAssToSave').value;\n";
		html += "			if (nameass==''){\n";
		html += "				alert('" + msgBuild.getMessage("Sbi.saving.nameNotSpecified", "component_impexp_messages", locale) + "');\n";
		html += "				return;\n";
		html += "			}\n";
		html += "			checkAssUrl = '" + httpRequest.getContextPath() + "';\n";
		html += "			checkAssUrl += '/servlet/AdapterHTTP?';\n";
		html += "			pars = 'ACTION_NAME=MANAGE_IMPEXP_ASS_ACTION&MESSAGE=CHECK_IF_EXISTS&ID=' + document.getElementById('nameNewAssToSave').value;\n";
		html += "			new Ajax.Request(checkAssUrl,\n";
		html += "		 		{\n";
		html += "		    		method: 'post',\n";
		html += "		    		parameters: pars,\n";
		html += "		    		onSuccess: function(transport){\n";
		html += "		            	        	response = transport.responseText || \"\";\n";
		html += "		                	    	saveAss(response);\n";
		html += "		                	   },\n";
		html += "		    		onFailure: somethingWentWrongSaveAss,\n";
		html += "		    		asynchronous: false\n";
		html += "		 		 }\n";
		html += "			 );\n";
		html += "		}\n";
		html += "		function somethingWentWrongSaveAss() {\n";
		html += "		}\n";
		html += "		</script>\n";

		// save the association
		html += "		<script>\n";
		html += "		function saveAss(exists) {\n";
		html += "			if (exists != 'true' || confirm('" + msgBuild.getMessage("Sbi.saving.alreadyExisting", "component_impexp_messages", locale) + "')) {\n";
		html += "				document.getElementById('OVERWRITE').value = 'true';\n";
		html += "				document.getElementById('formNewAss').submit();\n";
		html += "			}\n";
		html += "		}\n";
		html += "		</script>\n";
		return html;
	}

	private String generateHtmlForList(List assFiles, String modality) {
		String html = "<table widht='100%'>";
		html += "<tr>";
		html += "<td class='portlet-section-header'>"+msgBuild.getMessage("impexp.name", "component_impexp_messages", locale)+"</td>";
		html += "<td class='portlet-section-header'>"+msgBuild.getMessage("impexp.description", "component_impexp_messages", locale)+"</td>";
		html += "<td class='portlet-section-header'>"+msgBuild.getMessage("impexp.creationDate", "component_impexp_messages", locale)+"</td>";
		html += "<td class='portlet-section-header'>&nbsp;</td>";
		html += "</tr>";
		String rowClass = "";
		boolean alternate = false;
		Iterator iterAssFile = assFiles.iterator();
		while(iterAssFile.hasNext()) {
			rowClass = (alternate) ? "portlet-section-alternate" : "portlet-section-body";
			alternate = !alternate;
			AssociationFile assFile = (AssociationFile)iterAssFile.next();
			html += "<tr>";
			html += "<td class='"+rowClass+"'>"+assFile.getName()+"</td>";
			html += "<td class='"+rowClass+"'>"+assFile.getDescription()+"</td>";
			Date dat = new Date(assFile.getDateCreation());
			Calendar cal = new GregorianCalendar();
			cal.setTime(dat);
			String datSt = "" +  cal.get(Calendar.DAY_OF_MONTH) + "/" +
			(cal.get(Calendar.MONTH) + 1) + "/" +
			cal.get(Calendar.YEAR) +  "   " +
			cal.get(Calendar.HOUR_OF_DAY) + ":" +
			(cal.get(Calendar.MINUTE) < 10 ? "0" : "") + 
			cal.get(Calendar.MINUTE);
			html += "<td class='"+rowClass+"'>"+datSt+"</td>";
			if(modality.equals("MANAGE")) {
				String eraseUrl = httpRequest.getContextPath(); ;
				eraseUrl += "/servlet/AdapterHTTP?ACTION_NAME=MANAGE_IMPEXP_ASS_ACTION";	
				eraseUrl += "&MESSAGE=DELETE_ASSOCIATION_FILE&ID="+assFile.getId();	
				String downloadUrl = httpRequest.getContextPath(); ;
				downloadUrl += "/servlet/AdapterHTTP?ACTION_NAME=MANAGE_IMPEXP_ASS_ACTION";	
				downloadUrl += "&MESSAGE=DOWNLOAD_ASSOCIATION_FILE&ID="+assFile.getId();	
				html += "<td class='"+rowClass+"'>\n";
				html += "<a class='link_without_dec' href='"+eraseUrl+"' style='text-decoration:none;'>\n";		
				html += "<img src='"+urlBuilder.getResourceLinkByTheme(httpRequest, "/img/erase.gif",currTheme)+"' \n" + 
				"title='"+msgBuild.getMessage("impexp.erase", "component_impexp_messages", locale)+"' \n" + 
				"alt='"+msgBuild.getMessage("impexp.erase", "component_impexp_messages", locale)+"' />\n";
				html += "</a>\n";		
				html += "&nbsp;&nbsp;\n";
				html += "<a class='link_without_dec' href='"+downloadUrl+"' style='text-decoration:none;'>\n";
				html += "<img src='"+urlBuilder.getResourceLinkByTheme(httpRequest, "/img/down16.gif",currTheme)+"' \n" + 
				"title='"+msgBuild.getMessage("Sbi.download", "component_impexp_messages", locale)+"' \n" + 
				"alt='"+msgBuild.getMessage("Sbi.download", "component_impexp_messages", locale)+"' />\n";
				html += "</a>\n";		
				html += "</td>";
			} else if(modality.equals("SELECT") ) {
				html += "<td class='"+rowClass+"'>\n";
				html += "<a class='link_without_dec' href=\"javascript:parent.selectAssFile('"+assFile.getId()+"', '"+assFile.getName()+"')\">\n";
				html += "<img src='"+urlBuilder.getResourceLinkByTheme(httpRequest, "/img/button_ok.gif",currTheme)+"' \n" + 
				"title='"+msgBuild.getMessage("impexp.select", "component_impexp_messages", locale)+"' \n" + 
				"alt='"+msgBuild.getMessage("mpexp.select", "component_impexp_messages", locale)+"' />\n";
				html += "</a>\n";
			}
			html += "</tr>";
		}
		html += "</table>";
		return html;
	}

	private void saveAssHandler() {
		logger.debug("IN");
		String htmlResp = "";
		try {
			String associationFileName = httpRequest.getParameter("FILE_NAME");
			String folderName = httpRequest.getParameter("FOLDER_NAME");
			String name = httpRequest.getParameter("NAME");
			String description = httpRequest.getParameter("DESCRIPTION");
			String overwriteStr = httpRequest.getParameter("OVERWRITE");
			boolean overwrite = (overwriteStr == null || overwriteStr.trim().equals("")) ? false : Boolean.parseBoolean(overwriteStr);
			AssociationFile assFile = new AssociationFile();
			assFile.setDescription(description);
			assFile.setName(name);
			assFile.setDateCreation(new Date().getTime());
			assFile.setId(name);
			String pathExportFolder = ImportUtilities.getImportTempFolderPath();
			File file = new File(pathExportFolder + "/" + folderName + "/" + associationFileName + ".xml");
			FileInputStream fis = new FileInputStream(file);
			byte[] fileAssContent = GeneralUtilities.getByteArrayFromInputStream(fis);
			fis.close();
			IAssociationFileDAO assfiledao = new AssociationFileDAO();
			if (assfiledao.exists(assFile.getId())) {
				if (overwrite) {
					assfiledao.deleteAssociationFile(assFile);
					assfiledao.saveAssociationFile(assFile, fileAssContent);
				} else {
					logger.warn("Overwrite parameter is false: association file with id=[" + assFile.getId() + "] " +
							"and name=[" + assFile.getName() + "] will not be saved.");
				}
			} else {
				assfiledao.saveAssociationFile(assFile, fileAssContent);
			}
			htmlResp = msgBuild.getMessage("Sbi.saved.ok", "component_impexp_messages", locale);
		} catch (Exception e) {
			logger.error("Error wile saving the association file, ", e);
			htmlResp = msgBuild.getMessage("Sbi.saved.ko", "component_impexp_messages", locale);
		} finally {
			try {
				httpResponse.getOutputStream().write(htmlResp.getBytes());
				httpResponse.getOutputStream().flush();
			} catch (Exception e) {
				logger.error("Error while sending response to the client, ", e);
			}
			logger.debug("OUT");
		}
	}


}
