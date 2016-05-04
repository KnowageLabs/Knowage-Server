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

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.olap4j.OlapException;
import org.olap4j.metadata.Hierarchy;
import org.olap4j.metadata.Level;
import org.olap4j.metadata.Member;
import org.olap4j.metadata.NamedList;
import org.olap4j.metadata.Property.StandardMemberProperty;
import org.pivot4j.PivotModel;
import org.pivot4j.transform.ChangeSlicer;
import org.pivot4j.transform.PlaceMembersOnAxes;

import it.eng.spagobi.engines.whatif.WhatIfEngineConfig;
import it.eng.spagobi.engines.whatif.WhatIfEngineInstance;
import it.eng.spagobi.engines.whatif.common.AbstractWhatIfEngineService;
import it.eng.spagobi.engines.whatif.common.WhatIfConstants;
import it.eng.spagobi.engines.whatif.cube.CubeUtilities;
import it.eng.spagobi.engines.whatif.member.SbiMember;
import it.eng.spagobi.engines.whatif.version.SbiVersion;
import it.eng.spagobi.engines.whatif.version.VersionDAO;
import it.eng.spagobi.tools.datasource.bo.IDataSource;
import it.eng.spagobi.utilities.engines.SpagoBIEngineRuntimeException;
import it.eng.spagobi.utilities.exceptions.SpagoBIEngineRestServiceRuntimeException;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

@Path("/1.0/hierarchy")
public class HierarchyResource extends AbstractWhatIfEngineService {

	private static final String NODE_PARM = "node";
	public static transient Logger logger = Logger.getLogger(HierarchyResource.class);

	@GET
	@Path("{hierarchy}/slice/{member}/{multi}")
	@Produces("text/html; charset=UTF-8")
	public String addSlicer(@javax.ws.rs.core.Context HttpServletRequest req, @PathParam("hierarchy") String hierarchyName,
			@PathParam("member") String memberName, @PathParam("multi") boolean multiSelection) {

		WhatIfEngineInstance ei = getWhatIfEngineInstance();
		PivotModel model = ei.getPivotModel();
		Hierarchy hierarchy = null;
		Member member = null;

		ChangeSlicer ph = model.getTransform(ChangeSlicer.class);

		try {
			hierarchy = CubeUtilities.getHierarchy(model.getCube(), hierarchyName);
			member = CubeUtilities.getMember(hierarchy, memberName);
		} catch (OlapException e) {
			logger.debug("Error getting the member " + memberName + " from the hierarchy " + hierarchyName, e);
			throw new SpagoBIEngineRuntimeException("Error getting the member " + memberName + " from the hierarchy " + hierarchyName, e);
		}

		List<org.olap4j.metadata.Member> slicers = ph.getSlicer(hierarchy);

		if (!multiSelection) {
			slicers.clear();
		}

		slicers.add(member);
		ph.setSlicer(hierarchy, slicers);

		return renderModel(model);
	}

	@GET
	@Path("/{hierarchy}/filtertree/{axis}")
	@Produces("text/html; charset=UTF-8")
	public String getMemberValue(@javax.ws.rs.core.Context HttpServletRequest req, @PathParam("hierarchy") String hierarchyUniqueName,
			@PathParam("axis") int axis, @QueryParam(NODE_PARM) String node) {
		Hierarchy hierarchy = null;

		List<Member> list = new ArrayList<Member>();
		List<Member> visibleMembers = null;
		String memberDescription;

		WhatIfEngineInstance ei = getWhatIfEngineInstance();
		PivotModel model = ei.getPivotModel();

		// if not a filter axis
		if (axis >= 0) {
			PlaceMembersOnAxes pm = model.getTransform(PlaceMembersOnAxes.class);
			visibleMembers = pm.findVisibleMembers(CubeUtilities.getAxis(axis));
		}

		logger.debug("Getting the node path from the request");
		// getting the node path from request

		if (node == null) {
			logger.debug("no node path found in the request");
			return null;
		}
		logger.debug("The node path is " + node);

		logger.debug("Getting the hierarchy " + hierarchyUniqueName + " from the cube");
		try {
			NamedList<Hierarchy> hierarchies = model.getCube().getHierarchies();
			for (int i = 0; i < hierarchies.size(); i++) {
				String hName = hierarchies.get(i).getUniqueName();
				if (hName.equals(hierarchyUniqueName)) {
					hierarchy = hierarchies.get(i);
					break;
				}
			}
		} catch (Exception e) {
			logger.debug("Error getting the hierarchy " + hierarchy, e);
			throw new SpagoBIEngineRuntimeException("Error getting the hierarchy " + hierarchy, e);
		}

		try {

			if (CubeUtilities.isRoot(node)) {
				logger.debug("Getting the members of the first level of the hierarchy");
				Level l = hierarchy.getLevels().get(0);
				logger.debug("This is the root.. Returning the members of the first level of the hierarchy");
				logger.debug("OUT");
				list = l.getMembers();
			} else {
				logger.debug("getting the child members");
				Member m = CubeUtilities.getMember(hierarchy, node);
				if (m != null) {
					list = (List<Member>) m.getChildMembers();
				}

			}
		} catch (Exception e) {
			logger.debug("Error getting the member tree " + node, e);
			throw new SpagoBIEngineRuntimeException("Error getting the member tree " + node, e);
		}

		List<SbiMember> members = new ArrayList<SbiMember>();

		// If the tree contains also versions we need to resolve the description
		// of the version
		List<SbiVersion> versions = new ArrayList<SbiVersion>();
		boolean isVersionDimension = hierarchy.getDimension().getUniqueName().equals(WhatIfConstants.VERSION_DIMENSION_UNIQUENAME);
		if (isVersionDimension) {
			versions = getVersions();
		}

		for (int i = 0; i < list.size(); i++) {
			Member aMember = list.get(i);
			Boolean memberVisibleInTheSchema = true;
			memberDescription = "";
			try {
				memberVisibleInTheSchema = (Boolean) aMember.getPropertyValue(StandardMemberProperty.$visible);
			} catch (Exception e) {
				logger.error("impossible to load the property visible for the member " + aMember.getUniqueName());
			}
			if (memberVisibleInTheSchema == null || memberVisibleInTheSchema) {

				if (isVersionDimension) {
					for (int j = 0; j < versions.size(); j++) {
						if (versions.get(j).getId().toString().equals(aMember.getName())) {
							memberDescription = versions.get(j).getDescription();
						}
					}
				}

				// check the visible members
				if (visibleMembers != null && axis >= 0) {
					members.add(new SbiMember(aMember, visibleMembers.contains(aMember), memberDescription));
				} else {
					members.add(new SbiMember(aMember, true, memberDescription));
				}
			}
		}

		try {
			return serialize(members);
		} catch (Exception e) {
			logger.error("Error serializing the MemberEntry", e);
			throw new SpagoBIRuntimeException("Error serializing the MemberEntry", e);
		}

	}

	@GET
	@Path("/{hierarchy}/search/{axis}/{name}/{showS}")
	public String searchMemberByName(@javax.ws.rs.core.Context HttpServletRequest req, @PathParam("hierarchy") String hierarchyUniqueName,
			@PathParam("axis") int axis, @PathParam("name") String name, @PathParam("showS") boolean showS) {
		Hierarchy hierarchy = null;
		int nodeLimit = WhatIfEngineConfig.getInstance().getDepthLimit();
		nodeLimit = nodeLimit > 0 ? nodeLimit : Integer.MAX_VALUE;
		List<Integer> positionList = new ArrayList<Integer>();
		List<String> fatherNameList = new ArrayList<String>();
		List<Member> list = new ArrayList<Member>();
		List<Member> visibleMembers = null;

		int lastDepth = -1;
		WhatIfEngineInstance ei = getWhatIfEngineInstance();
		PivotModel model = ei.getPivotModel();

		if (axis >= 0) {
			PlaceMembersOnAxes pm = model.getTransform(PlaceMembersOnAxes.class);
			visibleMembers = pm.findVisibleMembers(CubeUtilities.getAxis(axis));
		}

		logger.debug("Getting the hierarchy " + hierarchyUniqueName + "from the cube");
		try {
			NamedList<Hierarchy> hierarchies = model.getCube().getHierarchies();
			for (int i = 0; i < hierarchies.size(); i++) {
				String hName = hierarchies.get(i).getUniqueName();
				if (hName.equals(hierarchyUniqueName)) {
					hierarchy = hierarchies.get(i);
					break;
				}
			}
		} catch (Exception e) {
			logger.debug("Error getting the hierarchy " + hierarchy, e);
			throw new SpagoBIEngineRuntimeException("Error getting the hierarchy " + hierarchy, e);
		}

		List<NodeFilter> nodes = new ArrayList<HierarchyResource.NodeFilter>();
		Level l = hierarchy.getLevels().get(0);
		try {
			String nameLower = name.toLowerCase();

			for (int j = 0; j < hierarchy.getLevels().size() && j < nodeLimit; j++) {
				l = hierarchy.getLevels().get(j);
				list = l.getMembers();
				positionList = new ArrayList<Integer>();
				for (int i = 0; i < list.size(); i++) {
					String currentNameLower = list.get(i).getName().toString().toLowerCase();
					if (currentNameLower.contains(nameLower)) {
						positionList.add(i);
						if (list.get(i).getParentMember() != null)
							fatherNameList.add(list.get(i).getParentMember().getUniqueName());
						lastDepth = j;
					}
				}
			}

			l = hierarchy.getLevels().get(0);
			list = l.getMembers();
			for (int i = 0; i < list.size(); i++) {
				nodes.add(new NodeFilter(list.get(i), lastDepth, fatherNameList, name, visibleMembers, showS));
			}

		} catch (OlapException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		JSONArray serializedobject = new JSONArray();

		for (Iterator iterator = nodes.iterator(); iterator.hasNext();) {
			NodeFilter nodeFilter = (NodeFilter) iterator.next();
			try {
				serializedobject.put(nodeFilter.serialize());
			} catch (JSONException e) { // TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		try {
			return serializedobject.toString();
		} catch (Exception e) {
			logger.error("Error serializing the MemberEntry", e);
			throw new SpagoBIRuntimeException("Error serializing the MemberEntry", e);
		}
	}

	@GET
	@Path("/{hierarchy}/filtertree2/{axis}")
	@Produces("text/html; charset=UTF-8")
	public String getMemberValue2(@javax.ws.rs.core.Context HttpServletRequest req, @PathParam("hierarchy") String hierarchyUniqueName,
			@PathParam("axis") int axis) {
		Hierarchy hierarchy = null;

		List<Member> list = new ArrayList<Member>();

		WhatIfEngineInstance ei = getWhatIfEngineInstance();
		PivotModel model = ei.getPivotModel();

		logger.debug("Getting the hierarchy " + hierarchyUniqueName + "from the cube");
		try {
			NamedList<Hierarchy> hierarchies = model.getCube().getHierarchies();
			for (int i = 0; i < hierarchies.size(); i++) {
				String hName = hierarchies.get(i).getUniqueName();
				if (hName.equals(hierarchyUniqueName)) {
					hierarchy = hierarchies.get(i);
					break;
				}
			}
		} catch (Exception e) {
			logger.debug("Error getting the hierarchy " + hierarchy, e);
			throw new SpagoBIEngineRuntimeException("Error getting the hierarchy " + hierarchy, e);
		}

		List<NodeFilter> nodes = new ArrayList<HierarchyResource.NodeFilter>();
		Level l = hierarchy.getLevels().get(0);
		//// System.out.println(hierarchy.getLevels().size());
		try {
			list = l.getMembers();
			for (int i = 0; i < list.size(); i++) {
				nodes.add(new NodeFilter(list.get(i)));
			}
		} catch (OlapException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		JSONArray serializedobject = new JSONArray();

		for (Iterator iterator = nodes.iterator(); iterator.hasNext();) {
			NodeFilter nodeFilter = (NodeFilter) iterator.next();
			try {
				serializedobject.put(nodeFilter.serialize());
			} catch (JSONException e) { // TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		try {
			return serializedobject.toString();
		} catch (Exception e) {
			logger.error("Error serializing the MemberEntry", e);
			throw new SpagoBIRuntimeException("Error serializing the MemberEntry", e);
		}

	}

	@GET
	@Path("/{hierarchy}/getvisible/{axis}")
	public String getVisibleMembers(@javax.ws.rs.core.Context HttpServletRequest req, @PathParam("hierarchy") String hierarchyUniqueName,
			@PathParam("axis") int axis) {

		Hierarchy hierarchy = null;

		List<Member> list = new ArrayList<Member>();
		List<Member> visibleMembers = null;
		String memberDescription;

		WhatIfEngineInstance ei = getWhatIfEngineInstance();
		PivotModel model = ei.getPivotModel();

		// if not a filter axis
		if (axis >= 0) {
			PlaceMembersOnAxes pm = model.getTransform(PlaceMembersOnAxes.class);
			visibleMembers = pm.findVisibleMembers(CubeUtilities.getAxis(axis));
		}

		JSONArray ja = new JSONArray();

		for (int i = 0; i < visibleMembers.size(); i++) {
			try {
				ja.put(serializeVisibleObject(visibleMembers.get(i)));
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		try {

			return ja.toString();
		} catch (Exception e) {
			logger.error("Error serializing visible members", e);
			throw new SpagoBIRuntimeException("Error serializing visible members", e);
		}
	}

	private class NodeFilter {
		private String id;
		private String name;
		private String uniqueName;
		private boolean collapsed;
		private boolean visible;
		private List<NodeFilter> children;

		public NodeFilter(Member m) throws OlapException {
			super();

			this.id = m.getUniqueName();
			this.uniqueName = m.getUniqueName();
			this.name = m.getCaption();
			this.visible = false;
			this.collapsed = false;
			this.children = new ArrayList<HierarchyResource.NodeFilter>();

			if (m != null) {
				List<Member> list = (List<Member>) m.getChildMembers();
				if (list != null && list.size() > 0) {
					for (int i = 0; i < list.size(); i++) {
						NodeFilter nf = new NodeFilter(list.get(i));
						children.add(nf);
					}
				}
			}
		}

		@SuppressWarnings("unused")
		public NodeFilter(Member m, int depth, int position, String fatherName, List<Member> visibleMembers, boolean showS) throws OlapException {
			super();
			if (visibleMembers != null) {
				this.visible = visibleMembers.contains(m);
			} else {
				this.visible = false;
			}

			this.id = m.getUniqueName();
			this.uniqueName = m.getUniqueName();
			this.name = m.getCaption();
			this.collapsed = false;
			this.children = new ArrayList<HierarchyResource.NodeFilter>();

			if (m.getDepth() <= depth) {
				List<Member> list = (List<Member>) m.getChildMembers();
				if (list != null && list.size() > 0) {
					this.collapsed = true;

					if (m.getDepth() == depth - 1 && !showS) {
						NodeFilter nf = new NodeFilter(list.get(position), depth, position, fatherName, visibleMembers, showS);
						children.add(nf);
					} else {
						for (int i = 0; i < list.size(); i++) {
							if (m.getDepth() == 0) {
								NodeFilter nf = new NodeFilter(list.get(i), depth, position, fatherName, visibleMembers, showS);
								children.add(nf);

							} else if (fatherName.contains(list.get(i).getParentMember().getUniqueName())) {
								NodeFilter nf = new NodeFilter(list.get(i), depth, position, fatherName, visibleMembers, showS);
								children.add(nf);
							}
						}
					}
				}
			}
		}

		@SuppressWarnings("unused")
		public NodeFilter(Member m, int depth, List<String> fatherNameList, String name, List<Member> visibleMembers, boolean showS) throws OlapException {

			super();
			if (visibleMembers != null) {
				this.visible = visibleMembers.contains(m);
			} else {
				this.visible = false;
			}

			this.id = m.getUniqueName();
			this.uniqueName = m.getUniqueName();
			this.name = m.getCaption();
			this.collapsed = false;
			this.children = new ArrayList<HierarchyResource.NodeFilter>();

			int curDepth = m.getDepth();
			if (curDepth <= depth) {
				List<Member> list = (List<Member>) m.getChildMembers();

				for (int i = 0; i < list.size(); i++) {
					String parentUN = list.get(i).getParentMember() != null ? list.get(i).getParentMember().getUniqueName() : "";
					if (list.get(i).getName().toLowerCase().contains(name.toLowerCase()) && !showS) {
						this.collapsed = true;
						children.add(new NodeFilter(list.get(i), depth, fatherNameList, name, visibleMembers, showS));
					} else if (fatherNameList.contains(parentUN) && showS) {
						this.collapsed = true;
						children.add(new NodeFilter(list.get(i), depth, fatherNameList, name, visibleMembers, showS));
					} else if (isPotentialChild(fatherNameList, list.get(i).getUniqueName())) {
						this.collapsed = true;
						children.add(new NodeFilter(list.get(i), depth, fatherNameList, name, visibleMembers, showS));
					}

				}

			}

		};

		public boolean isPotentialChild(List<String> fatherNameList, String uniqueName) {
			for (int i = 0; i < fatherNameList.size(); i++) {
				if (fatherNameList.get(i).contains(uniqueName))
					return true;
			}

			return false;
		}

		public JSONObject serialize() throws JSONException {
			JSONObject obj = new JSONObject();
			obj.put("id", id);
			obj.put("name", name);
			obj.put("uniqueName", uniqueName);
			obj.put("collapsed", collapsed);
			obj.put("visible", visible);

			JSONArray children = new JSONArray();

			for (Iterator iterator = this.children.iterator(); iterator.hasNext();) {
				NodeFilter nodeFilter = (NodeFilter) iterator.next();
				children.put(nodeFilter.serialize());
			}

			obj.put("children", children);
			return obj;
		}
	}

	private List<SbiVersion> getVersions() {
		Connection connection;
		List<SbiVersion> versions = new ArrayList<SbiVersion>();
		IDataSource dataSource = getWhatIfEngineInstance().getDataSource();
		try {
			logger.debug("Getting the connection to DB");
			connection = dataSource.getConnection(null);
		} catch (Exception e) {
			logger.error("Error opening connection to datasource " + dataSource.getLabel());
			throw new SpagoBIRuntimeException("Error opening connection to datasource " + dataSource.getLabel(), e);
		}

		try {
			VersionDAO dao = new VersionDAO(getWhatIfEngineInstance());
			versions = dao.getAllVersions(connection);
		} catch (Exception e) {
			logger.debug("Error persisting the modifications", e);
			throw new SpagoBIEngineRuntimeException("Exception loading the versions", e);
		} finally {
			logger.debug("Closing the connection used to get the versions");
			try {
				connection.close();
			} catch (SQLException e) {
				logger.error("Error closing the connection to the db");
				throw new SpagoBIEngineRestServiceRuntimeException(getLocale(), e);
			}
			logger.debug("Closed the connection used to get the versions");
		}
		return versions;
	}

	private JSONObject serializeVisibleObject(Member m) throws JSONException {
		JSONObject obj = new JSONObject();
		obj.put("id", m.getUniqueName());
		obj.put("name", m.getName());
		obj.put("uniqueName", m.getUniqueName());
		obj.put("text", m.getName());
		obj.put("visible", true);

		return obj;
	}

}
