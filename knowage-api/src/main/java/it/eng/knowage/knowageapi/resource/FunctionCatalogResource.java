/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2021 Engineering Ingegneria Informatica S.p.A.
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
package it.eng.knowage.knowageapi.resource;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.PATCH;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import it.eng.knowage.knowageapi.resource.dto.FunctionCompleteDTO;
import it.eng.knowage.knowageapi.resource.dto.FunctionDTO;
import it.eng.knowage.knowageapi.service.FunctionCatalogAPI;

/**
 * @author Marco Libanori
 */
@Path("/1.0/functioncatalog")
@Component
@Validated
public class FunctionCatalogResource {

	private static final Logger LOGGER = Logger.getLogger(FunctionCatalogResource.class);

	@Autowired
	private FunctionCatalogAPI api;

	@GET
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public List<FunctionDTO> getAll(@QueryParam("s") @DefaultValue("") String searchString) {
		try {
			return api.find(searchString);
		} catch (Exception e) {
			LOGGER.error("Error looking for function matching \"" + searchString + "\"", e);
			throw e;
		}
	}

	@GET
	@Path("/completelist")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public List<FunctionCompleteDTO> getAllComplete(@QueryParam("s") @DefaultValue("") String searchString) {
		try {
			return api.findComplete(searchString);
		} catch (Exception e) {
			LOGGER.error("Error looking for function matching \"" + searchString + "\"", e);
			throw e;
		}
	}

	@GET
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public FunctionCompleteDTO get(@PathParam("id") UUID id) {
		try {
			return api.get(id);
		} catch (Exception e) {
			LOGGER.error("Error getting function with id " + String.valueOf(id), e);
			throw e;
		}
	}

	@POST
	@Path("/")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public Response create(FunctionCompleteDTO function) {
		try {
			FunctionCompleteDTO create = api.create(function);
			return Response.ok(create).build();
		} catch (Exception e) {
			LOGGER.error("Error getting function with id " + Optional.ofNullable(function).map(FunctionCompleteDTO::getName).orElse("null"), e);
			return Response.serverError().build();
		}
	}

	@POST
	@Path("/new")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public Response createWithNewUuid(FunctionCompleteDTO function) {
		try {
			function.setId(UUID.randomUUID());
			FunctionCompleteDTO create = api.create(function);
			return Response.ok(create).build();
		} catch (Exception e) {
			LOGGER.error("Error getting function with id " + Optional.ofNullable(function).map(FunctionCompleteDTO::getName).orElse("null"), e);
			return Response.serverError().build();
		}
	}

	@DELETE
	@Path("/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response delete(@PathParam("id") UUID id) {
		try {
			api.delete(id);
			return Response.ok().build();
		} catch (Exception e) {
			LOGGER.error("Error deleting function with id " + String.valueOf(id), e);
			return Response.serverError().build();
		}
	}

	@PATCH
	@Path("/")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	public Response update(FunctionCompleteDTO function) {
		try {
			FunctionCompleteDTO ret = api.update(function);
			return Response.ok(ret).build();
		} catch (Exception e) {
			LOGGER.error("Error updating function with id " + Optional.ofNullable(function).map(FunctionCompleteDTO::getName).orElse("null"), e);
			throw e;
		}
	}

}
