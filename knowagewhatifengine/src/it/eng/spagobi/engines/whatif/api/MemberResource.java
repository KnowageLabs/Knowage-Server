/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
/**
 * @author Monica Franceschini (monica.franceschini@eng.it)
 */
package it.eng.spagobi.engines.whatif.api;

import it.eng.spagobi.engines.whatif.WhatIfEngineInstance;
import it.eng.spagobi.engines.whatif.common.AbstractWhatIfEngineService;
import it.eng.spagobi.engines.whatif.cube.CubeUtilities;
import it.eng.spagobi.engines.whatif.model.SpagoBIPivotModel;
import it.eng.spagobi.utilities.exceptions.SpagoBIRestServiceException;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.olap4j.CellSet;
import org.olap4j.CellSetAxis;
import org.olap4j.OlapException;
import org.olap4j.Position;
import org.olap4j.metadata.Hierarchy;
import org.olap4j.metadata.Member;
import org.pivot4j.sort.SortCriteria;
import org.pivot4j.transform.DrillExpandMember;
import org.pivot4j.transform.DrillExpandPosition;
import org.pivot4j.transform.DrillReplace;
import org.pivot4j.ui.command.DrillDownCommand;

@Path("/1.0/member")
public class MemberResource extends AbstractWhatIfEngineService {

	@GET
	@Path("/drilldown/{axis}/{position}/{member}/{positionUniqueName}/{memberUniqueName}")
	@Produces("text/html; charset=UTF-8")
	public String drillDown(@javax.ws.rs.core.Context HttpServletRequest req, @PathParam("axis") int axisPos, @PathParam("position") int positionPos,
			@PathParam("member") int memberPos, @PathParam("positionUniqueName") String positionUniqueName,
			@PathParam("memberUniqueName") String memberUniqueName) {
		SimpleDateFormat format = new SimpleDateFormat("hh:mm:ss");
		String time = "Drilldown start " + format.format(new Date());
		System.out.println(time);
		WhatIfEngineInstance ei = getWhatIfEngineInstance();
		SpagoBIPivotModel model = (SpagoBIPivotModel) ei.getPivotModel();
		Integer subsetStart = model.getSubsetStart(model.getCellSet().getAxes().get(1));
		model.removeSubset(model.getCellSet().getAxes().get(1));
		CellSet cellSet = model.getCellSet();
		// Axes of the resulting query.
		List<CellSetAxis> axes = cellSet.getAxes();

		// The ROWS axis
		CellSetAxis rowsOrColumns = axes.get(axisPos);

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

		String drillType = ei.getModelConfig().getDrillType();

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
		SimpleDateFormat format = new SimpleDateFormat("hh:mm:ss");
		String time = "Drillup start " + format.format(new Date());
		System.out.println(time);
		List<Member> m = null;
		Member m2 = null;
		WhatIfEngineInstance ei = getWhatIfEngineInstance();
		SpagoBIPivotModel model = (SpagoBIPivotModel) ei.getPivotModel();

		model.removeSubset(model.getCellSet().getAxes().get(1));
		CellSet cellSet = model.getCellSet();

		// Axes of the resulting query.
		List<CellSetAxis> axes = cellSet.getAxes();

		// The ROWS axis
		CellSetAxis rowsOrColumns = axes.get(axisPos);

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

		String drillType = ei.getModelConfig().getDrillType();

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

		Integer subsetStart = model.getSubsetStart(model.getCellSet().getAxes().get(1));
		model.setSubset(rowsOrColumns, subsetStart, 10);
		time = "Drillup end " + format.format(new Date());
		System.out.println(time);
		System.out.println();
		System.out.println();
		return renderModel(model);
	}

	@GET
	@Path("/sort/{axisToSortpos}/{axis}/{positionUniqueName}/{sortType}")
	public String sort(@PathParam("axisToSortpos") Integer axisToSortpos, @PathParam("axis") Integer axis,
			@PathParam("positionUniqueName") String positionUniqueName, @PathParam("sortType") String sortType) {

		WhatIfEngineInstance ei = getWhatIfEngineInstance();
		SpagoBIPivotModel model = (SpagoBIPivotModel) ei.getPivotModel();

		Integer subsetStart = model.getSubsetStart(model.getCellSet().getAxes().get(1));
		model.removeSubset(model.getCellSet().getAxes().get(1));
		CellSet cellSet = model.getCellSet();
		CellSetAxis axisToSort = cellSet.getAxes().get(axisToSortpos);
		CellSetAxis axisM = cellSet.getAxes().get(axis);
		List<Position> positions = axisM.getPositions();

		Position position = CubeUtilities.getPosition(positions, positionUniqueName);

		model.setSorting(true);
		model.setSortCriteria(SortCriteria.valueOf(sortType));

		model.sort(axisToSort, position);

		model.setSubset(axisToSort, subsetStart, 10);
		return renderModel(model);
	}

	@GET
	@Path("/next/{axis}/{step}")
	public String next(@PathParam("axis") Integer axis, @PathParam("step") Integer step) {
		WhatIfEngineInstance ei = getWhatIfEngineInstance();
		SpagoBIPivotModel model = (SpagoBIPivotModel) ei.getPivotModel();

		CellSet cellSet = model.getCellSet();

		CellSetAxis axisToSet = cellSet.getAxes().get(axis);

		model.next(axisToSet, step);

		return renderModel(model);
	}

	@GET
	@Path("/previous/{axis}/{step}")
	public String previous(@PathParam("axis") Integer axis, @PathParam("step") Integer step) {

		WhatIfEngineInstance ei = getWhatIfEngineInstance();
		SpagoBIPivotModel model = (SpagoBIPivotModel) ei.getPivotModel();

		CellSet cellSet = model.getCellSet();

		CellSetAxis axisToSet = cellSet.getAxes().get(axis);

		model.previous(axisToSet, step);

		return renderModel(model);
	}

	@GET
	@Path("/start/{axis}/{start}")
	public String startFrom(@PathParam("axis") Integer axis, @PathParam("start") Integer start) {
		SimpleDateFormat format = new SimpleDateFormat("hh:mm:ss");
		String time = "Start from start " + format.format(new Date());
		System.out.println(time);
		WhatIfEngineInstance ei = getWhatIfEngineInstance();
		SpagoBIPivotModel model = (SpagoBIPivotModel) ei.getPivotModel();

		CellSet cellSet = model.getCellSet();

		CellSetAxis axisToSet = cellSet.getAxes().get(axis);

		model.startFrom(axisToSet, start);
		time = "Start from end " + format.format(new Date());
		System.out.println(time);
		System.out.println();
		System.out.println();
		return renderModel(model);
	}

}
