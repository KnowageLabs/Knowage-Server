/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2023 Engineering Ingegneria Informatica S.p.A.

 * Knowage is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * Knowage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.

 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package it.eng.spagobi.api;

import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toSet;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.analiticalmodel.document.dao.IBIObjectDAO;
import it.eng.spagobi.api.dto.AbstractViewFolderItem;
import it.eng.spagobi.api.dto.ViewFolder;
import it.eng.spagobi.api.dto.ViewOfImportedDoc;
import it.eng.spagobi.api.dto.ViewOverDocument;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.metadata.SbiCommonInfo;
import it.eng.spagobi.user.UserProfileManager;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;
import it.eng.spagobi.view.dao.ISbiViewDAO;
import it.eng.spagobi.view.dao.ISbiViewForDocDAO;
import it.eng.spagobi.view.dao.ISbiViewHierarchyDAO;
import it.eng.spagobi.view.metadata.SbiView;
import it.eng.spagobi.view.metadata.SbiViewForDoc;
import it.eng.spagobi.view.metadata.SbiViewHierarchy;

/**
 * @author Marco Libanori
 * @since KNOWAGE_TM-513
 */
@Path("/1.0/repository")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class RepositoryResource extends AbstractSpagoBIResource {

	private static final Logger LOGGER = Logger.getLogger(RepositoryResource.class);

	@Inject
	private ObjectMapper mapper;

	@GET
	@Path("/")
	public ViewFolder getFolders() {
		ISbiViewHierarchyDAO dao = DAOFactory.getSbiViewHierarchyDAO();
		dao.setUserProfile(UserProfileManager.getProfile());
		Set<SbiViewHierarchy> allFolders = dao.readAllOwned();

		Deque<SbiViewHierarchy> l = new LinkedList<>(allFolders);
		Map<String, ViewFolder> m = new HashMap<>();

		// Create tree
		ViewFolder root = createRootNode();

		m.put(null, root);

		while (!l.isEmpty()) {
			Iterator<SbiViewHierarchy> it = l.iterator();

			while (it.hasNext()) {
				SbiViewHierarchy curr = it.next();
				SbiViewHierarchy parent = curr.getParent();
				String parentId = parent != null ? parent.getId() : null;

				if (m.containsKey(parentId)) {
					it.remove();

					ViewFolder currAsViewFolder = toViewFolder(curr);

					m.get(parentId).getChildren().add(currAsViewFolder);
					m.put(currAsViewFolder.getId(), currAsViewFolder);
				}
			}
		}

		return root;
	}

	@GET
	@Path("/{id}")
	public FolderContentsResponse getFolderContents(@PathParam("id") String id) throws JsonProcessingException {
		FolderContentsResponse ret = new FolderContentsResponse();

		SbiViewHierarchy vh = getFolder(id);

		Consumer<AbstractViewFolderItem> addToRet = e -> ret.getContent().add(e);

		Set<SbiView> v1 = getViewsInFolder(vh);
		Set<SbiViewForDoc> v2 = getImportedDocsInFolder(vh);

		v1.stream().map(this::toViewOverDocument).forEach(addToRet);

		v2.stream().map(this::toViewOfImportedDoc).forEach(addToRet);

		return ret;
	}

	@POST
	@Path("/")
	public ViewFolder createFolder(ViewFolder e) {
		ISbiViewHierarchyDAO dao = DAOFactory.getSbiViewHierarchyDAO();
		dao.setUserProfile(UserProfileManager.getProfile());

		String parentId = e.getParentId();

		SbiViewHierarchy parent = null;

		if (nonNull(parentId)) {
			parent = getFolder(parentId);
		}

		SbiViewHierarchy v = new SbiViewHierarchy();

		v.setDescr(e.getDescription());
		v.setName(e.getName());
		v.setParent(parent);
		v.setProgr(e.getProgr());

		dao.create(v);

		return toViewFolder(v);
	}

	@PUT
	@Path("/")
	public ViewFolder updateFolder(ViewFolder e) {
		ISbiViewHierarchyDAO dao = DAOFactory.getSbiViewHierarchyDAO();
		dao.setUserProfile(UserProfileManager.getProfile());

		String parentId = e.getParentId();

		SbiViewHierarchy parent = null;

		if (nonNull(parentId)) {
			parent = getFolder(parentId);
		}

		SbiViewHierarchy v = getFolder(e.getId());

		v.setDescr(e.getDescription());
		v.setName(e.getName());
		v.setParent(parent);

		checkFolderReferencingItself(v);

		dao.update(v);

		return toViewFolder(v);
	}

	@DELETE
	@Path("/{id}")
	@Consumes(MediaType.WILDCARD)
	public ViewFolder deleteFolder(@PathParam("id") String id) {
		ISbiViewHierarchyDAO dao = DAOFactory.getSbiViewHierarchyDAO();
		dao.setUserProfile(UserProfileManager.getProfile());

		SbiViewHierarchy v = getFolder(id);

		checkFolderContainingSomething(v);
		checkFolderReferencingItself(v);

		dao.delete(v);

		return toViewFolder(v);
	}

	@GET
	@Path("/view/{id}")
	public ViewOverDocument readView(@PathParam("id") String id) {
		SbiView v = getView(id);

		return toViewOverDocument(v);
	}

	@POST
	@Path("/view")
	public ViewOverDocument createView(ViewOverDocument e) throws EMFUserError {
		ISbiViewDAO dao = DAOFactory.getSbiViewDAO();
		dao.setUserProfile(UserProfileManager.getProfile());

		String parentId = e.getParentId();
		Integer biObjectId = e.getBiObjectId();

		SbiViewHierarchy parent = getFolder(parentId);
		BIObject document = getDocument(biObjectId);

		SbiView v = new SbiView();

		v.setBiObjId(document.getId());
		v.setDescr(e.getDescription());
		v.setDrivers(e.getDrivers());
		v.setLabel(e.getLabel());
		v.setName(e.getName());
		v.setParent(parent);
		v.setSettings(e.getSettings());

		dao.create(v);

		return toViewOverDocument(v);
	}

	@PUT
	@Path("/view")
	public ViewOverDocument updateView(ViewOverDocument e) {
		String id = e.getId();

		ISbiViewDAO dao = DAOFactory.getSbiViewDAO();
		dao.setUserProfile(UserProfileManager.getProfile());

		String parentId = e.getParentId();
		SbiViewHierarchy parent = getFolder(parentId);
		SbiView v = getView(id);

		// v.setBiObjId(e.getBiObjectId()) : no change to original document
		v.setDescr(e.getDescription());
		v.setDrivers(e.getDrivers());
		// v.setLabel(???) : no change to label
		v.setName(e.getName());
		v.setParent(parent);
		v.setSettings(e.getSettings());

		dao.update(v);

		return toViewOverDocument(v);
	}

	@DELETE
	@Path("/view/{id}")
	@Consumes(MediaType.WILDCARD)
	public ViewOverDocument deleteView(@PathParam("id") String id) {
		ISbiViewDAO dao = DAOFactory.getSbiViewDAO();
		dao.setUserProfile(UserProfileManager.getProfile());

		SbiView v = getView(id);
		dao.delete(v);

		return toViewOverDocument(v);
	}

	@GET
	@Path("/view/document/{id}")
	public Set<ViewOverDocument> getViewsOnDoc(@PathParam("id") int biObjectId) throws EMFUserError {
		BIObject document = getDocument(biObjectId);

		ISbiViewDAO dao = DAOFactory.getSbiViewDAO();
		dao.setUserProfile(UserProfileManager.getProfile());

		Set<SbiView> ownedAndPublicViewsOverDoc = dao.getOwnedAndPublicViewsOverDoc(document);

		return ownedAndPublicViewsOverDoc.stream().map(this::toViewOverDocument).collect(toSet());
	}

	@GET
	@Path("/document/{id}")
	public ViewOfImportedDoc readImportedDoc(@PathParam("id") String id) {
		SbiViewForDoc v = getViewForDoc(id);

		return toViewOfImportedDoc(v);
	}

	@POST
	@Path("/document")
	public ViewOfImportedDoc importDoc(ViewOfImportedDoc e) throws EMFUserError {
		ISbiViewForDocDAO dao = DAOFactory.getSbiViewForDocDAO();
		dao.setUserProfile(UserProfileManager.getProfile());

		String parentId = e.getParentId();
		Integer biObjectId = e.getBiObjectId();

		SbiViewHierarchy parent = getFolder(parentId);
		BIObject document = getDocument(biObjectId);

		SbiViewForDoc v = new SbiViewForDoc();

		v.setBiObjId(document.getId());
		v.setParent(parent);

		dao.create(v);

		return toViewOfImportedDoc(v);
	}

	@PUT
	@Path("/document")
	public ViewOfImportedDoc updateImportedDoc(ViewOfImportedDoc e) {
		String id = e.getId();

		ISbiViewForDocDAO dao = DAOFactory.getSbiViewForDocDAO();
		dao.setUserProfile(UserProfileManager.getProfile());

		String parentId = e.getParentId();
		SbiViewHierarchy parent = getFolder(parentId);
		SbiViewForDoc v = getViewForDoc(id);

		v.setParent(parent);

		dao.update(v);

		return toViewOfImportedDoc(v);
	}

	@DELETE
	@Path("/document/{id}")
	@Consumes(MediaType.WILDCARD)
	public ViewOfImportedDoc deleteImportedDoc(@PathParam("id") String id) {
		ISbiViewForDocDAO dao = DAOFactory.getSbiViewForDocDAO();
		dao.setUserProfile(UserProfileManager.getProfile());

		SbiViewForDoc v = getViewForDoc(id);
		dao.delete(v);

		return toViewOfImportedDoc(v);
	}

	private ViewOverDocument toViewOverDocument(SbiView e) {
		ViewOverDocument ret = new ViewOverDocument();
		SbiCommonInfo commonInfo = e.getCommonInfo();
		Instant created = Optional.ofNullable(commonInfo.getTimeIn()).map(this::toZonedDateTime).orElse(null);
		Instant updated = Optional.ofNullable(commonInfo.getTimeUp()).map(this::toZonedDateTime).orElse(null);
		SbiViewHierarchy parent = e.getParent();
		String parentId = parent.getId();

		try {
			BIObject object = getDocument(e.getBiObjId());

			ret.setBiObjectId(object.getId());
			ret.setBiObjectTypeCode(object.getBiObjectTypeCode());
			ret.setDocumentLabel(object.getLabel());
			ret.setDescription(e.getDescr());
			ret.setDrivers(e.getDrivers());
			ret.setId(e.getId());
			ret.setLabel(e.getLabel());
			ret.setName(e.getName());
			ret.setSettings(e.getSettings());
			ret.setCreated(created);
			ret.setUpdated(updated);
			ret.setParentId(parentId);
		} catch (EMFUserError e1) {
			throw new SpagoBIRuntimeException(e1);
		}

		return ret;
	}

	private ViewOfImportedDoc toViewOfImportedDoc(SbiViewForDoc e) {
		ViewOfImportedDoc ret = new ViewOfImportedDoc();
		SbiCommonInfo commonInfo = e.getCommonInfo();
		Instant created = Optional.ofNullable(commonInfo.getTimeIn()).map(this::toZonedDateTime).orElse(null);
		Instant updated = Optional.ofNullable(commonInfo.getTimeUp()).map(this::toZonedDateTime).orElse(null);
		SbiViewHierarchy parent = e.getParent();
		String parentId = parent.getId();
		try {
			BIObject object = getDocument(e.getBiObjId());

			ret.setBiObjectId(object.getId());
			ret.setBiObjectTypeCode(object.getBiObjectTypeCode());
			ret.setId(e.getId());
			ret.setParentId(parentId);
			ret.setDescription(object.getDescription());
			ret.setLabel(object.getLabel());
			ret.setName(object.getName());
			ret.setCreated(created);
			ret.setUpdated(updated);
		} catch (EMFUserError e1) {
			throw new SpagoBIRuntimeException(e1);
		}

		return ret;
	}

	private ViewFolder createRootNode() {
		ViewFolder root = new ViewFolder();

		root.setId(null);
		root.setName("root");
		root.setDescription("root");
		root.setProgr(-1);
		return root;
	}

	private ViewFolder toViewFolder(SbiViewHierarchy e) {
		ViewFolder ret = new ViewFolder();
		SbiViewHierarchy parent = e.getParent();
		SbiCommonInfo commonInfo = e.getCommonInfo();
		Instant created = Optional.ofNullable(commonInfo.getTimeIn()).map(Date::toInstant).orElse(null);
		Instant updated = Optional.ofNullable(commonInfo.getTimeUp()).map(Date::toInstant).orElse(null);

		ret.setId(e.getId());
		ret.setName(e.getName());
		ret.setDescription(e.getDescr());
		ret.setProgr(e.getProgr());
		ret.setCreated(created);
		ret.setUpdated(updated);

		if (nonNull(parent)) {
			ret.setParentId(parent.getId());
		}

		return ret;
	}

	private SbiViewHierarchy getFolder(String id) {
		SbiViewHierarchy ret = null;

		ISbiViewHierarchyDAO dao = DAOFactory.getSbiViewHierarchyDAO();
		dao.setUserProfile(UserProfileManager.getProfile());

		ret = dao.read(id);

		if (Objects.isNull(ret)) {
			throw new SpagoBIRuntimeException("Folder with following id doesn't exist: " + id);
		}

		return ret;
	}

	private BIObject getDocument(Integer id) throws EMFUserError {
		BIObject ret = null;

		IBIObjectDAO dao = DAOFactory.getBIObjectDAO();
		dao.setUserProfile(UserProfileManager.getProfile());

		ret = dao.loadBIObjectById(id);

		if (Objects.isNull(ret)) {
			throw new SpagoBIRuntimeException("Document with following id doesn't exist: " + id);
		}

		return ret;
	}

	private Set<SbiViewForDoc> getImportedDocsInFolder(SbiViewHierarchy vh) {
		ISbiViewForDocDAO dao2 = DAOFactory.getSbiViewForDocDAO();
		dao2.setUserProfile(UserProfileManager.getProfile());

		return dao2.readByViewHierarchyId(vh.getId());
	}

	private Set<SbiView> getViewsInFolder(SbiViewHierarchy vh) {
		ISbiViewDAO dao1 = DAOFactory.getSbiViewDAO();
		dao1.setUserProfile(UserProfileManager.getProfile());

		return dao1.readByViewHierarchyId(vh.getId());
	}

	private void checkFolderContainingSomething(SbiViewHierarchy vh) {
		if (folderHasSubFolders(vh) || folderContainsSomething(vh)) {
			throw new SpagoBIRuntimeException("Folder with following id is not empty: " + vh.getId());
		}
	}

	private boolean folderHasSubFolders(SbiViewHierarchy vh) {
		return !vh.getChildren().isEmpty();
	}

	private void checkFolderReferencingItself(SbiViewHierarchy vh) {
		SbiViewHierarchy parent = vh.getParent();
		if (nonNull(vh.getId()) && nonNull(parent) && vh.getId().equals(parent.getId())) {
			throw new SpagoBIRuntimeException("Folder with following id cannot have itself as parent: " + vh.getId());
		}
	}

	private boolean folderContainsSomething(SbiViewHierarchy vh) {
		boolean ret = false;

		Set<SbiView> s1 = getViewsInFolder(vh);
		if (!s1.isEmpty()) {
			ret = true;
		}

		Set<SbiViewForDoc> s2 = getImportedDocsInFolder(vh);
		if (!ret && !s2.isEmpty()) {
			ret = true;
		}

		return ret;
	}

	private SbiView getView(String id) {
		ISbiViewDAO dao = DAOFactory.getSbiViewDAO();
		dao.setUserProfile(UserProfileManager.getProfile());

		SbiView ret = null;

		try {
			ret = dao.read(id);
		} catch (Exception e) {
			throw new SpagoBIRuntimeException("View with following id doesn't exist: " + id, e);
		}

		return ret;
	}

	private SbiViewForDoc getViewForDoc(String id) {
		ISbiViewForDocDAO dao = DAOFactory.getSbiViewForDocDAO();
		dao.setUserProfile(UserProfileManager.getProfile());

		SbiViewForDoc ret = null;

		try {
			ret = dao.read(id);
		} catch (Exception e) {
			throw new SpagoBIRuntimeException("View with following id doesn't exist: " + id, e);
		}

		return ret;
	}

	private Instant toZonedDateTime(Date date) {
		// @formatter:off
		return Optional.of(date)
				.map(Date::toInstant)
				.orElse(null);
		// @formatter:on
	}

}

/**
 * This is a workaround to a problem between JAX-RS and lists.
 *
 * The method {@link RepositoryResource#getFolderContents(String)} returned a <code>List<AbstractViewFolderItem></code> but someone between Resteasy or Jackson
 * didn't use to apply the correct serialization to every object in the list. Using a wrapper like this seems to solve the problem.
 *
 * I was not able to find a solution or documentation about this behavior.
 */
class FolderContentsResponse {

	private final List<AbstractViewFolderItem> content = new ArrayList<>();

	/**
	 * @return the content
	 */
	public List<AbstractViewFolderItem> getContent() {
		return content;
	}

}