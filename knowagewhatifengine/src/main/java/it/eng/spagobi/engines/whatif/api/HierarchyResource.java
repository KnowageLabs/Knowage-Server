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
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

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
import org.pivot4j.transform.PlaceMembersOnAxes;
import org.pivot4j.transform.SwapAxes;

import it.eng.spagobi.engines.whatif.WhatIfEngineInstance;
import it.eng.spagobi.engines.whatif.common.AbstractWhatIfEngineService;
import it.eng.spagobi.engines.whatif.common.WhatIfConstants;
import it.eng.spagobi.engines.whatif.cube.CubeUtilities;
import it.eng.spagobi.engines.whatif.hierarchy.FilterTreeBuilder;
import it.eng.spagobi.engines.whatif.hierarchy.NodeFilter;
import it.eng.spagobi.engines.whatif.member.SbiMember;
import it.eng.spagobi.engines.whatif.model.SpagoBIPivotModel;
import it.eng.spagobi.engines.whatif.model.transform.slicer.SlicerManager;
import it.eng.spagobi.engines.whatif.version.SbiVersion;
import it.eng.spagobi.engines.whatif.version.VersionDAO;
import it.eng.spagobi.services.rest.annotations.ManageAuthorization;
import it.eng.spagobi.tools.datasource.bo.IDataSource;
import it.eng.spagobi.utilities.engines.SpagoBIEngineRuntimeException;
import it.eng.spagobi.utilities.exceptions.SpagoBIEngineRestServiceRuntimeException;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;
import it.eng.spagobi.utilities.rest.RestUtilities;

@Path("/1.0/hierarchy")
@ManageAuthorization

public class HierarchyResource extends AbstractWhatIfEngineService {

	public static transient Logger logger = Logger.getLogger(HierarchyResource.class);

	@POST
	@Path("/slice")
	// @Produces("text/html; charset=UTF-8")
	public String addSlicer(@javax.ws.rs.core.Context HttpServletRequest req) {

		String hierarchyUniqueName = null;
		List<String> memberUniqueNames = new ArrayList<>();
		SlicerManager slicerManager = new SlicerManager(getWhatIfEngineInstance().getSpagoBIPivotModel());

		try {
			JSONObject paramsObj = RestUtilities.readBodyAsJSONObject(req);

			hierarchyUniqueName = paramsObj.getString("hierarchy");
			JSONArray jSONArray = paramsObj.optJSONArray("members");
			memberUniqueNames = new ArrayList<>();
			for (int i = 0; i < jSONArray.length(); i++) {
				memberUniqueNames.add(jSONArray.getString(i));
			}

		} catch (Exception e) {
			logger.error("Error reading body", e);
		}

		slicerManager.clearSlicers(hierarchyUniqueName);
		slicerManager.setSlicers(hierarchyUniqueName, memberUniqueNames);

		return renderModel(getWhatIfEngineInstance().getPivotModel());
	}

	@POST
	@Path("/filtertree")
	@Produces("text/html; charset=UTF-8")

	public String getMemberValue(@javax.ws.rs.core.Context HttpServletRequest req) {
		Hierarchy hierarchy = null;
		WhatIfEngineInstance ei = getWhatIfEngineInstance();
		PivotModel model = ei.getPivotModel();
		SwapAxes transform = model.getTransform(SwapAxes.class);

		List<Member> list = new ArrayList<Member>();
		List<Member> visibleMembers = null;
		String memberDescription;

		String hierarchyUniqueName = null;
		int axis = -2;
		String node = null;

		try {
			JSONObject paramsObj = RestUtilities.readBodyAsJSONObject(req);

			hierarchyUniqueName = paramsObj.getString("hierarchy");
			node = paramsObj.getString("node");
			axis = paramsObj.getInt("axis");

			if (transform.isSwapAxes()) {
				axis = axis == 0 ? 1 : 0;
			}
		} catch (Exception e) {
			logger.error("Error reading body", e);
		}

		visibleMembers = getVisibleMembers(axis, hierarchyUniqueName);

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
			try {
				versions = getVersions();
			} catch (Throwable e) {
				logger.error(e);
			}
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

				members.add(new SbiMember(aMember, visibleMembers.contains(aMember), memberDescription));

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
	@Path("/{hierarchy}/filtertree2/{axis}")
	@Produces("text/html; charset=UTF-8")

	public List<NodeFilter> getMemberValue2(@javax.ws.rs.core.Context HttpServletRequest req, @PathParam("hierarchy") String hierarchyUniqueName,
			@PathParam("axis") int axis) {
		Hierarchy hierarchy = null;

		List<Member> list = new ArrayList<Member>();

		try {
			hierarchy = CubeUtilities.getHierarchy(getPivotModel().getCube(), hierarchyUniqueName);

			List<NodeFilter> nodes = new ArrayList<NodeFilter>();
			Level l = hierarchy.getLevels().get(0);

			list = l.getMembers();
			for (int i = 0; i < list.size(); i++) {
				nodes.add(new NodeFilter(list.get(i)));
			}

			return nodes;
		} catch (Exception e1) {
			logger.error("Error while getting filter tree", e1);
			throw new SpagoBIRuntimeException("Error while getting filter tree", e1);
		}

	}

	@POST
	@Path("/slicerTree")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public List<NodeFilter> getSlicerTree(Map<String, String> params) {

		try {
			SlicerManager slicerManager = new SlicerManager((SpagoBIPivotModel) getPivotModel());

			FilterTreeBuilder ftb = new FilterTreeBuilder();
			ftb.setHierarchy(getHierarchy(params.get("hierarchyUniqueName")));
			ftb.setTreeLeaves(slicerManager.getSlicers(params.get("hierarchyUniqueName")));
			ftb.setVisibleMembers(slicerManager.getSlicers(params.get("hierarchyUniqueName")));
			ftb.setShowSiblings(false);
			return ftb.build();

		} catch (Exception e1) {
			logger.error("Error while getting slicer tree", e1);
			throw new SpagoBIRuntimeException("Error while getting slicer tree", e1);
		}

	}

	@POST
	@Path("/visibleMembers")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public List<NodeFilter> findVisibleMembers(Map<String, String> params) {

		String hierarchy = params.get("hierarchy");
		List<Member> visibleMembers = null;

		try {
			PlaceMembersOnAxes pm = getPivotModel().getTransform(PlaceMembersOnAxes.class);
			visibleMembers = pm.findVisibleMembers(CubeUtilities.getHierarchy(getPivotModel().getCube(), hierarchy));

			FilterTreeBuilder ftb = new FilterTreeBuilder();
			ftb.setHierarchy(getHierarchy(hierarchy));
			ftb.setTreeLeaves(visibleMembers);
			ftb.setVisibleMembers(visibleMembers);
			ftb.setShowSiblings(false);
			return ftb.build();

		} catch (Exception e) {
			logger.error("Error while getting visible members of a hierarchy", e);
			throw new SpagoBIRuntimeException("Error while getting visible members of a hierarchy", e);
		}

	}

	@POST
	@Path("/getvisible")
	public String getVisibleMembers(@javax.ws.rs.core.Context HttpServletRequest req) {

		int axis = -2;
		String hier = "";
		List<Member> visibleMembers = null;

		WhatIfEngineInstance ei = getWhatIfEngineInstance();
		PivotModel model = ei.getPivotModel();

		try {
			JSONObject paramsObj = RestUtilities.readBodyAsJSONObject(req);

			axis = paramsObj.getInt("axis");
			hier = paramsObj.getString("hierarchy");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// if not a filter axis
		if (axis >= 0) {
			PlaceMembersOnAxes pm = model.getTransform(PlaceMembersOnAxes.class);
			try {
				visibleMembers = pm.findVisibleMembers(CubeUtilities.getHierarchy(model.getCube(), hier));
			} catch (OlapException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
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

	@POST
	@Path("/search")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public List<NodeFilter> searchMembersByName(SeachParameters seachParameter) {

		try {
			Hierarchy hierarchy = getHierarchy(seachParameter.getHierarchyUniqueName());

			FilterTreeBuilder ftb = new FilterTreeBuilder();
			List<Member> treeLeaves = CubeUtilities.findMembersByName(hierarchy, seachParameter.getName(), false);
			List<Member> visibleMembers = getVisibleMembers(seachParameter.getAxis(), seachParameter.getHierarchyUniqueName());
			ftb.setHierarchy(getHierarchy(seachParameter.getHierarchyUniqueName()));
			ftb.setTreeLeaves(treeLeaves);
			ftb.setVisibleMembers(visibleMembers);
			ftb.setShowSiblings(seachParameter.isShowSiblings());
			return ftb.build();

		} catch (Exception e1) {
			logger.error("Error while searching members", e1);
			throw new SpagoBIRuntimeException("Error while searching members", e1);
		}

	}

	/**
	 * @param hierarchyUniqueName
	 * @return
	 * @throws OlapException
	 */
	private Hierarchy getHierarchy(String hierarchyUniqueName) throws OlapException {
		return CubeUtilities.getHierarchy(getPivotModel().getCube(), hierarchyUniqueName);
	}

	private List<SbiVersion> getVersions() {
		Connection connection;
		List<SbiVersion> versions = new ArrayList<SbiVersion>();
		IDataSource dataSource = getWhatIfEngineInstance().getDataSource();
		try {
			logger.debug("Getting the connection to DB");
			connection = dataSource.getConnection();
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

	/**
	 * @param axis
	 */
	private List<Member> getVisibleMembers(int axis, String hierarchyUniqueName) {
		List<Member> visibleMembers = new ArrayList<>();
		if (axis >= 0) {
			PlaceMembersOnAxes pm = getPivotModel().getTransform(PlaceMembersOnAxes.class);
			visibleMembers.addAll(pm.findVisibleMembers(CubeUtilities.getAxis(axis)));
		} else {
			SlicerManager sm = new SlicerManager((SpagoBIPivotModel) getPivotModel());
			visibleMembers.addAll(sm.getSlicers(hierarchyUniqueName));
		}

		return visibleMembers;

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
