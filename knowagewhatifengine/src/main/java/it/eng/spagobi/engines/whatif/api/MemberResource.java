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

package it.eng.spagobi.engines.whatif.api;

import java.io.IOException;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.olap4j.Axis;
import org.olap4j.Cell;
import org.olap4j.CellSet;
import org.olap4j.CellSetAxis;
import org.olap4j.OlapException;
import org.olap4j.Position;
import org.olap4j.metadata.Hierarchy;
import org.olap4j.metadata.Level;
import org.olap4j.metadata.Member;
import org.olap4j.metadata.MetadataElement;
import org.olap4j.metadata.Property;
import org.pivot4j.sort.SortCriteria;
import org.pivot4j.sort.SortMode;
import org.pivot4j.transform.DrillExpandMember;
import org.pivot4j.transform.DrillExpandPosition;
import org.pivot4j.transform.DrillReplace;
import org.pivot4j.transform.DrillThrough;
import org.pivot4j.ui.collector.NonInternalPropertyCollector;
import org.pivot4j.ui.command.DrillDownCommand;

import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;

import it.eng.spagobi.engines.whatif.WhatIfEngineInstance;
import it.eng.spagobi.engines.whatif.common.AbstractWhatIfEngineService;
import it.eng.spagobi.engines.whatif.cube.CubeUtilities;
import it.eng.spagobi.engines.whatif.model.ModelConfig;
import it.eng.spagobi.engines.whatif.model.ResultSetConverter;
import it.eng.spagobi.engines.whatif.model.SpagoBIPivotModel;
import it.eng.spagobi.services.rest.annotations.ManageAuthorization;
import it.eng.spagobi.utilities.exceptions.SpagoBIRestServiceException;
import it.eng.spagobi.utilities.rest.RestUtilities;

@Path("/1.0/member")
@ManageAuthorization

public class MemberResource extends AbstractWhatIfEngineService {

	private static String factCountUniqueName = "[Measures].[Fact Count]";

	@POST
	@Path("/drilldown/{axis}/{position}/{member}")
	@Produces("text/html; charset=UTF-8")

	public String drillDown(@javax.ws.rs.core.Context HttpServletRequest req, @PathParam("axis") int axisPos, @PathParam("position") int positionPos,
			@PathParam("member") int memberPos) throws JSONException, IOException {
		Member m2 = null;
		Monitor totalTime = MonitorFactory.start("WhatIfEngine/it.eng.spagobi.engines.whatif.api.MemberResource.serialize.drillDown.totalTime");

		Monitor readbodyTime = MonitorFactory.start("WhatIfEngine/it.eng.spagobi.engines.whatif.api.MemberResource.serialize.drillDown.readBody");
		WhatIfEngineInstance ei = getWhatIfEngineInstance();
		SpagoBIPivotModel model = (SpagoBIPivotModel) ei.getPivotModel();
		ModelConfig modelConfig = getWhatIfEngineInstance().getModelConfig();

		model.removeSubset();
		JSONObject jo = RestUtilities.readBodyAsJSONObject(req);
		readbodyTime.stop();

		Monitor getDrillInfoTime = MonitorFactory.start("WhatIfEngine/it.eng.spagobi.engines.whatif.api.MemberResource.serialize.drillDown.getdrillinfo");
		// The ROWS axis
		CellSetAxis rowsOrColumns = getAxis(axisPos, model);

		// Member positions of the ROWS axis.
		List<Position> positions = rowsOrColumns.getPositions();

		Position p = CubeUtilities.getPosition(positions, jo.getString("positionUniqueName"));

		List<Member> m = p.getMembers();
		if (memberPos > -1) {
			m2 = m.get(memberPos);
		}

		try {
			m2 = CubeUtilities.getMember(model.getCube(), jo.getString("memberUniqueName"));
		} catch (OlapException e) {
			logger.error(e);
			throw new SpagoBIRestServiceException(getLocale(), e);
		}

		String drillType = modelConfig.getDrillType();
		getDrillInfoTime.stop();

		Monitor doDrillTime = MonitorFactory.start("WhatIfEngine/it.eng.spagobi.engines.whatif.api.MemberResource.serialize.drillDown.dodrill");
		if (drillType == null || drillType.equals(DrillDownCommand.MODE_POSITION)) {
			DrillExpandPosition transform = model.getTransform(DrillExpandPosition.class);
			if (transform.canExpand(p, m2)) {

				transform.expand(p, m2);
			}
		} else if (drillType != null && drillType.equals(DrillDownCommand.MODE_REPLACE)) {

			DrillReplace transform = model.getTransform(DrillReplace.class);
			if (transform.canDrillDown(m2)) {
				transform.drillDown(m2);
			}
		} else if (drillType != null && drillType.equals(DrillDownCommand.MODE_MEMBER)) {
			DrillExpandMember transform = model.getTransform(DrillExpandMember.class);
			if (transform.canExpand(m2)) {
				transform.expand(m2);
			}
		}
		// modelConfig.setRowCount(model.getCellSet().getAxes().get(Axis.ROWS.axisOrdinal()).getPositionCount());
		// modelConfig.setColumnCount(model.getCellSet().getAxes().get(Axis.COLUMNS.axisOrdinal()).getPositionCount());
		doDrillTime.stop();

		Monitor renderTime = MonitorFactory.start("WhatIfEngine/it.eng.spagobi.engines.whatif.api.MemberResource.serialize.drillDown.render");
		String table = renderModel(model);
		renderTime.stop();

		totalTime.stop();
		return table;
	}

	@POST
	@Path("/drillup")
	// {axis}/{position}/{memberPosition}/{positionUniqueName}/{memberUniqueName}
	@Produces("text/html; charset=UTF-8")

	public String drillUp(@javax.ws.rs.core.Context HttpServletRequest req) {
		WhatIfEngineInstance ei = getWhatIfEngineInstance();
		SpagoBIPivotModel model = (SpagoBIPivotModel) ei.getPivotModel();
		ModelConfig modelConfig = getWhatIfEngineInstance().getModelConfig();
		model.removeSubset();
		JSONObject jo;
		int axis = 0;

		String positionUniqueName = null;
		String memberUniqueName = null;

		try {
			jo = RestUtilities.readBodyAsJSONObject(req);
			axis = jo.getInt("axis");

			positionUniqueName = jo.getString("positionUniqueName");
			memberUniqueName = jo.getString("memberUniqueName");

		} catch (IOException e1) {
			logger.error("Error reading body", e1);
		} catch (JSONException e) {
			logger.error("Error serializing JSON", e);
		}

		Member m2 = null;
		Hierarchy hierarchy = null;

		// The ROWS axis
		CellSetAxis rowsOrColumns = getAxis(axis, model);

		// Member positions of the ROWS axis.
		List<Position> positions = rowsOrColumns.getPositions();

		Position p = CubeUtilities.getPosition(positions, positionUniqueName);

		// if the drill is of type replace, the link of the table for drill up
		// is the header of the hierarchy so instead of the memberUniqueName
		// positionUniqueName it contains the placeholder "x"
		if (!memberUniqueName.equals("x")) {

			try {
				m2 = CubeUtilities.getMember(model.getCube(), memberUniqueName);
				hierarchy = m2.getHierarchy();
			} catch (OlapException e) {
				logger.error(e);
				throw new SpagoBIRestServiceException(getLocale(), e);
			}
		} else {
			/*
			 * p = positions.get(positionPos); m = p.getMembers(); m2 = m.get(memberPos);
			 */
			try {
				hierarchy = CubeUtilities.getHierarchy(model.getCube(), positionUniqueName);
			} catch (OlapException e) {
				logger.error(e);
				throw new SpagoBIRestServiceException(getLocale(), e);
			}
		}

		// hierarchy = m2.getHierarchy();

		String drillType = modelConfig.getDrillType();

		if (drillType == null || drillType.equals(DrillDownCommand.MODE_POSITION)) {
			DrillExpandPosition transform = model.getTransform(DrillExpandPosition.class);
			if (transform.canCollapse(p, m2)) {
				transform.collapse(p, m2);
			}
		} else if (drillType != null && drillType.equals(DrillDownCommand.MODE_REPLACE)) {
			DrillReplace transform = model.getTransform(DrillReplace.class);
			if (transform.canDrillUp(hierarchy)) {
				transform.drillUp(hierarchy);
			}
		} else if (drillType != null && drillType.equals(DrillDownCommand.MODE_MEMBER)) {
			DrillExpandMember transform = model.getTransform(DrillExpandMember.class);
			if (transform.canCollapse(m2)) {
				transform.collapse(m2);
			}
		}

		return renderModel(model);
	}

	@POST
	@Path("/drilltrough/levels")
	@Produces("text/html; charset=UTF-8")

	public String getallLevels(@javax.ws.rs.core.Context HttpServletRequest req) throws OlapException {

		JSONArray array = new JSONArray();
		WhatIfEngineInstance ei = getWhatIfEngineInstance();
		SpagoBIPivotModel model = (SpagoBIPivotModel) ei.getPivotModel();
		String filter = null;

		try {
			JSONObject paramsObj = RestUtilities.readBodyAsJSONObject(req);

			filter = paramsObj.getString("filters");
		} catch (Exception e) {
			logger.error("Error reading body", e);
		}

		try {

			CellSet cellSet = model.getCellSet();
			List<CellSetAxis> axis = cellSet.getAxes();
			List<Hierarchy> axisHierarchies = axis.get(0).getAxisMetaData().getHierarchies();
			axisHierarchies.addAll(axis.get(1).getAxisMetaData().getHierarchies());

			JSONArray filters = new JSONArray(filter);
			for (int i = 0; i < filters.length(); i++) {
				JSONObject jsonObj = filters.getJSONObject(i);
				Hierarchy h = CubeUtilities.getHierarchy(model.getCube(), jsonObj.getString("selectedHierarchyUniqueName"));
				axisHierarchies.add(h);
			}

			// List<Hierarchy> hs = model.getCube().getHierarchies();
			for (Hierarchy h : axisHierarchies) {

				JSONObject hierarchy = new JSONObject();
				JSONArray levelsArray = new JSONArray();

				hierarchy.put("caption", h.getCaption());
				List<Level> levels = h.getLevels();
				for (Level level : levels) {
					if (level.getName() == "(All)") {
						continue;
					}
					if (level.getName() == "MeasuresLevel") {

						List<Member> temp = level.getMembers();
						for (Member member : temp) {
							if (!member.getUniqueName().equals(factCountUniqueName)) {// removes
																						// the
																						// Fact
																						// Count
																						// measure.
																						// It's
																						// a
																						// fake
																						// measures
																						// added
																						// from
																						// mondrian
																						// to
																						// ensure
																						// that
																						// cube
																						// has
																						// an
																						// atomic
																						// cell
																						// count
								JSONObject levelsObject = new JSONObject();
								levelsObject.put("caption", member.getCaption());
								levelsObject.put("uniqueName", member.getUniqueName());
								levelsObject.put("hierarchy", member.getHierarchy().getUniqueName());
								levelsObject.put("depth", member.getDepth());
								levelsArray.put(levelsObject);
							}
						}
						continue;
					}
					JSONObject levelsObject = new JSONObject();
					levelsObject.put("caption", level.getCaption());
					levelsObject.put("uniqueName", level.getUniqueName());
					levelsObject.put("hierarchy", level.getHierarchy().getUniqueName());
					levelsObject.put("depth", level.getDepth());
					levelsArray.put(levelsObject);

				}
				hierarchy.put("children", levelsArray);
				array.put(hierarchy);

			}

		} catch (Exception e) {
			logger.error("Error making JSON", e);
		}
		return array.toString();
	}

	@POST
	@Path("/properties")
	@Produces("text/html; charset=UTF-8")

	public String getProperties(@javax.ws.rs.core.Context HttpServletRequest req) throws OlapException, JSONException {

		String name = null;

		try {
			JSONObject paramsObj = RestUtilities.readBodyAsJSONObject(req);

			name = paramsObj.getString("memberUniqueName");
		} catch (Exception e) {
			logger.error("Error reading body", e);
		}

		WhatIfEngineInstance ei = getWhatIfEngineInstance();
		SpagoBIPivotModel model = (SpagoBIPivotModel) ei.getPivotModel();
		NonInternalPropertyCollector np = new NonInternalPropertyCollector();

		JSONArray propsArray = new JSONArray();
		Member m = CubeUtilities.getMember(model.getCube(), name);
		Level l = m.getLevel();
		List<Property> properties = np.getProperties(l);

		for (Property property : properties) {
			JSONObject obj = new JSONObject();
			obj.put("name", property.getName());
			obj.put("value", m.getPropertyFormattedValue(property));
			propsArray.put(obj);
		}
		return propsArray.toString();
	}

	@POST
	@Path("/drilltrough")
	@Produces("text/html; charset=UTF-8")

	public String drillt(@javax.ws.rs.core.Context HttpServletRequest req) throws OlapException {
		JSONArray array = null;
		ResultSet set;
		int ordinal = 0;

		try {
			JSONObject paramsObj = RestUtilities.readBodyAsJSONObject(req);

			ordinal = paramsObj.getInt("ordinal");
		} catch (Exception e) {
			logger.error("Error reading body", e);
		}
		WhatIfEngineInstance ei = getWhatIfEngineInstance();
		SpagoBIPivotModel model = (SpagoBIPivotModel) ei.getPivotModel();
		CellSet cellSet = model.getCellSet();

		try {
			Cell cell = cellSet.getCell(ordinal);
			DrillThrough transform = model.getTransform(DrillThrough.class);

			set = transform.drillThrough(cell);
			array = ResultSetConverter.convertResultSetIntoJSON(set);
		} catch (Exception e) {
			logger.error("Error in drillThrough", e);
		}
		return array.toString();
	}

	@POST
	@Path("/drilltrough/full")
	@Produces("text/html; charset=UTF-8")

	public String drillfull(@javax.ws.rs.core.Context HttpServletRequest req) throws OlapException {
		JSONArray array = null;
		ResultSet set = null;
		int ordinal = 0;
		String col = null;
		int max = 0;

		try {
			JSONObject paramsObj = RestUtilities.readBodyAsJSONObject(req);

			ordinal = paramsObj.getInt("ordinal");
			max = paramsObj.getInt("max");
			col = paramsObj.getString("levels");
		} catch (Exception e) {
			logger.error("Error reading body", e);
		}
		WhatIfEngineInstance ei = getWhatIfEngineInstance();
		SpagoBIPivotModel model = (SpagoBIPivotModel) ei.getPivotModel();
		List<MetadataElement> selection = new ArrayList<MetadataElement>();
		CellSet cellSet = model.getCellSet();

		try {
			Cell cell = cellSet.getCell(ordinal);
			JSONArray collections = new JSONArray(col);
			for (int i = 0; i < collections.length(); i++) {
				JSONObject jsonObj = collections.getJSONObject(i);
				String unique = jsonObj.getString("uniqueName");
				String hierarchy = jsonObj.getString("hierarchy");
				Integer depth = jsonObj.getInt("depth");
				Level l = CubeUtilities.getHierarchy(model.getCube(), hierarchy).getLevels().get(depth);
				if (l.getName() == "MeasuresLevel") {

					Member m = CubeUtilities.getMember(model.getCube(), unique);
					selection.add(m);
				} else {
					selection.add(l);
				}
			}

			if (selection != null && cell != null) {
				DrillThrough transform = model.getTransform(DrillThrough.class);
				set = transform.drillThrough(cell, selection, max);
				array = ResultSetConverter.convertResultSetIntoJSON(set);
			}

		} catch (NullPointerException e) {
			throw new SpagoBIRestServiceException("Selected member isnt associated with table column", buildLocaleFromSession(), e);
		} catch (Exception e) {
			throw new SpagoBIRestServiceException("Mondrian error", buildLocaleFromSession(), e);
		}
		return array.toString();
	}

	@POST
	@Path("/sort")
	public String sort(@javax.ws.rs.core.Context HttpServletRequest req) {
		WhatIfEngineInstance ei = getWhatIfEngineInstance();
		SpagoBIPivotModel model = (SpagoBIPivotModel) ei.getPivotModel();
		ModelConfig modelConfig = getWhatIfEngineInstance().getModelConfig();
		JSONObject jo;

		model.setSorting(true);
		try {
			jo = RestUtilities.readBodyAsJSONObject(req);
			modelConfig.setAxisToSort(jo.getInt("axisToSort"));
			modelConfig.setAxis(jo.getInt("axis"));
			modelConfig.setSortingPositionUniqueName(jo.getString("positionUniqueName"));
			modelConfig.setSortMode(jo.getString("sortMode"));
			modelConfig.setTopBottomCount(jo.getInt("topBottomCount"));

		} catch (IOException e1) {

			logger.error("Error reading body", e1);
		} catch (JSONException e) {
			logger.error("Error making JSON", e);
		}
		model.setTopBottomCount(modelConfig.getTopBottomCount());

		SortCriteria nextSortCriteria = SortMode.fromName(modelConfig.getSortMode()).nextMode(model.getSortCriteria());
		model.setSortCriteria(nextSortCriteria);

		return renderModel(model);
	}

	@GET
	@Path("/sort/disable")
	public String sorten() {
		WhatIfEngineInstance ei = getWhatIfEngineInstance();
		SpagoBIPivotModel model = (SpagoBIPivotModel) ei.getPivotModel();
		ModelConfig modelConfig = getWhatIfEngineInstance().getModelConfig();

		model.removeSubset();

		getWhatIfEngineInstance().getModelConfig().setSortingEnabled(!modelConfig.getSortingEnabled());
		if (!modelConfig.getSortingEnabled()) {
			model.setSortCriteria(null);
			model.setSorting(false);
		}

		model.removeOrder(Axis.ROWS);
		model.removeOrder(Axis.COLUMNS);
		List<Member> a = model.getSortPosMembers1();
		a.clear();

		return renderModel(model);
	}

	private CellSetAxis getAxis(int axisPos, SpagoBIPivotModel model) {

		CellSet cellSet = model.getCellSet();

		return cellSet.getAxes().get(axisPos);

	}

}
