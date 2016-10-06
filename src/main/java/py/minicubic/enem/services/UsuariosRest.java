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
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import py.minicubic.enem.services.dto.PersonaDTO;
import py.minicubic.enem.services.dto.ResponseData;
import py.minicubic.enem.services.dto.UsuariosDTO;
import py.minicubic.enem.services.ejb.UsuariosController;
import py.minicubic.enem.services.model.Persona;
import py.minicubic.enem.services.model.Usuarios;

/**
 *
 * @author hectorvillalba
 */

@Path("usuariosrest")
@Singleton
public class UsuariosRest {
    
    @Inject
    private UsuariosController controller;
    @PersistenceContext
    private EntityManager em;
    Logger log = Logger.getLogger("UsuariosRest");
    
    @Path("login")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public ResponseData<List<Usuarios>> obtenerUsuario(UsuariosDTO dto){
        ResponseData<List<Usuarios>> response = new ResponseData<>();
        log.info("*** Obtener Ususario ***");
        log.info("Username: " + dto.getUsuario());
        try {
            List<Usuarios> lista = controller.getUsuarios(dto.getUsuario(), dto.getPassword());
            if(lista != null && !lista.isEmpty()){
                log.info("Login satisfactorio...");
                response.setCodigo(200);
                response.setMensaje("Success");
            }else{
                log.warning("Datos invalidos");
                response.setCodigo(401);
                response.setMensaje("No autorizado, autenticacion rechazada");
            }
        } catch (Exception e) {
            log.warning(e.getMessage());
            response.setCodigo(401);
            response.setMensaje("Error al intentar loguearse, autenticacion rechazada");
        }
        return response;
    }
    
    @Path("registrar")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public ResponseData<Usuarios> registrarUsuario(PersonaDTO dto){
        ResponseData<Usuarios> response = new ResponseData<>();
        try {
                log.info("*** Registrar Usuario ***");
                List<Persona> lista = controller.getSponsor(dto.getIdSponsor());
                if(lista.isEmpty()){
                    log.warning("Sponsor invalido");
                    response.setCodigo(301);
                    response.setMensaje("No existe el sponsor ingresado");
                    return response;
                }

                Usuarios usuarios = new Usuarios();
                usuarios.setEstado("NOACTIVO");
                usuarios.setPassword(dto.getPasword());
                usuarios.setUsername(dto.getUsername());
                em.persist(usuarios);
                log.info("Usuarios creado: " + usuarios.getUsername());

                Persona persona = new Persona();
                persona.setNombres(dto.getNombres());
                persona.setApellidos(dto.getApellidos());
                persona.setCelular(dto.getCelular());
                persona.setEmail(dto.getEmail());
                persona.setFechaNacimiento(dto.getFechaNacimiento());
                em.persist(persona);
                log.info("Persona creada: " + persona.getNombres());
                 
                response.setCodigo(200);
                response.setMensaje("Success");
                response.setData(usuarios);
        } catch (Exception e) {
                log.warning(e.getMessage());
                response.setCodigo(400);
                response.setMensaje("Ocurrio un error al registrar Usuarios");
        }
        return response;
    }
    
    @Path("listanoactivos")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public ResponseData<List<Usuarios>> getUsuariosNoActivos(){
        ResponseData<List<Usuarios>> response = new ResponseData<>();
        try {
            response.setCodigo(200);
            response.setMensaje("Success");
            response.setData(controller.getListaNoActivos());
            return response;
        } catch (Exception e) {
            response.setCodigo(401);
            response.setMensaje("Ocurrio un error al listar usuarios no activos");
        }
       return response;
    }
}
