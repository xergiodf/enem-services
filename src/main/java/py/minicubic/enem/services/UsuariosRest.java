package py.minicubic.enem.services;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;
import javax.ejb.Singleton;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import py.minicubic.enem.services.annotations.LoggedIn;
import py.minicubic.enem.services.annotations.Secured;
import py.minicubic.enem.services.dto.PersonaDTO;
import py.minicubic.enem.services.dto.ResponseData;
import py.minicubic.enem.services.dto.UsuariosDTO;
import py.minicubic.enem.services.ejb.CiudadController;
import py.minicubic.enem.services.ejb.UsuariosController;
import py.minicubic.enem.services.mail.EnviarMail;
import py.minicubic.enem.services.model.Ciudad;
import py.minicubic.enem.services.model.Franquiciado;
import py.minicubic.enem.services.model.Persona;
import py.minicubic.enem.services.model.Usuarios;
import py.minicubic.enem.services.util.Constants;
import py.minicubic.enem.services.util.Util;

/**
 *
 * @author hectorvillalba
 */
@Path("usuariosrest")
@Singleton
public class UsuariosRest {

    @Inject
    @LoggedIn
    Usuarios usuarioLogueado;
    @Inject
    private UsuariosController controller;
    @Inject
    private CiudadController ciudadController;

    @PersistenceContext
    private EntityManager em;
    Logger log = Logger.getLogger("UsuariosRest");
    
    @Inject
    private EnviarMail enviarMail;

    @Path("login")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public ResponseData<String> obtenerUsuario(UsuariosDTO dto) {
        ResponseData<String> response = new ResponseData<>();
        log.info("*** Obtener Ususario ***");
        log.info("Username: " + dto.getUsuario());

        try {

            Usuarios usuario = null;
            // Si existe un token en el request, llego desde el mail
            if (!Util.isEmpty(dto.getTokenCambioPass())) {
                
                // Verificamos que sea valido el token
                usuario = controller.getUsuarioByUsernameByCambioPassToken(dto.getUsuario(), dto.getTokenCambioPass());
                
                if ( Util.isEmpty(usuario) ) {
                    log.warning("No existe token en la base de datos. Ya fue utilizado.");
                    response.setCodigo(401);
                    response.setMensaje("No autorizado, autenticacion rechazada");
                    return response;
                }
                
                // Limpiamos el token
                usuario.setTokenCambioPass("");
                em.merge(usuario);
            } else {
                List<Usuarios> lista = controller.getUsuarios(dto.getUsuario(), dto.getPassword());
                if (lista.isEmpty()) {
                    log.warning("Datos invalidos");
                    response.setCodigo(401);
                    response.setMensaje("No autorizado, autenticacion rechazada");
                    return response;
                }
                
                usuario = lista.iterator().next();
            }

            log.info("Login satisfactorio...");
            response.setCodigo(200);
            response.setMensaje("Success");
            response.setData(Util.createToken(usuario.getIdUsuario()));
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
    public ResponseData<Usuarios> registrarUsuario(PersonaDTO dto) {
        ResponseData<Usuarios> response = new ResponseData<>();
        try {
            // Validación de Sponsor
            Persona sponsor = null;
            log.info("*** Registrando Usuario ***");
            List<Usuarios> lista = controller.getUsuarioByUsername(dto.getSponsorUsername());

            if (lista.isEmpty()) {
                log.warning("Sponsor invalido");
                response.setCodigo(301);
                response.setMensaje("No existe el sponsor ingresado");
                return response;
            } else {
                List<Persona> listaSponsor = controller.getPersona(lista.get(0).getIdUsuario());
                sponsor = listaSponsor.get(0);
            }

            // Validaciones de nulidad
            if (dto.getNombres() == null || dto.getNombres().isEmpty() // Nombres
                    || dto.getNroDocumento() == null || dto.getNroDocumento().isEmpty() // Nro Documento
                    || dto.getDireccion() == null || dto.getDireccion().isEmpty() // Dirección
                    || dto.getEmail() == null || dto.getEmail().isEmpty() // Email
                    || dto.getPassword() == null || dto.getPassword().isEmpty() // Password
                    || dto.getFechaNacimiento() == null || dto.getFechaNacimiento().isEmpty() // Fecha Nacimiento
                    || dto.getSponsorUsername() == null || dto.getSponsorUsername().isEmpty() // Sponsor
                    || dto.getApellidos() == null || dto.getApellidos().isEmpty() // Apellido
                    || dto.getGenero() == null || dto.getGenero().isEmpty()) {                  // Genero

                log.warning("Algunos campos son requeridos");
                response.setCodigo(301);
                response.setMensaje("Campos requeridos: *Nombres *Nro. Documento *Direccion *Email *Password *Fecha Nacimiento *Sponsor *Apellido *Genero");
                return response;
            }

            // Validacion de Ciudad
            Ciudad ciudad = null;
            if (dto.getIdCiudad() != null || !dto.getIdCiudad().isEmpty()) {

                ciudad = ciudadController.getCiudad(Long.valueOf(dto.getIdCiudad()));
                if (ciudad == null) {
                    log.warning("Ciudad invalida");
                    response.setCodigo(302);
                    response.setMensaje("Ciudad invalida");
                    return response;
                }
            }

            // Validación de Edad
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            Date fechaNacimiento = sdf.parse(dto.getFechaNacimiento());

            // Validación de usuario
            if (!controller.getUsuarioByUsername(dto.getUsername()).isEmpty()) {
                log.warning("Usuario ya existe en la base de datos");
                response.setCodigo(303);
                response.setMensaje("Usuario ya existe en la base de datos");
                return response;
            }

            // Validación de email
            if (!controller.getPersonasByEmail(dto.getEmail()).isEmpty()) {
                log.warning("Email ya existe en la base de datos");
                response.setCodigo(304);
                response.setMensaje("Email ya existe en la base de datos");
                return response;
            }

            GregorianCalendar now = new GregorianCalendar();
            GregorianCalendar nacimiento = new GregorianCalendar();
            nacimiento.setTime(fechaNacimiento);

            int edad = now.get(GregorianCalendar.YEAR) - nacimiento.get(GregorianCalendar.YEAR);
            int mesNaci = nacimiento.get(GregorianCalendar.MONTH);
            int diaNaci = nacimiento.get(GregorianCalendar.DAY_OF_MONTH);
            int mes = now.get(GregorianCalendar.MONTH);
            int dia = now.get(GregorianCalendar.DAY_OF_MONTH);

            if (mes > mesNaci) {
                edad = edad + 1;
            } else if (mes == mesNaci) {
                if (dia >= diaNaci) {
                    edad = edad + 1;
                }
            }

            if (edad < 18) {
                log.warning("Edad invalida...");
                response.setCodigo(303);
                response.setMensaje("Edad invalida");
                return response;
            }

            Usuarios usuarios = new Usuarios();
            usuarios.setEstado(Constants.ESTADO_SINCONFIRMAR);
            usuarios.setPassword(dto.getPassword());
            usuarios.setUsername(dto.getUsername());
            usuarios.setTokenConfirmacionEmail(UUID.randomUUID().toString());
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

            persona.setFechaNacimiento(fechaNacimiento);
            persona.setUsuario(usuarios);
            persona.setCiudad(ciudad);

            em.persist(persona);
            log.info("Persona creada: " + persona.getNombres());

            Franquiciado franquiciado = new Franquiciado();
            franquiciado.setNumeracion(1);
            franquiciado.setPersona(persona);
            franquiciado.setSponsor(sponsor);
            franquiciado.setBrazo("");
            em.persist(franquiciado);
            log.info("Franquiciado creado con exito...");

                
            response.setCodigo(200);
            response.setMensaje("Success");
            response.setData(usuarios);
            List<String> emails = new ArrayList<>();
            emails.add(persona.getEmail());
            enviarMail.sendeEmail("Bienvenido " + usuarios.getUsername(), 
                    "Usted ha sido registrado satisfactoriamente en ENEM, por favor ingrese a la pagina principal con su usuario y pass", emails);
        } catch (Exception e) {
            e.printStackTrace();
            response.setCodigo(400);
            response.setMensaje("Ocurrió un error en el servidor");
        }
        return response;
    }

    @Path("listanoactivos")
    @GET
    @Secured
    @Produces(MediaType.APPLICATION_JSON)
    public ResponseData<List<Usuarios>> getUsuariosNoActivos() {
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
    @Secured
    @Produces(MediaType.APPLICATION_JSON)
    public ResponseData<List<Usuarios>> getUsuariosActivos() {
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

    @Path("lista")
    @GET
    @Secured
    @Produces(MediaType.APPLICATION_JSON)
    public ResponseData<List<UsuariosDTO>> getUsuarios() {
        ResponseData<List<UsuariosDTO>> response = new ResponseData<>();
        try {
            response.setCodigo(200);
            response.setMensaje("Success");

            UsuariosDTO usuarioResponse = null;
            List<UsuariosDTO> usuariosResponse = new ArrayList<>();
            DateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

            for (Persona persona : controller.getListPersonaUsuarios()) {
                usuarioResponse = new UsuariosDTO();
                usuarioResponse.setNombreCompleto(persona.getNombres() + " " + persona.getApellidos());
                usuarioResponse.setActivo(!persona.getUsuario().getEstado().equals("NOACTIVO"));
                usuarioResponse.setFechaRegistro(df.format(persona.getUsuario().getFechaRegistro()));
                usuarioResponse.setUsuario(persona.getUsuario().getUsername());

                usuariosResponse.add(usuarioResponse);
            }

            response.setData(usuariosResponse);
            return response;
        } catch (Exception e) {
            e.printStackTrace();
            response.setCodigo(401);
            response.setMensaje("Ocurrio un error al listar usuarios");
        }
        return response;
    }

    @Path("cambiarEstadoUsuario/{userId}")
    @GET
    @Secured
    @Produces(MediaType.APPLICATION_JSON)
    public ResponseData cambiarEstadoUsuario(@PathParam(value = "userId") String userId) {
        ResponseData<List<UsuariosDTO>> response = new ResponseData<>();
        try {

            // Verificar que el usuario logueado tiene permisos para hacer esto            
            Usuarios usuario = controller.getUsuario(Long.valueOf(userId));

            if (Util.isEmpty(usuario)) {
                log.warning("No se encuentra usuario con id: " + userId);
                response.setCodigo(305);
                response.setMensaje("No se encuentra usuario con id: " + userId);
                return response;
            }

            response.setCodigo(200);
            response.setMensaje("Success");

            usuario.setEstado((usuario.getEstado().equals("NOACTIVO")) ? "ACTIVO" : "NOACTIVO");

            em.merge(usuario);

            return response;
        } catch (Exception e) {
            e.printStackTrace();
            response.setCodigo(401);
            response.setMensaje("Ocurrio un error al cambiar el estado del usuario");
        }
        return response;
    }

    @Path("confirmarEmailUsuario/{username}/{token}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public ResponseData confirmarEmail(@PathParam(value = "username") String username,
            @PathParam(value = "token") String token) {
        ResponseData response = new ResponseData();

        try {

            // Verificar token de parámetro con el de la base de datos
            Usuarios usuario = controller.getUsuarioByUsernameByEmailConfirmToken(username, token);

            if (Util.isEmpty(usuario)) {
                log.warning("Usuario no encontrado en base a los parámetros seleccionados");
                response.setCodigo(306);
                response.setMensaje("No existe la combinación usuario (" + username + ") y token (" + token + ")");
            }

            // Limpiar el hash
            usuario.setTokenConfirmacionEmail("");
            usuario.setEstado(Constants.ESTADO_INACTIVO);

            em.merge(usuario);

            response.setCodigo(200);
            response.setMensaje("Se confirmó el email del usuario " + username);
        } catch (Exception e) {
            e.printStackTrace();
            response.setCodigo(401);
            response.setMensaje("Ocurrio un error al confirmar email del usuario: " + username);
        }

        return response;
    }

    @Path("cambiarContrasenha")
    @POST
    @Secured
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public ResponseData cambiarContrasenha(UsuariosDTO dto) {
        ResponseData response = new ResponseData<>();

        try {
            // Verificamos que la contraseña anterior sea la misma
            List<Usuarios> usuarioList = controller.getUsuarios(usuarioLogueado.getUsername(), dto.getPassword());

            if (usuarioList.isEmpty()) {
                log.warning("Contraseñas no coinciden");
                response.setCodigo(307);
                response.setMensaje("Las contraseñas no coinciden");
                return response;
            }

            // Realizamos el cambio
            usuarioLogueado.setPassword(dto.getNewPassword());
            em.merge(usuarioLogueado);

            response.setCodigo(200);
            response.setMensaje("Contraseña cambiada correctamente");

        } catch (Exception e) {
            e.printStackTrace();
            response.setCodigo(401);
            response.setMensaje("Ocurrio un error al cambiar la contraseña");
        }

        return response;
    }

    @Path("solicitarCambioContrasenha/{mail}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public ResponseData solicitarCambioContrasenha(@PathParam(value = "mail") String username) {
        ResponseData response = new ResponseData<>();

        try {

            // Generar un token y actualizar en el usuario
            List<Usuarios> usuarioList = controller.getUsuarioByUsername(username);

            if (usuarioList.isEmpty()) {
                log.warning("No existe el usuario " + username);
                response.setCodigo(308);
                response.setMensaje(username);
            }

            Usuarios usuario = usuarioList.iterator().next();
            usuario.setTokenCambioPass(UUID.randomUUID().toString());
            em.merge(usuario);

            // Enviar un mail
        } catch (Exception e) {
            e.printStackTrace();
            response.setCodigo(401);
            response.setMensaje("Ocurrio un error al solicitar el cambio de contraseña del usuario " + username);
        }

        return response;
    }
}
