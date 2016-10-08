/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package py.minicubic.enem.services;

import java.text.SimpleDateFormat;
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
import py.minicubic.enem.services.ejb.CiudadController;
import py.minicubic.enem.services.ejb.UsuariosController;
import py.minicubic.enem.services.model.Ciudad;
import py.minicubic.enem.services.model.Franquiciado;
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
    @Inject
    private CiudadController ciudadController;
    
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
                
                Persona sponsor = null;
                log.info("*** Registrar Usuario ***");
                List<Usuarios> lista = controller.getSponsor(dto.getSponsorUsername());
                if(lista.isEmpty()){
                    log.warning("Sponsor invalido");
                    response.setCodigo(301);
                    response.setMensaje("No existe el sponsor ingresado");
                    return response;
                }else{
                    List<Persona> listaSponsor = controller.getPersona(lista.get(0).getIdUsuario());
                    sponsor = listaSponsor.get(0);
                }
                
                if (dto.getNombres() == null || dto.getNombres().isEmpty() ||
                    dto.getNroDocumento() == null || dto.getNroDocumento().isEmpty() ||
                    dto.getDireccion() == null || dto.getDireccion().isEmpty() ||
                    dto.getEmail() == null || dto.getEmail().isEmpty() ||
                    dto.getPassword() == null || dto.getPassword().isEmpty() ||
                    dto.getFechaNacimiento() == null || dto.getFechaNacimiento().isEmpty() || 
                    dto.getSponsorUsername() == null || dto.getSponsorUsername().isEmpty() ||
                    dto.getApellidos() == null && dto.getApellidos().isEmpty()) {
                    log.warning("Algunos campos son requeridos");
                    response.setCodigo(301);
                    response.setMensaje("Campos requeridos vacios");
                    return response;
                }
                
                
                Ciudad ciudad = ciudadController.getCiudad(Long.valueOf(dto.getIdCiudad()));
                if (ciudad == null) {
                    log.warning("Ciudad invalida");
                    response.setCodigo(302);
                    response.setMensaje("Ciudad invalida");
                }

                Usuarios usuarios = new Usuarios();
                usuarios.setEstado("NOACTIVO");
                usuarios.setPassword(dto.getPassword());
                usuarios.setUsername(dto.getUsername());
                em.persist(usuarios);
                log.info("Usuarios creado: " + usuarios.getUsername());

                Persona persona = new Persona();
                persona.setNombres(dto.getNombres());
                persona.setNroDocumento(Long.parseLong(dto.getNroDocumento()));
                persona.setApellidos(dto.getApellidos());
                persona.setCelular(dto.getCelular());
                persona.setTelefono(dto.getTelefono());
                persona.setRuc(dto.getRuc());
                persona.setGenero(dto.getGenero());
                persona.setDireccion(dto.getDireccion());
                persona.setEmail(dto.getEmail());
                
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");                
                persona.setFechaNacimiento(sdf.parse(dto.getFechaNacimiento()));
                persona.setUsuario(usuarios);
                persona.setCiudad(ciudad);

                
                em.persist(persona);
                log.info("Persona creada: " + persona.getNombres());
                
                Franquiciado franquiciado = new Franquiciado();
                franquiciado.setNumeracion(1);
                franquiciado.setPersona(persona);
                franquiciado.setSponsor(sponsor);
                em.persist(franquiciado);
                log.info("Franquiciado creado con exito...");
                 
                response.setCodigo(200);
                response.setMensaje("Success");
                response.setData(usuarios);
        } catch (Exception e) {
                response.setCodigo(400);
                response.setMensaje(e.getMessage());
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
    
    @Path("listaactivos")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public ResponseData<List<Usuarios>> getUsuariosActivos(){
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
