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

package it.eng.knowage.rest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.Provider;

import it.eng.knowage.rest.annotation.FeatureFlag;

/**
 *
 */
@Provider
@Priority(Priorities.AUTHORIZATION)
public class FeatureFlagProvider implements ContainerRequestFilter {

	@Context
	private ResourceInfo resourceInfo;

	@Override
	public void filter(ContainerRequestContext requestContext) throws IOException {
		FeatureFlag annotation = resourceInfo.getResourceMethod().getAnnotation(FeatureFlag.class);
		if (annotation != null && !isFeatureEnabled(annotation)) {
			Map<String, Object> response = new HashMap<>();

			List<Map<String, Object>> messages = new ArrayList<>();
			Map<String, Object> message = new HashMap<>();
			message.put("message", "not-enabled-to-call-service");
			messages.add(message);

			response.put("errors", messages);

			requestContext.abortWith(
					Response.status(Status.FORBIDDEN).type(MediaType.APPLICATION_JSON).entity(response).build());
		}

	}

	private boolean isFeatureEnabled(FeatureFlag annotation) {

		String name = annotation.value();

		String fromProperty = System.getProperty(name);
		String fromEnv = System.getenv(name);

		return Optional.ofNullable(fromProperty).map(Boolean::parseBoolean)
				.orElse(Optional.ofNullable(fromEnv).map(Boolean::parseBoolean).orElse(true));

	}
}
