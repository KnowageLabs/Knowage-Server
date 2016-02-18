/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
/**
 * @author Alberto Ghedin (alberto.ghedin@eng.it)
 *
 * @class AxisResource
 *
 * Provides services to manage the axis resource
 *
 */
package it.eng.spagobi.engines.whatif.api;

import it.eng.spagobi.engines.whatif.WhatIfEngineInstance;
import it.eng.spagobi.engines.whatif.axis.AxisDimensionManager;
import it.eng.spagobi.engines.whatif.common.AbstractWhatIfEngineService;
import it.eng.spagobi.engines.whatif.model.SpagoBIPivotModel;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.apache.log4j.Logger;
import org.olap4j.metadata.Hierarchy;
import org.olap4j.metadata.Member;
import org.pivot4j.transform.SwapAxes;

@Path("/1.0/axis")
public class AxisResource extends AbstractWhatIfEngineService {

	public static transient Logger logger = Logger.getLogger(AxisResource.class);

	private AxisDimensionManager axisBusiness;

	private AxisDimensionManager getAxisBusiness() {
		WhatIfEngineInstance ei = getWhatIfEngineInstance();

		if (axisBusiness == null) {
			axisBusiness = new AxisDimensionManager(ei.getPivotModel());
		}
		return axisBusiness;
	}

	/**
	 * Service to swap the axis
	 *
	 * @return the rendered pivot table
	 */
	@POST
	@Path("/swap")
	@Produces("text/html; charset=UTF-8")
	public String swapAxis() {
		logger.debug("IN");
		WhatIfEngineInstance ei = getWhatIfEngineInstance();
		SpagoBIPivotModel model = (SpagoBIPivotModel) ei.getPivotModel();

		SwapAxes transform = model.getTransform(SwapAxes.class);
		if (transform.isSwapAxes()) {
			transform.setSwapAxes(false);
		} else {
			transform.setSwapAxes(true);
		}

		model.setSubset(model.getCellSet().getAxes().get(1), 0, 15);
		String table = renderModel(model);
		logger.debug("OUT");
		return table;

	}

	/**
	 * Service to move an hierarchy from an axis to another
	 *
	 * @param req
	 *            the HttpServletRequest
	 * @param fromAxisPos
	 *            the source axis(0 for rows, 1 for columns, -1 for filters)
	 * @param toAxisPos
	 *            the destination axis(0 for rows, 1 for columns, -1 for
	 *            filters)
	 * @param hierarchyName
	 *            the unique name of the hierarchy to move
	 * @return the rendered pivot table
	 */
	@POST
	@Path("/{fromAxis}/moveDimensionToOtherAxis/{hierarchy}/{toAxis}")
	@Produces("text/html; charset=UTF-8")
	public String placeHierarchyOnAxis(@javax.ws.rs.core.Context HttpServletRequest req, @PathParam("fromAxis") int fromAxisPos,
			@PathParam("toAxis") int toAxisPos, @PathParam("hierarchy") String hierarchyName) {

		getAxisBusiness().moveDimensionToOtherAxis(fromAxisPos, toAxisPos, hierarchyName);

		return renderModel(getPivotModel());
	}

	/**
	 * Service to move a hierarchy in the axis
	 *
	 * @param req
	 *            the HttpServletRequest
	 * @param axisPos
	 *            the destination axis(0 for rows, 1 for columns, -1 for
	 *            filters)
	 * @param hierarchyUniqueName
	 *            the unique name of the hierarchy to move
	 * @param newPosition
	 *            the new position of the hierarchy
	 * @param direction
	 *            the direction of the movement (-1 up, +1 down)
	 * @return the rendered pivot table
	 */
	@POST
	@Path("/{axis}/moveHierarchy/{hierarchyUniqueName}/{newPosition}/{direction}")
	@Produces("text/html; charset=UTF-8")
	public String moveHierarchies(@javax.ws.rs.core.Context HttpServletRequest req, @PathParam("axis") int axisPos,
			@PathParam("hierarchyUniqueName") String hierarchyUniqueName, @PathParam("newPosition") int newPosition, @PathParam("direction") int direction) {

		getAxisBusiness().moveHierarchy(axisPos, hierarchyUniqueName, newPosition, direction);

		return renderModel(getPivotModel());
	}

	/**
	 * Removes the oldHierarchy from the axis and adds the new newHierarchy in
	 * the same position
	 *
	 * @param axisPos
	 *            the axis that contains the old hierarchy
	 * @param newHierarchyUniqueName
	 *            the unique name of the new hierarchy
	 * @param oldHierarchyUniqueName
	 *            the unique name of the old hierarchy
	 * @param hierarchyPosition
	 *            the position of the old hierarchy
	 */
	@POST
	@Path("/{axis}/updateHierarchyOnDimension/{newHierarchyUniqueName}/{oldHierarchyUniqueName}/{hierarchyPosition}")
	@Produces("text/html; charset=UTF-8")
	public String updateHierarchyOnDimension(@javax.ws.rs.core.Context HttpServletRequest req, @PathParam("axis") int axisPos,
			@PathParam("newHierarchyUniqueName") String newHierarchyUniqueName, @PathParam("oldHierarchyUniqueName") String oldHierarchyUniqueName,
			@PathParam("hierarchyPosition") int hierarchyPosition) {

		Hierarchy h = getAxisBusiness().updateHierarchyOnAxis(axisPos, newHierarchyUniqueName, oldHierarchyUniqueName, hierarchyPosition);

		getModelConfig().setDimensionHierarchy(h.getDimension().getUniqueName(), newHierarchyUniqueName);

		return renderModel(getPivotModel());
	}

	/**
	 * Service to change the visibility of the members of a hierarchy. It takes
	 * a hierarchy, removes all the members and shows only the ones passed in
	 * the body of the request
	 *
	 * @param req
	 *            the HttpServletRequest
	 * @param axisPos
	 *            the source axis(0 for rows, 1 for columns, -1 for filters)
	 * @return the rendered pivot table
	 */
	@POST
	@Path("/{axis}/placeMembersOnAxis")
	@Produces("text/html; charset=UTF-8")
	public String placeMembersOnAxis(@javax.ws.rs.core.Context HttpServletRequest req, @PathParam("axis") int axisPos) {

		List<Member> members = getMembersFromBody();

		if (members.size() > 0) {
			getAxisBusiness().updateAxisHierarchyMembers(members.get(0).getHierarchy(), members);
		}

		return renderModel(getPivotModel());
	}

}
