/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.tools.massiveExport.services;

import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.analiticalmodel.functionalitytree.bo.LowFunctionality;
import it.eng.spagobi.analiticalmodel.functionalitytree.dao.ILowFunctionalityDAO;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.BIObjectParameter;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.services.AbstractSpagoBIAction;
import it.eng.spagobi.commons.utilities.GeneralUtilities;
import it.eng.spagobi.commons.utilities.StringUtilities;
import it.eng.spagobi.tools.massiveExport.utils.Utilities;
import it.eng.spagobi.tools.scheduler.bo.CronExpression;
import it.eng.spagobi.tools.scheduler.bo.Job;
import it.eng.spagobi.tools.scheduler.bo.Trigger;
import it.eng.spagobi.tools.scheduler.dao.ISchedulerDAO;
import it.eng.spagobi.tools.scheduler.jobs.ExecuteBIDocumentJob;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;

import java.io.File;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

public class StartMassiveScheduleAction extends AbstractSpagoBIAction {

	private static final long serialVersionUID = 1L;

	private final String SERVICE_NAME = "START_MASSIVE_SCHEDULE_ACTION";
	
	public final String ANALYTICAL_DRIVER_VALUES_SEPARATOR = ";";

	// Objects recieved
	private final String PARAMETERS_PAGE = "Sbi.browser.mexport.MassiveExportWizardParametersPage";
	private final String OPTIONS_PAGE = "Sbi.browser.mexport.MassiveExportWizardOptionsPage";
	private final String TRIGGER_PAGE = "Sbi.browser.mexport.MassiveExportWizardTriggerPage";


	private final String FUNCTIONALITY_ID = "functId";
	private final String ROLE = "selectedRole";
	private final String MIME_TYPE = "mimeType";
	private final String TYPE = "type";  
	private final String SPLITTING_FILTER = "splittingFilter"; 


	// logger component
	private static Logger logger = Logger.getLogger(StartMassiveScheduleAction.class); 

	@Override
	public void doService() {

		ISchedulerDAO schedulerDAO;
		Trigger trigger = null;
		Job job = null;
		Integer folderId = null;
		String documentType = null;
		String role = null; 
		String outputMIMEType = null;
		boolean splittingFilter = false;
		JSONObject optionsPageContentJSON = null;
		JSONObject parametersPageContentJSON = null;
		JSONObject triggerPageContentJSON = null;
		boolean triggerSuccesfullySaved = false;
		boolean jobSuccesfullySaved = false;
		
		logger.debug("IN");

		schedulerDAO = null;
		try{
		
			
			try{
				folderId = this.getAttributeAsInteger(FUNCTIONALITY_ID);
				logger.debug("Input parameter [" + FUNCTIONALITY_ID + "] is equal to [" + folderId + "]");
				Assert.assertNotNull(folderId, "Input parameter [" + FUNCTIONALITY_ID + "] cannot be null");

				documentType = this.getAttributeAsString(TYPE);
				logger.debug("Input parameter [" + TYPE + "] is equal to [" + documentType + "]");

				optionsPageContentJSON = this.getAttributeAsJSONObject(OPTIONS_PAGE);
				logger.debug("Input parameter [" + OPTIONS_PAGE + "] is equal to [" + optionsPageContentJSON + "]");
				Assert.assertNotNull(optionsPageContentJSON, "Input parameter [" + OPTIONS_PAGE + "] cannot be null");

				role = optionsPageContentJSON.getString(ROLE);
				logger.debug("Input parameter [" + ROLE + "] is equal to [" + role + "]");
				Assert.assertNotNull(role, "Input parameter [" + ROLE + "] cannot be null");

				outputMIMEType = optionsPageContentJSON.getString(MIME_TYPE);
				logger.debug("Input parameter [" + MIME_TYPE + "] is equal to [" + outputMIMEType + "]");
				Assert.assertNotNull(outputMIMEType, "Input parameter [" + MIME_TYPE + "] cannot be null");

				splittingFilter = optionsPageContentJSON.getBoolean(SPLITTING_FILTER);
				logger.debug("Input parameter [" + SPLITTING_FILTER + "] is equal to [" + splittingFilter + "]");

				parametersPageContentJSON = this.getAttributeAsJSONObject(PARAMETERS_PAGE);
				logger.debug("Input parameter [" + PARAMETERS_PAGE + "] is equal to [" + parametersPageContentJSON + "]");
				Assert.assertNotNull(parametersPageContentJSON, "Input parameter [" + PARAMETERS_PAGE + "] cannot be null");
				
				triggerPageContentJSON = this.getAttributeAsJSONObject(TRIGGER_PAGE);
				logger.debug("Input parameter [" + OPTIONS_PAGE + "] is equal to [" + triggerPageContentJSON + "]");
				Assert.assertNotNull(triggerPageContentJSON, "Input parameter [" + OPTIONS_PAGE + "] cannot be null");

			} catch (Throwable t) {
				throw new SpagoBIServiceException(SERVICE_NAME, "Error in retrieving parameters: ", t);
			} 

			
			
			LowFunctionality folder = getFolder(folderId);
			logger.debug("Target folder is [" + folder.getName() + "]");
			List<BIObject> documentsToExport = getDocumentsToExport(folder, documentType);
			logger.debug("Target folder [" + folder.getName() + "] contains [" + documentsToExport.size() + "] document(s) of type [" + documentType + "] to export");
			
			JSONObject generalConfJSON = triggerPageContentJSON.getJSONObject("generalConf");
			
			// create the job
			JSONObject jobConfJSON = generalConfJSON.getJSONObject("job");
			try {
					
				jobConfJSON.put("name", getName(getUserProfile(), folder));
				jobConfJSON.put("description", getDescription(getUserProfile(), folder));
				jobConfJSON.put("groupName", getGroupName(getUserProfile(), folder));
				job = createJob(jobConfJSON, documentsToExport, parametersPageContentJSON);
				
				job.addParameters( createDistpachChannelParameters(documentsToExport, getUserProfile(), folder) );				
				job.addParameter("modality", SpagoBIConstants.MASSIVE_EXPORT_MODALITY);
				job.addParameter("outputMIMEType", outputMIMEType);
				job.addParameter("isSplittingFilter", splittingFilter? "true": "false");
				
				
				
				Assert.assertNotNull(job, "Impossible to create job [" + jobConfJSON + "]");
				logger.debug("Job [" + job + "] succesfully created");
			} catch (SpagoBIServiceException t) {
				throw (t);		
			} catch (Throwable t) {
				throw new SpagoBIServiceException(SERVICE_NAME, "An unexpected error occurred while creating job [" + jobConfJSON + "]", t);
			} 
			
			
			// create the trigger
			JSONObject triggerConfJSON = generalConfJSON.getJSONObject("trigger");
			try {
				triggerConfJSON.put("name", getName(getUserProfile(), folder));
				triggerConfJSON.put("description", getDescription(getUserProfile(), folder));
				triggerConfJSON.put("groupName", getGroupName(getUserProfile(), folder));
				
				trigger = createTrigger(triggerConfJSON);
				JSONObject cronConfJSON = triggerPageContentJSON.getJSONObject("cronConf");
				CronExpression cronExpression = getChronExpression(cronConfJSON);
				trigger.setCronExpression(cronExpression);			
				trigger.setJob(job);
				
				Assert.assertNotNull(job, "Impossible to create trigger [" + triggerConfJSON + "]");
				
				logger.debug("Trigger [" + trigger + "] succesfully created");
			} catch (SpagoBIServiceException t) {
				throw (t);		
			} catch (Throwable t) {
				throw new SpagoBIServiceException(SERVICE_NAME, "An unexpected error occurred while creating trigger [" + triggerConfJSON + "]", t);
			} 
			
			// save job and trigger
			schedulerDAO = DAOFactory.getSchedulerDAO();
		
			// the job first
			try {			
				schedulerDAO.insertJob(job);
				jobSuccesfullySaved = true;
				logger.debug("Job [" + job + "] succesfully saved");
			} catch (SpagoBIServiceException t) {
				throw (t);		
			} catch (Throwable t) {
				throw new SpagoBIServiceException(SERVICE_NAME, "Impossible to save job [" + job + "]", t);
			} 
			// the trigger then
			try {
				schedulerDAO.saveTrigger(trigger);
				triggerSuccesfullySaved = true;
				logger.debug("Trigger [" + trigger + "] succesfully saved");
			} catch (SpagoBIServiceException t) {
				throw (t);		
			} catch (Throwable t) {
				throw new SpagoBIServiceException(SERVICE_NAME, "Impossible to save trigger [" + job + "]", t);
			} 
			
		} catch (SpagoBIServiceException t) {
			throw (t);		
		} catch (Throwable t) {
			throw new SpagoBIServiceException(SERVICE_NAME, "An unexpected error occuerd while executing service ["+ SERVICE_NAME + "]", t);
		} finally {
			if(jobSuccesfullySaved && !triggerSuccesfullySaved) {
				logger.debug("Rolback operation is required");
				schedulerDAO.deleteJob(job.getName(), job.getGroupName());
				logger.debug("Job [" + job + " has been deleted]");
				logger.debug("Rolback operation executed succesfully");
			}
			logger.debug("OUT");
		}
	}
	
	// we use the same group name for job and trigger
	private String getGroupName(IEngUserProfile userProfile, LowFunctionality folder) {
		String name = "private/users" + "/" + userProfile.getUserUniqueIdentifier() + "/massive/" + folder.getName();
		return name;
	}
	
	// we use the same name for job and trigger
	private String getName(IEngUserProfile userProfile, LowFunctionality folder) {
		String name = userProfile.getUserUniqueIdentifier() + "@" + folder.getCode();
		return name;
	}
	
	// we use the same name for job and trigger
	private String getDescription(IEngUserProfile userProfile, LowFunctionality folder) {
		String description = "Massive scheduling defined by user [" + userProfile.getUserUniqueIdentifier() + "] on folder [" + folder.getName() + "]";
		return description;
	}
	
	private LowFunctionality getFolder(Integer folderId) {
		LowFunctionality folder;
		
		logger.debug("OUT");
		
		folder = null;
		try {
			ILowFunctionalityDAO functionalityTreeDao = DAOFactory.getLowFunctionalityDAO();
			folder = functionalityTreeDao.loadLowFunctionalityByID(folderId, true);
			Assert.assertNotNull(folder, "Folder [" + folderId + "] cannot be loaded");
		} catch(Throwable t) {
			throw new SpagoBIServiceException(SERVICE_NAME, "An unexpected error occurred while loading folder ["+ folderId + "]", t);
		} finally {
			logger.debug("OUT");
		}
		
		return folder;
	}
	
	private List<BIObject> getDocumentsToExport(LowFunctionality folder, String documentType) {
		List<BIObject> documentsToExport = null;
		documentsToExport = Utilities.getContainedObjFilteredbyType(folder, documentType);
		return documentsToExport;
	}
	
	private Job createJob(JSONObject jobConfJSON, List<BIObject> documentsToExport, JSONObject documentsParameterValuesJSON) {
		Job job;
		
		logger.debug("IN");
		
		job = null;
		try {
			job = new Job();
			job.setName( jobConfJSON.getString("name") );
			job.setDescription( jobConfJSON.optString("description") );
			job.setGroupName( jobConfJSON.getString("groupName") );
			job.setRequestsRecovery(false);
			job.setJobClass( ExecuteBIDocumentJob.class );
			
			Map<String, String> parameters = createJobParameters(documentsToExport, documentsParameterValuesJSON);			
			job.addParameters(parameters);
			
		} catch(Throwable t) {
			throw new SpagoBIServiceException(SERVICE_NAME, "An unexpected error occuerd while creating job", t);
		} finally {
			logger.debug("OUT");
		}
		
		return job;
	}

	private Map<String, String> createJobParameters(List<BIObject> documentsToExport, JSONObject documentsParameterValuesJSON) {
		Map<String, String> parameters;
		
		logger.debug("IN");
		
		parameters = new HashMap<String,String>();
		try {
			// documentLabel__num this is necessary because the same document can be added to one scheduled activity more than one time
			int docNo = 1;
			for(BIObject document : documentsToExport) {
				String pName = document.getLabel() + "__" + docNo++;
				String pValue = "";
				String separator = "";
				List<BIObjectParameter> documentParameters = document.getBiObjectParameters();
				for(BIObjectParameter documentParameter : documentParameters) {
					String documentParameterUrl = documentParameter.getParameterUrlName();
					String value = null;
					// descriptions are already concatenated with ANALYTICAL_DRIVER_VALUES_SEPARATOR (i.e. ";")
					String descriptions = documentsParameterValuesJSON.getString(documentParameterUrl + "_field_visible_description");
					Object valueObj = documentsParameterValuesJSON.get(documentParameterUrl);
					if (valueObj instanceof JSONArray) {
						JSONArray array = (JSONArray) valueObj;
						StringBuffer buffer = new StringBuffer(); 
						for (int i = 0 ; i < array.length() ; i++) {
							buffer.append(array.getString(i));
							if ( i < array.length() - 1 ) {
								buffer.append(ANALYTICAL_DRIVER_VALUES_SEPARATOR);
							}
						}
						value = buffer.toString();
					} else {
						value = valueObj.toString();
					}
					//String value = documentsParameterValuesJSON.getString(documentParameterUrl);
					pValue += separator + documentParameterUrl + "="+ value;
					separator = "%26";
					pValue += separator + documentParameterUrl + "_field_visible_description=" + descriptions;
				}
				parameters.put(pName, pValue);
			}
			
			String value = createDocumentLabelsParameterValue(documentsToExport);
			parameters.put("documentLabels", value);
		
		} catch(Throwable t) {
			throw new SpagoBIServiceException(SERVICE_NAME, "An unexpected error occuerd while creating job's parameters", t);
		} finally {
			logger.debug("OUT");
		}
		
		return parameters;
	}
	
	private String createDocumentLabelsParameterValue(List<BIObject> documentsToExport) {
		String value = "";
		
		String separetor = "";
		int docNo = 1;
		for(BIObject document : documentsToExport) {
			value += separetor + document.getLabel() + "__" + docNo++;
			separetor = ",";				
		}
		return value;
	}
	
	private Map<String, String> createDistpachChannelParameters(List<BIObject> documentsToExport, IEngUserProfile userProfile, LowFunctionality folder) {
		Map<String, String> parameters;
		String name;
		String value;
		
		parameters = new HashMap<String, String>();

		File destinationFolder = Utilities.getMassiveScheduleZipFolder(
				(String)userProfile.getUserUniqueIdentifier(), folder.getCode());
		
		
		name = "globalDispatcherContext";
		value = "saveasfile=true"
			+ "%26" + "destinationfolder=" + destinationFolder.getAbsolutePath()
			+ "%26" + "isrelativetoresourcefolder=false" 
			+ "%26" + "functionalitytreefolderlabel=" + folder.getCode()
			+ "%26" + "owner=" + (String)userProfile.getUserUniqueIdentifier();
	

		parameters.put(name, value);
		
		return parameters;   	   
	}

	private Date getTime(String dateStr, String timeStr) throws ParseException {
		Calendar calendar;
	
		calendar = null;
		
		if(StringUtilities.isNotEmpty(dateStr)) {
			DateFormat dataFormat = new SimpleDateFormat( GeneralUtilities.getServerDateFormat());
			Date date = dataFormat.parse(dateStr);
			calendar = new GregorianCalendar();
			calendar.setTime(date);
			
			if(StringUtilities.isNotEmpty(timeStr)) {
				DateFormat timeFormat = new SimpleDateFormat("hh:mm a");
				Date time = timeFormat.parse(timeStr);
				Calendar timeCalendar = new GregorianCalendar();
				timeCalendar.setTime(time);
				calendar.set(calendar.HOUR, timeCalendar.get(calendar.HOUR));
				calendar.set(calendar.MINUTE, timeCalendar.get(calendar.MINUTE));
				calendar.set(calendar.AM_PM, timeCalendar.get(calendar.AM_PM));
			}
		}
		
		return calendar != null? calendar.getTime(): null;
	}

	private Trigger createTrigger(JSONObject triggerConfJSON) {
		Trigger trigger;
		
		logger.debug("IN");
		
		trigger = null;
		try {
			trigger = new Trigger();
			
			trigger.setName( triggerConfJSON.getString("name") );
			trigger.setDescription( triggerConfJSON.optString("description") );
			
			String startDateStr = triggerConfJSON.optString("startDate");
			if( StringUtilities.isEmpty(startDateStr) ) {
				throw new SpagoBIServiceException(SERVICE_NAME, "Start date cannot be empty");
			}
			String startTimeStr = triggerConfJSON.optString("startTime");
			Date startTime = getTime(startDateStr, startTimeStr);
			trigger.setStartTime(startTime);
			
			String endDateStr = triggerConfJSON.optString("endDate");
			if( StringUtilities.isEmpty(endDateStr) ) {
				String endTimeStr = triggerConfJSON.optString("endTime");
				Date endTime = getTime(endDateStr, endTimeStr);
				trigger.setEndTime(endTime);	
			}
		} catch(SpagoBIServiceException t) {
			throw t;
		} catch(Throwable t) {
			throw new SpagoBIServiceException(SERVICE_NAME, "An unexpected error occuerd while creating trigger", t);
		} finally {
			logger.debug("OUT");
		}
		
		return trigger;
	}
	
	private CronExpression getChronExpression(JSONObject cronConfJSON) {
       
		StringBuffer expression;
		JSONObject oneshotOptionsJSON;
		JSONObject minutesOptionsJSON;
		JSONObject hourlyOptionsJSON;
		
		JSONObject dailyOptionsJSON;
		JSONObject weeklyOptionsJSON;
		JSONObject monthlyOptionsJSON;
        
     
		logger.debug("IN");
		try {
			oneshotOptionsJSON = cronConfJSON.optJSONObject("oneshot");
	        minutesOptionsJSON = cronConfJSON.optJSONObject("minutes");
	        hourlyOptionsJSON = cronConfJSON.optJSONObject("hourly");
	        dailyOptionsJSON = cronConfJSON.optJSONObject("daily");
	        weeklyOptionsJSON = cronConfJSON.optJSONObject("weekly");
	        monthlyOptionsJSON = cronConfJSON.optJSONObject("monthly");
	       
	        expression = new StringBuffer();
	        
	        if(oneshotOptionsJSON != null) {
	        	String enabled = oneshotOptionsJSON.optString("enabled");
	    		if( StringUtilities.isNotEmpty(enabled) && "TRUE".equalsIgnoreCase(enabled)) {
	    			expression.append("single{}");
	    		}
	    	}
	        
	    	if(minutesOptionsJSON != null) {
	    		String minutes = minutesOptionsJSON.optString("minutes");
	    		if( StringUtilities.isNotEmpty(minutes) ) {
		    		expression.append("minute{");    		
		    		expression.append("numRepetition=");
		    		expression.append(minutes);
		    		expression.append("}");
	    		}
	    	}
	    	
	    	if(hourlyOptionsJSON != null) {
	    		String houres = hourlyOptionsJSON.optString("houres");
	    		if( StringUtilities.isNotEmpty(houres)) {
		    		expression.append("hour{");
		    		expression.append("numRepetition=");
		    		expression.append(houres);
		    		expression.append("}");
	    		}
	    	}
	    	
	    	if(dailyOptionsJSON != null) {
	    		String days = dailyOptionsJSON.optString("days");
	    		if( StringUtilities.isNotEmpty(days)) {
		    		expression.append("day{");
		    		expression.append("numRepetition=");
		    		expression.append(days);
		    		expression.append("}");
	    		}
	    	}
	    	
	    	// week{numRepetition=1;days=SUN,MON,TUE,WED,THU,FRI,SAT,}
	    	if(weeklyOptionsJSON != null) {
	    		JSONArray inDays = weeklyOptionsJSON.optJSONArray("inDays");
	    		if( inDays != null && inDays.length() > 0) {
		    		expression.append("week{");
		    		expression.append("numRepetition=1;");
		    		expression.append("days=");
		    		for(int i = 0; i < inDays.length(); i++) {
		    			String separator = ((i+1) == inDays.length())? "": ",";
		    			expression.append(inDays.getString(i) + separator);
		    		}
		    		expression.append("}");
	    		}
	    	}
	    	
	    	//month{numRepetition=1;months=NONE;dayRepetition=10;weeks=NONE;days=NONE;}
	    	if(monthlyOptionsJSON != null) {
	    		String inDay = monthlyOptionsJSON.optString("inDay");
	    		if( StringUtilities.isNotEmpty(inDay)) {
		    		expression.append("month{");
		    		expression.append("numRepetition=1;");
		    		expression.append("months=NONE;");
		    		expression.append("dayRepetition=");
		    		expression.append(inDay  + ";");
		    		expression.append("weeks=NONE;");
		    		expression.append("days=NONE;");
		    		expression.append("}");
	    		}
	    	}
	    	


		} catch(Throwable t) {
			throw new SpagoBIRuntimeException("An unexpected error occuerd while creating cron expression", t);
		} finally {
			logger.debug("OUT");
		}
    	
    	return new CronExpression(expression.toString());
    }
}
