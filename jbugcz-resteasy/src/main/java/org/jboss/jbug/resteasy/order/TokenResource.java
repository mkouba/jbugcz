package org.jboss.jbug.resteasy.order;

import static javax.ws.rs.core.MediaType.TEXT_PLAIN;

import javax.inject.Inject;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.HttpHeaders;

import org.slf4j.Logger;

/**
 * Note that since we use EE 6 the resteasy-cdi module asks the CDI container
 * for the managed instance of a JAX-RS component instead of creating a new
 * instance.
 * 
 * @author Martin Kouba
 */
@Path("/token")
public class TokenResource {

	@Inject
	private Logger logger;

	@Inject
	private TokenStore tokenStore;

	@POST
	@Produces(TEXT_PLAIN)
	public String getToken(@QueryParam("password") String secret, @HeaderParam(HttpHeaders.USER_AGENT) String userAgent) {
		logger.info("Get new token for {}]", userAgent);
		return tokenStore.generateNewToken(secret);
	}

}
