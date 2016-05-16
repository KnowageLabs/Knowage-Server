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

import it.eng.spagobi.engines.whatif.WhatIfEngineConfig;
import it.eng.spagobi.engines.whatif.WhatIfEngineInstance;
import it.eng.spagobi.engines.whatif.common.AbstractWhatIfEngineService;
import it.eng.spagobi.engines.whatif.cube.CubeUtilities;
import it.eng.spagobi.engines.whatif.model.ModelConfig;
import it.eng.spagobi.engines.whatif.model.ResultSetConverter;
import it.eng.spagobi.engines.whatif.model.SpagoBIPivotModel;
import it.eng.spagobi.utilities.exceptions.SpagoBIRestServiceException;
import it.eng.spagobi.utilities.rest.RestUtilities;

import java.io.IOException;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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
import org.pivot4j.transform.SwapAxes;
import org.pivot4j.ui.collector.NonInternalPropertyCollector;
import org.pivot4j.ui.command.DrillDownCommand;

@Path("/1.0/member")
public class MemberResource extends AbstractWhatIfEngineService {

	private SpagoBIPivotModel model;
	private ModelConfig modelConfig;

	private void init() {
		WhatIfEngineInstance ei = getWhatIfEngineInstance();
		model = (SpagoBIPivotModel) ei.getPivotModel();
		modelConfig = getWhatIfEngineInstance().getModelConfig();
	}

	@POST
	@Path("/drilldown/{axis}/{position}/{member}")
	@Produces("text/html; charset=UTF-8")
	public String drillDown(@javax.ws.rs.core.Context HttpServletRequest req, @PathParam("axis") int axisPos, @PathParam("position") int positionPos,
			@PathParam("member") int memberPos) throws JSONException, IOException  {
		SimpleDateFormat format = new SimpleDateFormat("hh:mm:ss");
		String time = "Drilldown start " + format.format(new Date());
		//System.out.println(time);
		init();

		String str = RestUtilities.readBody(req);
		JSONObject jo = new JSONObject(str);
		model.removeSubset();
		//System.out.println(model.getCurrentMdx());
		// The ROWS axis
		CellSetAxis rowsOrColumns = getAxis(axisPos);

		// Member positions of the ROWS axis.
		List<Position> positions = rowsOrColumns.getPositions();

		Position p = CubeUtilities.getPosition(positions, jo.getString("positionUniqueName"));

		List<Member> m = p.getMembers();
		Member m2 = m.get(memberPos);

		try {
			m2 = CubeUtilities.getMember(model.getCube(), jo.getString("memberUniqueName"));
		} catch (OlapException e) {
			logger.error(e);
			throw new SpagoBIRestServiceException(getLocale(), e);
		}

		String drillType = modelConfig.getDrillType();

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
		modelConfig.setRowCount(model.getCellSet().getAxes().get(Axis.ROWS.axisOrdinal()).getPositionCount());
		modelConfig.setColumnCount(model.getCellSet().getAxes().get(Axis.COLUMNS.axisOrdinal()).getPositionCount());

		time = "Drilldown end " + format.format(new Date());
		//System.out.println(time);
		//System.out.println();
		//System.out.println();
		String table = renderModel(model);

		return table;
	}

	@GET
	@Path("/drillup/{axis}/{position}/{member}/{positionUniqueName}/{memberUniqueName}")
	@Produces("text/html; charset=UTF-8")
	public String drillUp(@javax.ws.rs.core.Context HttpServletRequest req, @PathParam("axis") int axisPos, @PathParam("position") int positionPos,
			@PathParam("member") int memberPos, @PathParam("positionUniqueName") String positionUniqueName,
			@PathParam("memberUniqueName") String memberUniqueName) {
		init();

		SimpleDateFormat format = new SimpleDateFormat("hh:mm:ss");
		String time = "Drillup start " + format.format(new Date());
		//System.out.println(time);
		List<Member> m = null;
		Member m2 = null;
		Hierarchy hierarchy = null;

		model.removeSubset();

		// The ROWS axis
		CellSetAxis rowsOrColumns = getAxis(axisPos);

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
			 * p = positions.get(positionPos); m = p.getMembers(); m2 =
			 * m.get(memberPos);
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

		modelConfig.setRowCount(model.getCellSet().getAxes().get(Axis.ROWS.axisOrdinal()).getPositionCount());
		modelConfig.setColumnCount(model.getCellSet().getAxes().get(Axis.COLUMNS.axisOrdinal()).getPositionCount());

		time = "Drillup end " + format.format(new Date());
		//System.out.println(time);
		//System.out.println();
		//System.out.println();
		return renderModel(model);
	}

	@GET
	@Path("/drilltrough/levels")
	@Produces("text/html; charset=UTF-8")
	public String getallLevels() throws OlapException {

		JSONArray array = new JSONArray();
		WhatIfEngineInstance ei = getWhatIfEngineInstance();
		SpagoBIPivotModel model = (SpagoBIPivotModel) ei.getPivotModel();
		try {
			List<Hierarchy> hs = model.getCube().getHierarchies();
			for (Hierarchy h : hs) {
				JSONObject hierarchy = new JSONObject();
				JSONArray levelsArray = new JSONArray();

				hierarchy.put("name", h.getName());
				List<Level> levels = h.getLevels();
				for (Level level : levels) {
					JSONObject levelsObject = new JSONObject();
					levelsObject.put("name", level.getName());
					levelsObject.put("hierarchy", level.getHierarchy().getUniqueName());
					levelsObject.put("depth", level.getDepth());
					levelsArray.put(levelsObject);

				}
				hierarchy.put("children", levelsArray);
				array.put(hierarchy);

			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return array.toString();
	}

	@GET
	@Path("/properties/{uni_name}")
	@Produces("text/html; charset=UTF-8")
	public String getProperties(@PathParam("uni_name") String name) throws OlapException, JSONException {

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

	@GET
	@Path("/drilltrough/{ordinal}")
	@Produces("text/html; charset=UTF-8")
	public String drillt(@PathParam("ordinal") Integer ordinal) throws OlapException {
		JSONArray array = null;
		ResultSet set;
		WhatIfEngineInstance ei = getWhatIfEngineInstance();
		SpagoBIPivotModel model = (SpagoBIPivotModel) ei.getPivotModel();
		CellSet cellSet = model.getCellSet();
		int max_rows = WhatIfEngineConfig.getInstance().getDrillTroughMaxRows();
		try {
			Cell cell = cellSet.getCell(ordinal);
			DrillThrough transform = model.getTransform(DrillThrough.class);

			set = transform.drillThrough(cell);
			array = ResultSetConverter.convertResultSetIntoJSON(set);
			// //System.out.println(array);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return array.toString();
	}

	@GET
	@Path("/drilltrough/{ordinal}/{collection}/{max}")
	@Produces("text/html; charset=UTF-8")
	public String drillfull(@PathParam("ordinal") Integer ordinal, @PathParam("collection") String col, @PathParam("max") Integer max) throws OlapException {
		JSONArray array = null;

		ResultSet set;
		WhatIfEngineInstance ei = getWhatIfEngineInstance();
		SpagoBIPivotModel model = (SpagoBIPivotModel) ei.getPivotModel();
		List<MetadataElement> selection = new ArrayList<MetadataElement>();
		CellSet cellSet = model.getCellSet();

		try {

			JSONArray collections = new JSONArray(col);
			for (int i = 0; i < collections.length(); i++) {
				JSONObject jsonObj = collections.getJSONObject(i);
				String hierarchy = jsonObj.getString("hierarchy");
				Integer depth = jsonObj.getInt("depth");
				Level l = CubeUtilities.getHierarchy(model.getCube(), hierarchy).getLevels().get(depth);
				selection.add(l);
			}

			Cell cell = cellSet.getCell(ordinal);

			DrillThrough transform = model.getTransform(DrillThrough.class);
			set = transform.drillThrough(cell, selection, max);
			array = ResultSetConverter.convertResultSetIntoJSON(set);
			// //System.out.println(array);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return array.toString();
	}

	@GET
	@Path("/sort/{axisToSortpos}/{axis}/{positionUniqueName}/{sortMode}")
	public String sort(@PathParam("axisToSortpos") Integer axisToSortpos, @PathParam("axis") Integer axis,
			@PathParam("positionUniqueName") String positionUniqueName, @PathParam("sortMode") String sortMode) {
		init();
		model.setSorting(true);
		SortCriteria nextSortCriteria = SortMode.fromName(sortMode).nextMode(model.getSortCriteria());
		model.setSortCriteria(nextSortCriteria);
		sortModel(axisToSortpos, axis, positionUniqueName, sortMode);
		//System.out.println(model.getSortCriteria());
		model.setSorting(true);
		String table = renderModel(model);
		//System.out.println(model.getSortCriteria() + "memberResource 341");
		return table;
	}

	@GET
	@Path("/sort/{axisToSortpos}/{axis}/{positionUniqueName}/{sortMode}/{topBottomCount}")
	public String sort(@PathParam("axisToSortpos") Integer axisToSortpos, @PathParam("axis") Integer axis,
			@PathParam("positionUniqueName") String positionUniqueName, @PathParam("sortMode") String sortMode,
			@PathParam("topBottomCount") Integer topBottomCount) {
		init();
		model.setTopBottomCount(topBottomCount);
		model.setSorting(true);

		SortCriteria nextSortCriteria = SortMode.fromName(sortMode).nextMode(model.getSortCriteria());
		model.setSortCriteria(nextSortCriteria);

		sortModel(axisToSortpos, axis, positionUniqueName, sortMode);
		model.setSorting(true);
		return renderModel(model);
	}

	@GET
	@Path("/sort/disable")
	public String sorten() {
		init();

		model.removeSubset();

		ModelConfig modelConfig = getWhatIfEngineInstance().getModelConfig();
		getWhatIfEngineInstance().getModelConfig().setSortingEnabled(!modelConfig.getSortingEnabled());
		if (!modelConfig.getSortingEnabled()) {
			model.setSortCriteria(null);
			model.setSorting(false);
		}

		model.removeOrder(model.getCellSet().getAxes().get(Axis.ROWS.axisOrdinal()));
		model.removeOrder(model.getCellSet().getAxes().get(Axis.COLUMNS.axisOrdinal()));
		List<Member> a = model.getSortPosMembers1();
		a.clear();

		return renderModel(model);
	}

	private CellSetAxis getAxis(int axisPos) {

		CellSet cellSet = model.getCellSet();

		return cellSet.getAxes().get(axisPos);

	}

	private void sortModel(Integer axisToSortpos, Integer axis, String positionUniqueName, String sortMode) {

		CellSetAxis axisToSort = null;
		CellSetAxis axisM = null;

		SwapAxes transform = model.getTransform(SwapAxes.class);

		model.removeSubset();
		model.removeOrder(model.getCellSet().getAxes().get(1));
		model.removeOrder(model.getCellSet().getAxes().get(0));
		axisToSort = getAxis(axisToSortpos);
		axisM = getAxis(axis);

		List<Position> positions = axisM.getPositions();

		Position position = CubeUtilities.getPosition(positions, positionUniqueName);

		if (transform.isSwapAxes()) {

			axisToSort = axisM;

		}
		if (!model.isSorting(position)) {
			while (model.getSortCriteria() != null) {
				SortCriteria nextSortCriteria = SortMode.fromName(sortMode).nextMode(model.getSortCriteria());
				model.setSortCriteria(nextSortCriteria);
			}
			SortCriteria nextSortCriteria = SortMode.fromName(sortMode).nextMode(model.getSortCriteria());
			model.setSortCriteria(nextSortCriteria);
		}
		if (model.getSortCriteria() != null) {

			model.sort(axisToSort, position);
			model.getSortPosMembers1();

			if (model.getSortCriteria().equals(SortCriteria.BOTTOMCOUNT) || model.getSortCriteria().equals(SortCriteria.TOPCOUNT)) {
				modelConfig.setStartRow(0);
				modelConfig.setStartColumn(0);

			}

		}

	}

	@GET
	@Path("/start/{x}/{y}")
	public String startFrom(@PathParam("x") Integer x, @PathParam("y") Integer y) {
		SimpleDateFormat format = new SimpleDateFormat("hh:mm:ss");
		String time = "Start from start " + format.format(new Date());
		//System.out.println(time);
		init();

		model.startFrom(x, y);
		time = "Start from end " + format.format(new Date());
		//System.out.println(time);
		//System.out.println();
		//System.out.println();
		return renderModel(model);
	}

}
