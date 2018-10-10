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
package it.eng.spagobi.services.rest.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation must be used as a flag for public services, i.e. for services that do not require authentication. In case the service is NOT annotated, it
 * means that the user must be authenticated in some way.
 *
 * @see it.eng.spagobi.services.rest.AbstractSecurityServerInterceptor#filter(javax.ws.rs.container.ContainerRequestContext)
 *
 * @author Davide Zerbetto (davide.zerbetto@eng.it)
 */

@Retention(RetentionPolicy.RUNTIME)
// can use in method only.
@Target(ElementType.METHOD)
public @interface PublicService {
}
