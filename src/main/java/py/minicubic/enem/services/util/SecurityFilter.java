package py.minicubic.enem.services.util;

import java.io.IOException;
import javax.annotation.Priority;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.MediaType;

/**
 *
 * @author xergio
 */
@Provider
@Priority(Priorities.AUTHENTICATION)
public class SecurityFilter implements ContainerRequestFilter, ContainerResponseFilter {

    public void filter(ContainerRequestContext requestContext) throws IOException {
        
//        // Get the HTTP Authorization header from the request
//        String authHeader = requestContext.getHeaderString(HttpHeaders.AUTHORIZATION);
//
//        try {
//
//            // Check if the HTTP Authorization header is present and formatted correctly 
//            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
//                
//                throw new NotAuthorizedException(Constants.MSG_401);
//            }
//
//            // Extract the token from the HTTP Authorization header
//            String token = authHeader.substring("Bearer".length()).trim();
//
//            if (!Util.verifyToken(token)) {
//
//                throw new NotAuthorizedException(Constants.MSG_401);
//            } else {
//
//                // Disparamos el evento que creara el Objeto Datos Socios
//                loggedInEvent.fire(Long.valueOf(Util.getClaims(token).getSubject()));
//            }
//        } catch (Exception e) {
//            LoginResponse response = new LoginResponse(e.getMessage(), false);
//            requestContext.abortWith(Response.ok(response, MediaType.APPLICATION_JSON).build());
//        }
    }

    public void filter(ContainerRequestContext requestContext, ContainerResponseContext response) {
        response.getHeaders().putSingle("Access-Control-Allow-Origin", "*");
        response.getHeaders().putSingle("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE");
        response.getHeaders().putSingle("Access-Control-Allow-Headers", "Content-Type, Authorization");
    }
}
