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
package it.eng.spagobi.events.bo;

import it.eng.spago.util.StringUtils;
import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.analiticalmodel.document.dao.IBIObjectDAO;
import it.eng.spagobi.analiticalmodel.document.dao.SubreportDAOHibImpl;
import it.eng.spagobi.commons.SingletonConfig;
import it.eng.spagobi.commons.bo.Subreport;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.utilities.GeneralUtilities;
import it.eng.spagobi.events.dao.EventLogDAOHibImpl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * This class map the SBI_EVENTS_LOG table
 * 
 * @author Gioia
 *
 */
public class EventLog implements Serializable {
	private Integer id;
	private String user;
	private Date date;
	private String desc;
	private String params;
	private String handler;
	private List roles;

	
	static private Logger logger = Logger.getLogger(EventLog.class);
	
	/**
	 * Gets the date.
	 * 
	 * @return the date
	 */
	public Date getDate() {
		return date;
	}

	/**
	 * Sets the date.
	 * 
	 * @param date the new date
	 */
	public void setDate(Date date) {
		this.date = date;
	}

	/**
	 * Gets the desc.
	 * 
	 * @return the desc
	 */
	public String getDesc() {
		return desc;
	}

	/**
	 * Sets the desc.
	 * 
	 * @param desc the new desc
	 */
	public void setDesc(String desc) {
		this.desc = desc;
	}

	/**
	 * Gets the id.
	 * 
	 * @return the id
	 */
	public Integer getId() {
		return id;
	}

	/**
	 * Sets the id.
	 * 
	 * @param id the new id
	 */
	public void setId(Integer id) {
		this.id = id;
	}

	/**
	 * Gets the params.
	 * 
	 * @return the params
	 */
	public String getParams() {
		return params;
	}

	/**
	 * Sets the params.
	 * 
	 * @param params the new params
	 */
	public void setParams(String params) {
		this.params = params;
	}

	/**
	 * Gets the user.
	 * 
	 * @return the user
	 */
	public String getUser() {
		return user;
	}

	/**
	 * Sets the user.
	 * 
	 * @param user the new user
	 */
	public void setUser(String user) {
		this.user = user;
	}	

	/**
	 * Gets the roles.
	 * 
	 * @return the roles
	 */
	public List getRoles() {
		return roles;
	}

	/**
	 * Sets the roles.
	 * 
	 * @param roles the new roles
	 */
	public void setRoles(List roles) {
		this.roles = roles;
	}

	/**
	 * Gets the handler.
	 * 
	 * @return the handler
	 */
	public String getHandler() {
		return handler;
	}

	/**
	 * Sets the handler.
	 * 
	 * @param handler the new handler
	 */
	public void setHandler(String handler) {
		this.handler = handler;
	}

	public String getFormattedDescription(){
		String description = getDesc();
		if (description != null) {
			description = GeneralUtilities.replaceInternationalizedMessages(description);
			description = description.replaceAll("<br/>", " ");
			if (description.length() > 50) description = description.substring(0, 50) + "...";
			description = description.replaceAll(">", "&gt;");
			description = description.replaceAll("<", "&lt;");
			description = description.replaceAll("\"", "&quot;");
		}
		return description;
	}

	public String getFormattedDate(){
		SingletonConfig config = SingletonConfig.getInstance();
		String formatSB = null;
		if(config!=null){
			formatSB = config.getConfigValue("SPAGOBI.DATE-FORMAT.format");
		}
		String format = (formatSB==null)?"": formatSB;
		format = format.replaceAll("D", "d");
		format = format.replaceAll("m", "M");
		format = format.replaceAll("Y", "y");

		return StringUtils.dateToString(getDate(), format);
	}
	
	@JsonIgnore
	public JSONObject getParametesrMap(){
		if(getParams()!=null){
			try {
				return new JSONObject(getParams());
			} catch (JSONException e) {
				logger.error(e);
			}
		}
		return new JSONObject();
	}
	
	public Map<String,Object> getAdditionalInformation(){
		Map<String,Object> map = new HashMap<String, Object>();
		try {
			IBIObjectDAO biObjectDAO = DAOFactory.getBIObjectDAO();
			String biobjectIdStr = (String)  getParametesrMap().get("biobjectId");
			Integer biObjectId = new Integer(biobjectIdStr);
			BIObject biObject = biObjectDAO.loadBIObjectById(biObjectId);
			
			map.put("name", biObject.getName());
			map.put("label", biObject.getLabel());
			map.put("description", biObject.getDescription());
			map.put("engine", biObject.getEngineLabel());
			map.put("subobject", biObject.getEngineLabel());
			
			List<Map<String,String>> subobjects = new ArrayList<Map<String,String>>();
			
			SubreportDAOHibImpl subreportDAOHibImpl = new SubreportDAOHibImpl();
			List list = subreportDAOHibImpl.loadSubreportsByMasterRptId(biObject.getId());
			for(int i = 0; i < list.size(); i++) {
				Subreport subreport = (Subreport)list.get(i);
				BIObject biobj = biObjectDAO.loadBIObjectForDetail(subreport.getSub_rpt_id());
				Map<String,String> subobject = new HashMap<String, String>();
				subobject.put("label",biobj.getLabel());
				subobject.put("name",biobj.getName());
				subobjects.add(subobject);
			}
			if(subobjects.size()>0){
				map.put("subobjects", subobjects);
			}
			
			
			/*
			Map eventParams = EventsManager.parseParamsStr(getParams());
			String startEventId = (String) eventParams.get("startEventId");
			if (startEventId != null) {
				// it's an end process event
				response.setAttribute("startEventId", startEventId);
				String result = (String) eventParams.get("operation-result");
				response.setAttribute("operation-result", result);
			} else {
				// it's an end process event, nothing more to do
			}*/
			
			
		} catch (Exception e) {
			logger.error(e);
		}
		return map;

	}
	
	public String getType(){
		String handler = getHandler();
		
		if(handler.contains("Talend")){
			return "Talend";
		}
		if(handler.contains("Common")){
			return "Commonj";
		}
		return "Generic";
	}

	
}
