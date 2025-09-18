package it.eng.spagobi.api.v3;


import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;

import it.eng.spagobi.services.common.JWTSsoServiceAlgorithmFactory;
import it.eng.spagobi.services.rest.annotations.PublicService;

@Path("/jwt")
public class JwtVerificationResource {

	private static final JWTSsoServiceAlgorithmFactory ALGORITHM_FACTORY = JWTSsoServiceAlgorithmFactory.getInstance();

	@POST
	@Path("/verify")
	@Produces(MediaType.TEXT_PLAIN)
	@PublicService
	public Response verifyToken(@HeaderParam("Authorization") String authHeader) {
		if (authHeader == null || !authHeader.startsWith("Bearer ")) {
			return Response.status(Response.Status.BAD_REQUEST).entity("Header Authorization missing or malformed").build();
		}

		String token = authHeader.substring("Bearer ".length());

		try {
			Algorithm algorithm = ALGORITHM_FACTORY.getAlgorithm();
			JWTVerifier verifier = JWT.require(algorithm).build();
			DecodedJWT jwt = verifier.verify(token);

			String userId = jwt.getClaim("user_id").asString();
			return Response.ok("Valid token for user_id: " + userId).build();

		} catch (JWTVerificationException e) {
			return Response.status(Response.Status.UNAUTHORIZED).entity("Invalid token: " + e.getMessage()).build();
		}
	}

}
