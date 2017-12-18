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
package it.eng.spagobi.engines.whatif.axis;

import it.eng.spagobi.engines.whatif.cube.CubeUtilities;
import it.eng.spagobi.utilities.engines.SpagoBIEngineRuntimeException;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.olap4j.Axis;
import org.olap4j.OlapException;
import org.olap4j.metadata.Dimension;
import org.olap4j.metadata.Hierarchy;
import org.olap4j.metadata.Member;
import org.pivot4j.PivotModel;
import org.pivot4j.transform.ChangeSlicer;
import org.pivot4j.transform.PlaceHierarchiesOnAxes;
import org.pivot4j.transform.PlaceMembersOnAxes;

public class AxisDimensionManager {

	public static transient Logger logger = Logger.getLogger(AxisDimensionManager.class);

	private PivotModel model;

	public AxisDimensionManager(PivotModel model) {
		super();
		this.model = model;
	}

	/**
	 * Service to move an hierarchy from an axis to another
	 *
	 * @param fromAxisPos
	 *            the source axis(0 for rows, 1 for columns, -1 for filters)
	 * @param toAxisPos
	 *            the destination axis(0 for rows, 1 for columns, -1 for
	 *            filters)
	 * @param hierarchyName
	 *            the unique name of the hierarchy to move
	 * @return the moved hierarchy
	 */
	public Hierarchy moveDimensionToOtherAxis(int fromAxisPos, int toAxisPos, String hierarchyName) {
		logger.debug("IN");

		Hierarchy hierarchy = null;
		PlaceMembersOnAxes pm = getModel().getTransform(PlaceMembersOnAxes.class);
		PlaceHierarchiesOnAxes ph = getModel().getTransform(PlaceHierarchiesOnAxes.class);

		List<Member> membersToMove = null;

		try {
			logger.debug("getting the hierarchy object from the cube");
			hierarchy = CubeUtilities.getHierarchy(getModel().getCube(), hierarchyName);
		} catch (OlapException e) {
			logger.error("Error getting the hierrarchy " + hierarchyName + " from the cube ", e);
			throw new SpagoBIEngineRuntimeException("Error addingthe hierrarchy " + hierarchyName + " in the axis " + toAxisPos, e);
		}

		// if the old axis is -1 the source are the filters
		if (fromAxisPos < 0) {

			// removes the slicers
			logger.debug("Removing slicers from the hierarchy " + hierarchy.getUniqueName());
			ChangeSlicer cs = getModel().getTransform(ChangeSlicer.class);
			List<org.olap4j.metadata.Member> slicers = cs.getSlicer(hierarchy);
			slicers.clear();
			cs.setSlicer(hierarchy, slicers);
			logger.debug("Slicers cleaned");

			logger.debug("Adding the hierarchy in the axis " + toAxisPos);
			ph.addHierarchy(CubeUtilities.getAxis(toAxisPos), hierarchy, false, -1);
			logger.debug("Hierarchy added");

		} else {
			// Column and rows

			membersToMove = pm.findVisibleMembers(hierarchy);

			// remove the members from the axis
			logger.debug("Removing the hierarchy from the axis " + fromAxisPos);
			ph.removeHierarchy(CubeUtilities.getAxis(fromAxisPos), hierarchy);
			logger.debug("Removed the hierarchy from the axis " + fromAxisPos);

			// if the new axis is not a filter we should ad the hierarchy and
			// the members in the axis. If the new axis is a filter we've only
			// to remove..
			// ..it from the origin axis
			if (toAxisPos >= 0) {
				// adding the hierarchy and the members to the new axis
				logger.debug("Adding the hierarchy in the axis " + toAxisPos);
				ph.addHierarchy(CubeUtilities.getAxis(toAxisPos), hierarchy, false, -1);
				logger.debug("Hierarchy added");
			}
			if (membersToMove != null) {

				logger.debug("Moving the members to the axis " + fromAxisPos);

				// If the dimension is a measure we should add the dimension,
				// cleans it (because it adds all the root members, so all the
				// measures), and add the visible measures
				try {
					if (hierarchy.getDimension().getDimensionType().equals(Dimension.Type.MEASURE)) {
						List<Member> membersToRemove = new ArrayList<Member>();
						List<Member> rootMembers = pm.findVisibleMembers(hierarchy);
						for (int i = 0; i < rootMembers.size(); i++) {
							Member member = rootMembers.get(i);
							if (!membersToMove.contains(member)) {
								membersToRemove.add(member);
							}
						}
						logger.debug("Removing the measures from the hierarchy");
						pm.removeMembers(hierarchy, membersToRemove);
						logger.debug("Measures removed");
					}
				} catch (OlapException e) {
					logger.error("Error cleaning the measure", e);
				}
				if (toAxisPos >= 0) {
					logger.debug("Adding the members to the axis");
					pm.addMembers(hierarchy, membersToMove);
					logger.debug("Members moved");
				}

			}

		}

		logger.debug("OUT");
		return hierarchy;
	}

	/**
	 * method to move a hierarchy in the axis
	 *
	 * @param axisPos
	 *            the destination axis(0 for rows, 1 for columns, -1 for
	 *            filters)
	 * @param hierarchyName
	 *            the unique name of the hierarchy to move
	 * @param position
	 *            the new position of the hierarchy
	 * @param direction
	 *            the direction of the movement (-1 up, +1 down)
	 */
	public void moveHierarchy(int axisPos, String hierarchyName, int position, int direction) {
		logger.debug("IN");

		Hierarchy hierarchy = null;
		PlaceHierarchiesOnAxes ph = getModel().getTransform(PlaceHierarchiesOnAxes.class);

		try {
			logger.debug("getting the hierarchy object from the cube");
			hierarchy = CubeUtilities.getHierarchy(getModel().getCube(), hierarchyName);
		} catch (OlapException e) {
			logger.error("Error getting the hierrarchy " + hierarchyName + " from the cube ", e);
			throw new SpagoBIEngineRuntimeException("Error adding the hierrarchy " + hierarchyName + " in the axis " + axisPos, e);
		}

		logger.debug("Getting the hierarchies list from the axis");

		logger.debug("Moving the hierarchy " + hierarchyName);
		if (direction > 0) {
			// if the direction is positive we should a 1 because the hierarchy
			// we want to move still be in the axis
			ph.moveHierarchy(CubeUtilities.getAxis(axisPos), hierarchy, position + 1);
		} else {
			ph.moveHierarchy(CubeUtilities.getAxis(axisPos), hierarchy, position);
		}
		logger.debug("Hierarchy moved");

		logger.debug("OUT");
	}

	/**
	 * Changes the visibility of the members of a hierarchy. It takes a
	 * hierarchy, removes all the members and shows only the ones passed in the
	 * body of the request
	 *
	 * @param hierarchy
	 *            hierarchy to update
	 * @param members
	 *            list of members to show
	 */
	public void updateAxisHierarchyMembers(Hierarchy hierarchy, List<Member> members) {

		PlaceMembersOnAxes pm = getModel().getTransform(PlaceMembersOnAxes.class);

		List<Member> visibleMembers = pm.findVisibleMembers(hierarchy);

		// add the first member..
		if (visibleMembers.contains(members.get(0))) {
			// if it's already visible dont remove it from the hierarchy
			visibleMembers.remove(members.get(0));
			members.remove(0);
		} else {
			// if it's not visible add the first member to the hierarchy
			List<Member> firtsMember = new ArrayList<Member>();
			firtsMember.add(members.remove(0));
			pm.addMembers(hierarchy, firtsMember);
		}
		// we've to do this because if we remove all the members from the
		// hierarchy, the hierarchy will be removed from the axis

		pm.removeMembers(hierarchy, visibleMembers);
		pm.addMembers(hierarchy, members);

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
	 * @return the new hierarchy
	 */
	public Hierarchy updateHierarchyOnAxis(int axisPos, String newHierarchyUniqueName, String oldHierarchyUniqueName, int hierarchyPosition) {
		logger.debug("IN");
		logger.debug("Updating the hierarchy in a dimension.. The new hierarchy is " + newHierarchyUniqueName + " the old one is " + oldHierarchyUniqueName);

		Hierarchy hierarchy = null;
		PlaceHierarchiesOnAxes ph = getModel().getTransform(PlaceHierarchiesOnAxes.class);
		Axis ax = CubeUtilities.getAxis(axisPos);

		try {
			logger.debug("getting the hierarchy object from the cube");
			hierarchy = CubeUtilities.getHierarchy(getModel().getCube(), oldHierarchyUniqueName);
		} catch (OlapException e) {
			logger.error("Error getting the hierrarchy " + oldHierarchyUniqueName + " from the cube ", e);
			throw new SpagoBIEngineRuntimeException("Error getting hierrarchy " + oldHierarchyUniqueName + " from the axis " + axisPos, e);
		}

		// removes the slicers
		logger.debug("Cleaning slicers");
		ChangeSlicer cs = getModel().getTransform(ChangeSlicer.class);
		List<org.olap4j.metadata.Member> slicers = cs.getSlicer(hierarchy);
		slicers.clear();
		cs.setSlicer(hierarchy, slicers);
		logger.debug("Slicers cleaned");

		if (axisPos >= 0) {// if it's not a filter

			logger.debug("Removing the old hierarchy " + oldHierarchyUniqueName + " from the axis " + axisPos);
			ph.removeHierarchy(ax, hierarchy);
			logger.debug("Hierarchy removed");

			try {
				logger.debug("getting the hierarchy object from the cube");
				hierarchy = CubeUtilities.getHierarchy(getModel().getCube(), newHierarchyUniqueName);
			} catch (OlapException e) {
				logger.error("Error getting the hierrarchy " + newHierarchyUniqueName + " from the cube ", e);
				throw new SpagoBIEngineRuntimeException("Error getting hierrarchy " + newHierarchyUniqueName + " from the axis " + axisPos, e);
			}

			logger.debug("Adding a new hierarchy " + newHierarchyUniqueName + " in the axis " + axisPos);
			ph.addHierarchy(ax, hierarchy, false, hierarchyPosition);
			logger.debug("Hierarchy added");
		}

		logger.debug("OUT");
		return hierarchy;
	}

	public PivotModel getModel() {
		return model;
	}

	public void setModel(PivotModel model) {
		this.model = model;
	}

}
