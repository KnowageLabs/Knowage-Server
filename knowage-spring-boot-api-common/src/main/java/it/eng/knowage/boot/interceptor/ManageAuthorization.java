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
package it.eng.knowage.boot.interceptor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Alessandro Daniele (alessandro.daniele@eng.it)
 * @deprecated This annotation is completely useless. Use {@link it.eng.spagobi.services.rest.annotations.PublicService PublicService} for public services (i.e.
 *             services that do NOT require neither authentication nor authorization) and {@link it.eng.spagobi.services.rest.annotations.UserConstraint
 *             UserConstraint} for service that instead require both authentication and authorization. If both
 *             {@link it.eng.spagobi.services.rest.annotations.PublicService PublicService} and {@link it.eng.spagobi.services.rest.annotations.UserConstraint
 *             UserConstraint} are missing in a JAX-RS resource, it means that the service requires authentication but not authorization (i.e. all authenticated
 *             users can invoke thta service).
 * @see it.eng.spagobi.services.rest.AbstractSecurityServerInterceptor#filter(javax.ws.rs.container.ContainerRequestContext)
 *
 */
@Deprecated
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ManageAuthorization {

}
