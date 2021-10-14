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
package it.eng.knowage.engine.cockpit.api.engine;

import static it.eng.spagobi.commons.constants.SpagoBIConstants.CREATE_CUSTOM_CHART;
import static it.eng.spagobi.commons.constants.SpagoBIConstants.DISCOVERY_WIDGET_USE;
import static it.eng.spagobi.commons.constants.SpagoBIConstants.DOCUMENT_WIDGET_USE;
import static it.eng.spagobi.commons.constants.SpagoBIConstants.EDIT_PYTHON_SCRIPTS;
import static it.eng.spagobi.commons.constants.SpagoBIConstants.MAP_WIDGET_USE;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import it.eng.knowage.engine.cockpit.CockpitEngine;
import it.eng.knowage.engine.cockpit.api.AbstractCockpitEngineResource;
import it.eng.spago.error.EMFInternalError;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.services.rest.annotations.ManageAuthorization;
import it.eng.spagobi.user.UserProfileManager;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 *
 */
@ManageAuthorization
@Path("/1.0/engine")
public class EngineResource extends AbstractCockpitEngineResource {

	static private Logger logger = Logger.getLogger(EngineResource.class);

	@GET
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public String getEngine(@Context HttpServletRequest req) {

		logger.debug("IN");
		try {
			return serializeEngine();
		} catch (Exception e) {
			throw new SpagoBIServiceException(req.getPathInfo(), "An unexpected error occured while executing service", e);
		} finally {
			logger.debug("OUT");
		}
	}

	// =======================================================================
	// SERIALIZATION METHODS
	// =======================================================================

	private String serializeEngine() {
		try {
			JSONObject resultJSON = new JSONObject();
			resultJSON.put("enabled", CockpitEngine.isEnabled());
			resultJSON.put("creationDate", CockpitEngine.getCreationDate());
			long uptime = System.currentTimeMillis() - CockpitEngine.getCreationDate().getTime();
			long days = uptime / 86400000;
			long remainder = uptime % 86400000;
			long hours = remainder / 3600000;
			remainder = remainder % 3600000;
			long minutes = remainder / 60000;
			remainder = remainder % 60000;
			long seconds = remainder / 1000;
			resultJSON.put("uptime", days + "d " + hours + "h " + minutes + "m " + seconds + "s");
			return resultJSON.toString();
		} catch (Exception e) {
			throw new RuntimeException("An unexpected error occured while serializing results", e);
		}
	}

	private JSONArray filterFolders(JSONArray foldersJSON) throws JSONException {
		JSONArray toReturn = new JSONArray();
		Set<Integer> folderIds = new HashSet<Integer>();
		for (int i = 0; i < foldersJSON.length(); i++) {
			int id = foldersJSON.getInt(i);
			Integer folderId = new Integer(id);
			if (!folderIds.contains(folderId)) {
				toReturn.put(id);
				folderIds.add(new Integer(folderId));
			} else {
				logger.debug("Folder filtered out because duplicate: [" + id + "]");
			}
		}
		return toReturn;
	}

	@GET
	@Path("/widget")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public List<Widget> getEngine() throws EMFInternalError {

		List<Widget> ret = new ArrayList<Widget>();
		UserProfile profile = UserProfileManager.getProfile();
		Collection functionalities = profile.getFunctionalities();

		ret.add(Widget.builder().withName("Text").withDescKey("sbi.cockpit.editor.newwidget.description.text").withImg("1.png").withCssClass("fa fa-font")
				.withType("text").withTag("text").build());

		ret.add(Widget.builder().withName("Image").withDescKey("sbi.cockpit.editor.newwidget.description.image").withImg("2.png")
				.withCssClass("fa fa-picture-o").withType("image").withTag("image").build());

		ret.add(Widget.builder().withName("Chart").withDescKey("sbi.cockpit.editor.newwidget.description.chart").withImg("4.png")
				.withCssClass("fas fa-chart-pie").withType("chart").withTag("chart").build());

		ret.add(Widget.builder().withName("Html").withDescKey("sbi.cockpit.editor.newwidget.description.html").withImg("4.png").withCssClass("fab fa-html5")
				.withType("html").withTag("html").build());

		ret.add(Widget.builder().withName("Table").withDescKey("sbi.cockpit.editor.newwidget.description.table").withImg("5.png").withCssClass("fa fa-table")
				.withType("table").withTag("table").build());

		ret.add(Widget.builder().withName("Cross Table").withDescKey("sbi.cockpit.editor.newwidget.description.cross").withImg("6.png")
				.withCssClass("fa fa-table").withType("static-pivot-table").withTag("table").withTag("pivot").withTag("cross").build());

		if (functionalities.contains(DOCUMENT_WIDGET_USE)) {

			ret.add(Widget.builder().withName("Document").withDescKey("sbi.cockpit.editor.newwidget.description.document").withImg("7.png")
					.withCssClass("fas fa-file-invoice").withType("document").withTag("document").withTag("datasource").build());
		}

		if (functionalities.contains(MAP_WIDGET_USE)) {

			ret.add(Widget.builder().withName("Map").withDescKey("sbi.cockpit.editor.newwidget.description.map").withImg("7.png")
					.withCssClass("fas fa-map-marked-alt").withType("map").withTag("map").build());
		}

		ret.add(Widget.builder().withName("Active Selections").withDescKey("sbi.cockpit.editor.newwidget.description.selection").withImg("8.png")
				.withCssClass("fa fa-check-square-o").withType("selection").withTag("selection").build());

		ret.add(Widget.builder().withName("Selector").withDescKey("sbi.cockpit.editor.newwidget.description.selector").withImg("9.png")
				.withCssClass("fa fa-caret-square-o-down").withType("selector").withTag("selector").build());

		if (functionalities.contains(EDIT_PYTHON_SCRIPTS) && isWidgetAllowedByProduct("Python/R")) {
			ret.add(Widget.builder().withName("Python").withDescKey("sbi.cockpit.editor.newwidget.description.python").withImg("10.png")
					.withCssClass("fab fa-python").withType("python").withTag("python").build());

			ret.add(Widget.builder().withName("r").withDescKey("sbi.cockpit.editor.newwidget.description.R").withImg("11.png").withCssClass("fab fa-r-project")
					.withType("r").withTag("r").build());
		}

		if (functionalities.contains(DISCOVERY_WIDGET_USE) && isWidgetAllowedByProduct("Discovery")) {

			ret.add(Widget.builder().withName("Discovery").withDescKey("sbi.cockpit.editor.newwidget.description.discovery")/* TODO : .withImg(???) */
					.withCssClass("fa fa-rocket").withType("discovery").withTag("discovery").build());
		}
		if (functionalities.contains(CREATE_CUSTOM_CHART) && isWidgetAllowedByProduct("CustomChart")) {
			ret.add(Widget.builder().withName("Custom Chart").withDescKey("sbi.cockpit.editor.newwidget.description.custom.chart").withImg("4.png")
					.withCssClass("fas fa-bezier-curve").withType("customchart").withTag("customChart").build());
		}

		return ret;
	}

	private boolean isWidgetAllowedByProduct(String type) {
		boolean toReturn = false;
		try {
			ProductProfilerClient profiler = new ProductProfilerClient();
			String userId = (String) UserProfileManager.getProfile().getUserUniqueIdentifier();
			toReturn = profiler.isAllowedToCreateWidget(userId, type);
		} catch (Exception e) {
			logger.warn("Error while profiling " + type + " Widget permissions");
		}
		return toReturn;
	}
}

/**
 * POJO for widget menu item in available widgets dialog.
 *
 * @author Marco Libanori
 */
class Widget {

	static class Builder {
		private String name;
		private String descKey;
		private List<String> tags = new ArrayList<String>();
		private String img;
		private String cssClass;
		private String type;

		public Builder withName(String name) {
			this.name = name;
			return this;
		}

		public Builder withDescKey(String descKey) {
			this.descKey = descKey;
			return this;
		}

		public Builder withImg(String img) {
			this.img = img;
			return this;
		}

		public Builder withCssClass(String cssClass) {
			this.cssClass = cssClass;
			return this;
		}

		public Builder withType(String type) {
			this.type = type;
			return this;
		}

		public Builder withTag(String tag) {
			this.tags.add(tag);
			return this;
		}

		public Widget build() {
			return new Widget(name, descKey, img, cssClass, type, tags);
		}
	}

	public static Builder builder() {
		return new Builder();
	}

	final String name;
	final String descKey;
	final List<String> tags = new ArrayList<String>();
	final String img;
	final String cssClass;
	final String type;

	private Widget(String name, String descKey, String img, String cssClass, String type, List<String> tags) {
		super();
		this.name = name;
		this.descKey = descKey;
		this.tags.addAll(tags);
		this.img = img;
		this.cssClass = cssClass;
		this.type = type;
	}

	public String getName() {
		return name;
	}

	public String getDescKey() {
		return descKey;
	}

	public List<String> getTags() {
		return tags;
	}

	public String getImg() {
		return img;
	}

	public String getCssClass() {
		return cssClass;
	}

	public String getType() {
		return type;
	}
}
