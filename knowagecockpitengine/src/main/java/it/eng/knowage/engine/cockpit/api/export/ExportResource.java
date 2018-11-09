/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2018 Engineering Ingegneria Informatica S.p.A.
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

package it.eng.knowage.engine.cockpit.api.export;

import it.eng.knowage.engine.cockpit.api.AbstractCockpitEngineResource;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.exceptions.SpagoBIRestServiceException;
import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;

@Path("/1.0/export")
public class ExportResource extends AbstractCockpitEngineResource {

    private static final Logger logger = Logger.getLogger(ExportResource.class);

    @POST
    @Path("/")
    @Produces("image/png")
    public Response getPngImageFromBase64(String body) {
        try {
            Assert.assertNotBlank(body, "Body message must be provided");
            JSONObject jsonBody = new JSONObject(body);

            String img64 = jsonBody.optString("image");
            Assert.assertNotBlank(img64, "Base64 image must be provided");

            String filename = jsonBody.optString("name");
            Assert.assertNotBlank(filename, "Image filename must be provided");

            Response.ResponseBuilder response = Response.ok(Base64.decodeBase64(img64));
            response.header("Content-Disposition", "attachment;filename=" + filename + ".png");
            return response.build();
        } catch (JSONException e) {
            throw new SpagoBIRestServiceException(buildLocaleFromSession(), e);
        }
    }
}
