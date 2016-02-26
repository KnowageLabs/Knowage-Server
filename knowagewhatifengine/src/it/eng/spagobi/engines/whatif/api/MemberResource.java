/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
/**
 * @author Monica Franceschini (monica.franceschini@eng.it)
 */
package it.eng.spagobi.engines.whatif.api;

import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.json.JSONArray;
import org.olap4j.Cell;
import org.olap4j.Axis;
import org.olap4j.CellSet;
import org.olap4j.CellSetAxis;
import org.olap4j.OlapException;
import org.olap4j.Position;
import org.olap4j.metadata.Hierarchy;
import org.olap4j.metadata.Level;
import org.olap4j.metadata.Member;
import org.olap4j.metadata.MetadataElement;
import org.pivot4j.sort.SortCriteria;
import org.pivot4j.sort.SortMode;
import org.pivot4j.transform.DrillExpandMember;
import org.pivot4j.transform.DrillExpandPosition;
import org.pivot4j.transform.DrillReplace;
import org.pivot4j.transform.DrillThrough;
import org.pivot4j.transform.SwapAxes;
import org.pivot4j.ui.command.DrillDownCommand;

import it.eng.spagobi.engines.whatif.WhatIfEngineInstance;
import it.eng.spagobi.engines.whatif.common.AbstractWhatIfEngineService;
import it.eng.spagobi.engines.whatif.cube.CubeUtilities;
import it.eng.spagobi.engines.whatif.model.ModelConfig;
import it.eng.spagobi.engines.whatif.model.ResultSetConverter;
import it.eng.spagobi.engines.whatif.model.SpagoBIPivotModel;
import it.eng.spagobi.utilities.exceptions.SpagoBIRestServiceException;

@Path("/1.0/member")
public class MemberResource extends AbstractWhatIfEngineService {

	private SpagoBIPivotModel model;
	private ModelConfig modelConfig;

	private void init() {
		WhatIfEngineInstance ei = getWhatIfEngineInstance();
		model = (SpagoBIPivotModel) ei.getPivotModel();

		modelConfig = getWhatIfEngineInstance().getModelConfig();
	}

	@GET
	@Path("/drilldown/{axis}/{position}/{member}/{positionUniqueName}/{memberUniqueName}")
	@Produces("text/html; charset=UTF-8")
	public String drillDown(@javax.ws.rs.core.Context HttpServletRequest req, @PathParam("axis") int axisPos, @PathParam("position") int positionPos,
			@PathParam("member") int memberPos, @PathParam("positionUniqueName") String positionUniqueName,
			@PathParam("memberUniqueName") String memberUniqueName) {
		SimpleDateFormat format = new SimpleDateFormat("hh:mm:ss");
		String time = "Drilldown start " + format.format(new Date());
		System.out.println(time);
		init();
		Integer subsetStart = model.getSubsetStart(model.getCellSet().getAxes().get(1));
		model.removeSubset(getAxis(1));

		// The ROWS axis
		CellSetAxis rowsOrColumns = getAxis(axisPos);

		// Member positions of the ROWS axis.
		List<Position> positions = rowsOrColumns.getPositions();

		Position p = CubeUtilities.getPosition(positions, positionUniqueName);

		List<Member> m = p.getMembers();
		Member m2;// = m.get(memberPos);

		try {
			m2 = CubeUtilities.getMember(model.getCube(), memberUniqueName);
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

		model.setSubset(rowsOrColumns, subsetStart, 10);
		time = "Drilldown end " + format.format(new Date());
		System.out.println(time);
		System.out.println();
		System.out.println();
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
		System.out.println(time);
		List<Member> m = null;
		Member m2 = null;

		model.removeSubset(getAxis(1));

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
			} catch (OlapException e) {
				logger.error(e);
				throw new SpagoBIRestServiceException(getLocale(), e);
			}
		} else {
			p = positions.get(positionPos);
			m = p.getMembers();
			m2 = m.get(memberPos);
		}

		Hierarchy hierarchy = m2.getHierarchy();

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

		Integer subsetStart = model.getSubsetStart(getAxis(1));
		model.setSubset(rowsOrColumns, subsetStart, 10);
		time = "Drillup end " + format.format(new Date());
		System.out.println(time);
		System.out.println();
		System.out.println();
		return renderModel(model);
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
		try {
			Cell cell = cellSet.getCell(ordinal);
			List<MetadataElement> selection = new ArrayList<MetadataElement>();
			Hierarchy h = CubeUtilities.getHierarchy(model.getCube(), "[Region]");
			for (Level level : h.getLevels()) {
				selection.add(level);
			}
			DrillThrough transform = model.getTransform(DrillThrough.class);
			if (selection.isEmpty()) {
				set = transform.drillThrough(cell);
			} else {
				set = transform.drillThrough(cell, selection, 30);
			}
			array = ResultSetConverter.convertResultSetIntoJSON(set);
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
		sortModel(axisToSortpos, axis, positionUniqueName);

		return renderModel(model);
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
		sortModel(axisToSortpos, axis, positionUniqueName);

		return renderModel(model);
	}

	@GET
	@Path("/sort/disable")
	public String sorten() {
		init();

		Integer subsetStart = model.getSubsetStart(model.getCellSet().getAxes().get(1));

		model.removeSubset(model.getCellSet().getAxes().get(1));

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

		model.setSubset(model.getCellSet().getAxes().get(1), subsetStart, 10);

		return renderModel(model);
	}

	private CellSetAxis getAxis(int axisPos) {

		CellSet cellSet = model.getCellSet();

		return cellSet.getAxes().get(axisPos);

	}

	private void sortModel(Integer axisToSortpos, Integer axis, String positionUniqueName) {
		Integer subsetStart1 = model.getSubsetStart(getAxis(1));
		Integer subsetStart0 = model.getSubsetStart(getAxis(0));
		CellSetAxis axisToSort = null;
		CellSetAxis axisM = null;

		SwapAxes transform = model.getTransform(SwapAxes.class);

		model.removeSubset(getAxis(Axis.ROWS.axisOrdinal()));
		model.removeSubset(getAxis(Axis.COLUMNS.axisOrdinal()));

		axisToSort = getAxis(axisToSortpos);
		axisM = getAxis(axis);

		List<Position> positions = axisM.getPositions();

		Position position = CubeUtilities.getPosition(positions, positionUniqueName);

		if (transform.isSwapAxes()) {

			axisToSort = axisM;

		}
		if (model.getSortCriteria() != null) {
			model.sort(axisToSort, position);
			model.getSortPosMembers1();

			if (model.getSortCriteria().equals(SortCriteria.BOTTOMCOUNT) || model.getSortCriteria().equals(SortCriteria.TOPCOUNT)) {
				model.setSubset(model.getCellSet().getAxes().get(1), 0, 10);
				model.setSubset(model.getCellSet().getAxes().get(0), 0, 15);
			} else {
				model.setSubset(axisToSort, subsetStart1, 10);

				model.setSubset(axisM, subsetStart0, 10);
			}

		} else {
			model.removeSubset(model.getCellSet().getAxes().get(1));
			model.removeSubset(model.getCellSet().getAxes().get(0));
			model.removeOrder(model.getCellSet().getAxes().get(1));
			model.removeOrder(model.getCellSet().getAxes().get(0));
			model.setSubset(axisToSort, subsetStart1, 10);

			model.setSubset(axisM, subsetStart0, 10);

		}

	}

	@GET
	@Path("/start/{axis}/{start}")
	public String startFrom(@PathParam("axis") Integer axis, @PathParam("start") Integer start) {
		SimpleDateFormat format = new SimpleDateFormat("hh:mm:ss");
		String time = "Start from start " + format.format(new Date());
		System.out.println(time);
		init();

		CellSetAxis axisToSet = getAxis(axis);

		model.startFrom(axisToSet, start);
		time = "Start from end " + format.format(new Date());
		System.out.println(time);
		System.out.println();
		System.out.println();
		return renderModel(model);
	}

}
