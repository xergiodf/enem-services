/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package py.minicubic.enem.services;

import java.util.List;
import java.util.logging.Logger;
import javax.ejb.Singleton;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.xml.ws.Response;
import py.minicubic.enem.services.dto.ResponseData;
import py.minicubic.enem.services.ejb.CiudadController;
import py.minicubic.enem.services.model.Ciudad;

/**
 *
 * @author hectorvillalba
 */

@Path("ciudadrest")
@Singleton
public class CiudadRest {
    @PersistenceContext
    private EntityManager em;
    
    @Inject
    private CiudadController controller;
    
    Logger log = Logger.getLogger("UsuariosRest");
    
    @Path("lista")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public ResponseData<List<Ciudad>> listaCiudades(){
        ResponseData<List<Ciudad>> response = new ResponseData<>();
        log.info("*** Lista Ciudades ***");
        try {
            response.setCodigo(200);
            response.setMensaje("Success");
            response.setData(controller.listaCiudad());
            log.info("Success");
            return response;
        } catch (Exception e) {
            log.warning("Error: " + e.getMessage());
            response.setCodigo(401);
            response.setMensaje(e.getMessage());
            response.setData(null);
            return response;
        }
    }
}
