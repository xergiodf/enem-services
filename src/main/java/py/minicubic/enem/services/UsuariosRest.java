package py.minicubic.enem.services;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
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
import py.minicubic.enem.services.ejb.ArbolController;
import py.minicubic.enem.services.ejb.CiudadController;
import py.minicubic.enem.services.ejb.UsuariosController;
import py.minicubic.enem.services.mail.EnviarMail;
import py.minicubic.enem.services.model.Ciudad;
import py.minicubic.enem.services.model.Franquiciado;
import py.minicubic.enem.services.model.Nodo;
import py.minicubic.enem.services.model.Persona;
import py.minicubic.enem.services.model.Rol;
import py.minicubic.enem.services.model.UsuarioRol;
import py.minicubic.enem.services.model.Usuarios;
import py.minicubic.enem.services.util.Constants;
import py.minicubic.enem.services.util.PasswordService;
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
    @Inject
    private ArbolController arbolController;

    @PersistenceContext
    private EntityManager em;
    static final Logger LOG = Logger.getLogger("UsuariosRest");

    @Path("login")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public ResponseData<UsuariosDTO> obtenerUsuario(UsuariosDTO dto) {
        ResponseData<UsuariosDTO> response = new ResponseData<>();
        LOG.info("*** Obtener Ususario ***");
        LOG.log(Level.INFO, "Username: {0}", dto.getUsuario());

        try {

            Usuarios usuario;

            PasswordService ps = new PasswordService();
            String encryptedPassword = ps.encrypt(dto.getPassword());

            usuario = controller.getUsuarios(dto.getUsuario(), encryptedPassword);
            if (Util.isEmpty(usuario)) {
                LOG.log(Level.WARNING, "No coinciden usuario/contrase\u00f1a -> {0}:{1}", new Object[]{dto.getUsuario(), encryptedPassword});
                response.setCodigo(401);
                response.setMensaje("No coinciden usuario/contraseña");
                return response;
            }

            if (Constants.ESTADO_SINCONFIRMAR.equals(usuario.getEstado())) {
                LOG.warning("Usuario sin confirmar");
                response.setCodigo(401);
                response.setMensaje("El usuario aún no confirmó su email. Verifique su correo para activar.");
                return response;
            }

            LOG.info("Login satisfactorio...");
            response.setCodigo(200);
            response.setMensaje("Success");

            Persona persona = controller.getPersonaByUsuarioId(usuario.getIdUsuario());

            dto = new UsuariosDTO();
            dto.setToken(Util.createToken(usuario.getIdUsuario()));
            dto.setNombreCompleto(persona.getNombres() + " " + persona.getApellidos());
            dto.setAdmin(controller.isUserAdmin(usuario.getIdUsuario()));
            dto.setDireccion(usuario.getDireccionRed().equals(Constants.DIR_DERECHA) ? Constants.DIR_DERECHA_STR : Constants.DIR_IZQUIERDA_STR);
            dto.setUsuario(usuario.getUsername());
            if (!Util.isEmpty(persona.getIdSponsor())) {
                Persona personaSponsor = controller.getPersona(persona.getIdSponsor());
                dto.setSponsor(personaSponsor.getUsuario().getUsername());
            }

            response.setData(dto);
        } catch (Exception e) {
            e.printStackTrace();
            response.setCodigo(401);
            response.setMensaje("Error al intentar loguearse, autenticacion rechazada");
        }
        return response;
    }

    @Path("datospatrocinador")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public ResponseData<PersonaDTO> getDatosPersona(UsuariosDTO dto) {
        ResponseData<PersonaDTO> response = new ResponseData<>();

        try {

            PersonaDTO personaObj = new PersonaDTO();
            Persona persona = controller.getPersonaByUsername(dto.getUsuario());
            personaObj.setNombres(persona.getNombres());
            personaObj.setApellidos(persona.getApellidos());

            response.setCodigo(200);
            response.setData(personaObj);
        } catch (NullPointerException npe) {
            response.setCodigo(404);
            response.setMensaje("No existe el patrocinador");
        } catch (Exception e) {
            e.printStackTrace();
            response.setCodigo(401);
            response.setMensaje("Error al obtener el usuario patrocinador");
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
            Persona sponsor;
            LOG.info("*** Registrando Usuario ***");
            List<Usuarios> lista = controller.getUsuarioByUsernameActivo(dto.getSponsorUsername());

            if (lista.isEmpty()) {
                LOG.warning("Sponsor invalido");
                response.setCodigo(301);
                response.setMensaje("No existe el sponsor ingresado");
                return response;
            } else {
                sponsor = controller.getPersonaByUsuarioId(lista.get(0).getIdUsuario());
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

                LOG.warning("Algunos campos son requeridos");
                response.setCodigo(301);
                response.setMensaje("Campos requeridos: *Nombres *Nro. Documento *Direccion *Email *Password *Fecha Nacimiento *Sponsor *Apellido *Genero");
                return response;
            }

            // Validacion de Ciudad
            Ciudad ciudad = null;
            if (!Util.isEmpty(dto.getIdCiudad())) {

                ciudad = ciudadController.getCiudad(Long.valueOf(dto.getIdCiudad()));
                if (ciudad == null) {
                    LOG.warning("Ciudad invalida");
                    response.setCodigo(302);
                    response.setMensaje("Ciudad invalida");
                    return response;
                }
            }

            // Validación de Edad
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            Date fechaNacimiento = sdf.parse(dto.getFechaNacimiento());

            // Validación de usuarioFranquiciado
            if (!controller.getUsuarioByUsername(dto.getUsername()).isEmpty()) {
                LOG.warning("Usuario ya existe en la base de datos");
                response.setCodigo(303);
                response.setMensaje("Usuario ya existe en la base de datos");
                return response;
            }

            // Validación de email
            if (!controller.getPersonasByEmail(dto.getEmail()).isEmpty()) {
                LOG.warning("Email ya existe en la base de datos");
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
                LOG.warning("Edad invalida...");
                response.setCodigo(305);
                response.setMensaje("Edad invalida");
                return response;
            }

            // Validación de ci
            if (!controller.getPersonaByCI(Long.valueOf(dto.getNroDocumento())).isEmpty()) {
                LOG.warning("CI ya existe en la base de datos");
                response.setCodigo(306);
                response.setMensaje("Nro. de Documento ya existe en la base de datos");
                return response;
            }

            PasswordService ps = new PasswordService();
            String encryptedPassword = ps.encrypt(dto.getPassword());

            Usuarios usuarios = new Usuarios();
            usuarios.setEstado(Constants.ESTADO_SINCONFIRMAR);
            usuarios.setPassword(encryptedPassword);
            usuarios.setUsername(dto.getUsername());
            usuarios.setTokenConfirmacionEmail(UUID.randomUUID().toString());
            usuarios.setDireccionRed(sponsor.getUsuario().getDireccionRed());
            em.persist(usuarios);
            LOG.log(Level.INFO, "Usuarios creado: {0}", usuarios.getUsername());

            Rol rol = new Rol();
            rol.setIdRol(2L);

            UsuarioRol userRol = new UsuarioRol();
            userRol.setUsuario(usuarios);
            userRol.setRol(rol);

            em.persist(userRol);

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
            persona.setIdSponsor(sponsor.getIdPersona());
            persona.setFechaNacimiento(fechaNacimiento);
            persona.setUsuario(usuarios);
            persona.setCiudad(ciudad);

            em.persist(persona);
            LOG.log(Level.INFO, "Persona creada: {0}", persona.getNombres());

            response.setCodigo(200);
            response.setMensaje("Success");
            response.setData(usuarios);

            String endPoint = Constants.BUSINESS_ENDPOINT + "&tokenEmail=" + usuarios.getTokenConfirmacionEmail();

            List<String> emails = new ArrayList<>();
            emails.add(persona.getEmail());
            EnviarMail.sendeEmail("Bienvenido " + usuarios.getUsername(),
                    "Usted ha sido registrado satisfactoriamente en ENEM, por favor confirme su email ingresando al siguiente link " + endPoint, emails);
        } catch (Exception e) {
            e.printStackTrace();
            response.setCodigo(400);
            response.setMensaje("Ocurrió un error en el servidor");
        }
        return response;
    }

    @Path("getusuario/{userId}")
    @GET
    @Secured
    @Produces(MediaType.APPLICATION_JSON)
    public ResponseData<PersonaDTO> getUsuarioById(@PathParam("userId") Long userId) {
        ResponseData<PersonaDTO> response = new ResponseData<>();
        try {

            if (!controller.isUserAdmin(usuarioLogueado.getIdUsuario())) {
                response.setCodigo(401);
                response.setMensaje(Constants.MSG_401);
                return response;
            }
            
            DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
            
            Persona persona = controller.getPersonaByUsuarioId(userId); 
            PersonaDTO dto = new PersonaDTO();
            dto.setIdUsuario(persona.getUsuario().getIdUsuario());
            dto.setNombres(persona.getNombres());
            dto.setApellidos(persona.getApellidos());
            dto.setNroDocumento(persona.getNroDocumento().toString());
            dto.setGenero(persona.getGenero());
            dto.setFechaNacimiento(df.format(persona.getFechaNacimiento()));
            dto.setEmail(persona.getEmail());
            dto.setCelular(persona.getCelular());
            dto.setTelefono(persona.getTelefono());
            dto.setRuc(persona.getRuc());
            dto.setIdCiudad(persona.getCiudad().getIdCiudad().toString());
            dto.setDireccion(persona.getDireccion());
            
            response.setCodigo(200);
            response.setMensaje("Success");
            response.setData(dto);
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

            if (!controller.isUserAdmin(usuarioLogueado.getIdUsuario())) {
                response.setCodigo(401);
                response.setMensaje(Constants.MSG_401);
                return response;
            }

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

            if (!controller.isUserAdmin(usuarioLogueado.getIdUsuario())) {
                response.setCodigo(401);
                response.setMensaje(Constants.MSG_401);
                return response;
            }

            response.setCodigo(200);
            response.setMensaje("Success");

            UsuariosDTO usuarioResponse;
            List<UsuariosDTO> usuariosResponse = new ArrayList<>();
            DateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            DateFormat df1 = new SimpleDateFormat("dd/MM/yyyy");

            for (Persona persona : controller.getListPersonaUsuarios(usuarioLogueado.getIdUsuario())) {
                usuarioResponse = new UsuariosDTO();
                usuarioResponse.setIdUsuario(persona.getUsuario().getIdUsuario());
                usuarioResponse.setNombres(persona.getNombres());
                usuarioResponse.setApellidos(persona.getApellidos());
                usuarioResponse.setNombreCompleto(persona.getNombres() + " " + persona.getApellidos());
                usuarioResponse.setActivo(persona.getUsuario().getEstado().equals("ACTIVO"));
                usuarioResponse.setFechaRegistro(df.format(persona.getUsuario().getFechaRegistro()));
                usuarioResponse.setUsuario(persona.getUsuario().getUsername());
                usuarioResponse.setGenero(persona.getGenero().equals("M") ? "MASCULINO" : "FEMENINO");
                usuarioResponse.setFechaNacimiento(df1.format(persona.getFechaNacimiento()));
                usuarioResponse.setSponsor((controller.getPersona(persona.getIdSponsor())).getUsuario().getUsername());
                usuarioResponse.setNroDocumento(persona.getNroDocumento().toString());
                usuarioResponse.setRuc(persona.getRuc());
                usuarioResponse.setTelefono(persona.getTelefono());
                usuarioResponse.setCelular(persona.getCelular());
                usuarioResponse.setLugarDireccion(persona.getDireccion());
                usuarioResponse.setCiudad(!Util.isEmpty(persona.getCiudad()) ? ciudadController.getCiudad(persona.getCiudad().getIdCiudad()).getNombre() : "");
                usuarioResponse.setMail(persona.getEmail());
                if (!Util.isEmpty(persona.getUsuario().getTokenConfirmacionEmail())) {
                    String link = Constants.BUSINESS_ENDPOINT + "&tokenEmail=" + persona.getUsuario().getTokenConfirmacionEmail();
                    usuarioResponse.setToken(link);
                }

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
                LOG.warning("Usuario no encontrado en base a los parámetros seleccionados");
                response.setCodigo(306);
                response.setMensaje("No existe la combinación usuario (" + username + ") y token (" + token + ")");
                return response;
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

    @Path("solicitarCambioContrasenha")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public ResponseData solicitarCambioContrasenha(PersonaDTO personaObj) {
        ResponseData response = new ResponseData<>();

        try {

            // Generar un token y actualizar en el usuarioFranquiciado
            List<Persona> personasList = controller.getPersonasByEmail(personaObj.getEmail());
            if (personasList.isEmpty()) {
                LOG.log(Level.WARNING, "No existe el usuario asociado al email:{0}", personaObj.getEmail());
                response.setCodigo(308);
                response.setMensaje("No existe ningun registro con el mail: " + personaObj.getEmail());
                return response;
            }

            Usuarios usuario = ((Persona) personasList.iterator().next()).getUsuario();
            usuario.setTokenCambioPass(UUID.randomUUID().toString());
            em.merge(usuario);

            // Enviar un mail
            String endPoint = Constants.BUSINESS_ENDPOINT + "&tokenCambioPass=" + usuario.getTokenCambioPass();

            List<String> emails = new ArrayList<>();
            emails.add(personaObj.getEmail());
            EnviarMail.sendeEmail("Solicitud de cambio de contraseña del usuario: " + usuario.getUsername(),
                    "Para cambiar su contraseña, ingrese por única vez en éste link " + endPoint, emails);

            response.setCodigo(200);
            response.setMensaje("Se ha enviado un mail a la dirección de correo " + personaObj.getEmail() + " para recuperar su contraseña.");
        } catch (Exception e) {
            e.printStackTrace();
            response.setCodigo(401);
            response.setMensaje("Ocurrio un error al solicitar el cambio de contraseña del usuario " + personaObj.getEmail());
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
            PasswordService ps = new PasswordService();
            String encryptedPassword = ps.encrypt(dto.getPassword());

            Usuarios usuario = controller.getUsuarios(usuarioLogueado.getUsername(), encryptedPassword);

            if (Util.isEmpty(usuario)) {
                LOG.warning("Contraseñas no coinciden");
                response.setCodigo(307);
                response.setMensaje("Las contraseñas no coinciden");
                return response;
            }

            // Realizamos el cambio
            usuario.setPassword(ps.encrypt(dto.getNewPassword()));
            em.merge(usuario);

            response.setCodigo(200);
            response.setMensaje("Contraseña cambiada correctamente");

        } catch (Exception e) {
            e.printStackTrace();
            response.setCodigo(401);
            response.setMensaje("Ocurrio un error al cambiar la contraseña");
        }

        return response;
    }

    @Path("cambioContrasenhaOlvidada")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public ResponseData cambioContrasenhaOlvidada(UsuariosDTO usuarioObj) {
        ResponseData response = new ResponseData<>();

        try {

            if (Util.isEmpty(usuarioObj.getTokenCambioPass())) {
                LOG.warning("Token invalido!");
                response.setCodigo(308);
                response.setMensaje("Token invalido!");
                return response;
            }

            Usuarios usuario = controller.getUsuarioByTokenCambioPass(usuarioObj.getTokenCambioPass());
            if (Util.isEmpty(usuario)) {
                LOG.warning("Token invalido!");
                response.setCodigo(308);
                response.setMensaje("Token invalido!");
                return response;
            }

            PasswordService ps = new PasswordService();
            String encryptedPassword = ps.encrypt(usuarioObj.getNewPassword());

            usuario.setTokenCambioPass("");
            usuario.setPassword(encryptedPassword);
            em.merge(usuario);

            response.setCodigo(200);
            response.setMensaje("Contraseña cambiada correctamente.");
        } catch (Exception e) {
            e.printStackTrace();
            response.setCodigo(401);
            response.setMensaje("Ocurrio un error al cambiar la contraseña del usuario.");
        }

        return response;
    }

    @Path("cambiarEstadoUsuario/{userId}")
    @GET
    @Secured
    @Produces(MediaType.APPLICATION_JSON)
    public ResponseData cambiarEstadoUsuario(@PathParam(value = "userId") Long userId) {
        ResponseData<List<UsuariosDTO>> response = new ResponseData<>();
        try {

            // Verificar que el usuarioFranquiciado logueado tiene permisos para hacer esto   
            if (!controller.isUserAdmin(usuarioLogueado.getIdUsuario())) {
                response.setCodigo(401);
                response.setMensaje(Constants.MSG_401);
                return response;
            }

            Persona personaFranquiciado = controller.getPersonaByUsuarioId(userId);
            if (Util.isEmpty(personaFranquiciado)) {
                LOG.log(Level.WARNING, "No se encuentra usuario con id: {0}", userId);
                response.setCodigo(301);
                response.setMensaje("No se encuentra usuario con id: " + userId);
                return response;
            }

            Usuarios usuarioFranquiciado = personaFranquiciado.getUsuario();
            if (Constants.ESTADO_SINCONFIRMAR.equals(usuarioFranquiciado.getEstado())) {
                LOG.log(Level.WARNING, "El usuario {0} aún no confirmó su cuenta.", usuarioFranquiciado.getUsername());
                response.setCodigo(302);
                response.setMensaje("El usuario " + usuarioFranquiciado.getUsername() + "aún no confirmó su cuenta");
                return response;
            }

            Persona personaSponsor = controller.getPersona(personaFranquiciado.getIdSponsor());

            if (Util.isEmpty(personaSponsor)) {
                LOG.log(Level.WARNING, "No se encuentra persona con id: {0}", personaFranquiciado.getIdSponsor());
                response.setCodigo(303);
                response.setMensaje("No se encuentra persona con id: " + personaFranquiciado.getIdSponsor());
                return response;
            }

            usuarioFranquiciado.setEstado((usuarioFranquiciado.getEstado().equals(Constants.ESTADO_INACTIVO)) ? Constants.ESTADO_ACTIVO : Constants.ESTADO_INACTIVO);

            em.merge(usuarioFranquiciado);

            // Enviar un mail
            List<String> emails = new ArrayList<>();
            emails.add(personaFranquiciado.getEmail());

            if (Constants.ESTADO_ACTIVO.equals(usuarioFranquiciado.getEstado())) {
                EnviarMail.sendeEmail("Activacion de la cuenta: " + usuarioFranquiciado.getUsername(),
                        "Este es un correo de aviso. No responder. Su cuenta ha sido activada correctamente.", emails);
            } else {
                EnviarMail.sendeEmail("Inactivacion de la cuenta: " + usuarioFranquiciado.getUsername(),
                        "Este es un correo de aviso. No responder. Su cuenta ha sido inactivada.", emails);
            }

            // Verificamos si existe en la red
            Franquiciado franquiciadoReg = controller.getFranquiciado(personaFranquiciado.getIdPersona(), personaSponsor.getIdPersona());
            if (Util.isEmpty(franquiciadoReg)) {
                LOG.info("No hay franquiciado. Creamos uno nuevo.");

                // Obtenemos el numero que le corresponde
                Integer numeracion = controller.getUltimoFranquiciadoBySponsor(personaSponsor.getIdPersona(), personaSponsor.getUsuario().getDireccionRed());

                franquiciadoReg = new Franquiciado();
                franquiciadoReg.setNumeracion(1 + numeracion.intValue());
                franquiciadoReg.setPersona(personaFranquiciado);
                franquiciadoReg.setSponsor(personaSponsor);
                franquiciadoReg.setBrazo(personaSponsor.getUsuario().getDireccionRed());
                em.persist(franquiciadoReg);
                LOG.info("Franquiciado creado con exito...");

                // Obtenemos la red del sponsor e insertamos en el orden que le corresponda
                Nodo nodoSponsor = arbolController.getArbolByPersona(personaSponsor.getIdPersona());
                arbolController.guardarNodo(personaFranquiciado.getIdPersona(), personaSponsor.getUsuario().getDireccionRed(), nodoSponsor);

                LOG.info("Nodo creado con exito...");
            } else {
                LOG.info("Franquiciado ya existe");
            }

            response.setCodigo(200);
            response.setMensaje("Usuario " + ((usuarioFranquiciado.getEstado().equals(Constants.ESTADO_INACTIVO)) ? "inactivado" : "activado") + " correctamente.");
            return response;
        } catch (Exception e) {
            e.printStackTrace();
            response.setCodigo(401);
            response.setMensaje("Ocurrio un error al cambiar el estado del usuario");
        }
        return response;
    }

    @Path("cambiarDireccionRed")
    @GET
    @Secured
    @Produces(MediaType.APPLICATION_JSON)
    public ResponseData cambiarDireccionRed() {
        ResponseData<UsuariosDTO> response = new ResponseData<>();
        try {

            Usuarios usuario = controller.getUsuario(usuarioLogueado.getIdUsuario());

            usuario.setDireccionRed(usuario.getDireccionRed().equals(Constants.DIR_DERECHA) ? Constants.DIR_IZQUIERDA : Constants.DIR_DERECHA);
            em.merge(usuario);

            UsuariosDTO usuarioObj = new UsuariosDTO();
            usuarioObj.setDireccion(usuario.getDireccionRed().equals(Constants.DIR_DERECHA) ? Constants.DIR_DERECHA_STR : Constants.DIR_IZQUIERDA_STR);
            response.setData(usuarioObj);
            response.setCodigo(200);
            response.setMensaje("Dirección de red cambiada correctamente");
            return response;
        } catch (Exception e) {
            e.printStackTrace();
            response.setCodigo(401);
            response.setMensaje("Ocurrio un error al cambiar la direccion de la red");
        }
        return response;
    }

    @Path("visualizarRed")
    @Secured
    @GET
    public String verRed() {
        try {

            Persona persona = controller.getPersonaByUsername(usuarioLogueado.getUsername());

            String arbol = arbolController.getNodoArbol(persona.getIdPersona(), null);

            return arbol;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "[{}]";
    }

    @Path("eliminarUsuario/{username}")
    @GET
    @Secured
    @Produces(MediaType.APPLICATION_JSON)
    public ResponseData eliminarUsuario(@PathParam(value = "username") String username) {
        ResponseData<List<UsuariosDTO>> response = new ResponseData<>();
        try {

            LOG.log(Level.INFO, "eliminarUsuario -> {0}", username);

            // Verificar que el usuarioFranquiciado logueado tiene permisos para hacer esto   
            if (!controller.isUserAdmin(usuarioLogueado.getIdUsuario())) {
                response.setCodigo(401);
                response.setMensaje(Constants.MSG_401);
                return response;
            }

            if (controller.eliminarUsuario(username)) {
                response.setCodigo(200);
                response.setMensaje("Usuario eliminado correctamente");
            } else {
                response.setCodigo(310);
                response.setMensaje("No se pudo eliminar el usuario");
            }

        } catch (Exception e) {
            e.printStackTrace();
            response.setCodigo(401);
            response.setMensaje("Ocurrio un error al eliminar el usuario");
        }

        return response;
    }

    @Path("editar/{userId}")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public ResponseData<Usuarios> editarUsuario(PersonaDTO dto, 
            @PathParam(value = "userId") Long userId) {
        ResponseData<Usuarios> response = new ResponseData<>();
        try {

            // Validaciones de nulidad
            if (dto.getNombres() == null || dto.getNombres().isEmpty() // Nombres
                    || dto.getNroDocumento() == null || dto.getNroDocumento().isEmpty() // Nro Documento
                    || dto.getDireccion() == null || dto.getDireccion().isEmpty() // Dirección
                    || dto.getEmail() == null || dto.getEmail().isEmpty() // Email
                    || dto.getFechaNacimiento() == null || dto.getFechaNacimiento().isEmpty() // Fecha Nacimiento
                    || dto.getApellidos() == null || dto.getApellidos().isEmpty() // Apellido
                    || dto.getGenero() == null || dto.getGenero().isEmpty()) {                  // Genero

                LOG.warning("editarUsuario -> No valida nulidad");
                response.setCodigo(301);
                response.setMensaje("Campos requeridos: *Nombres *Nro. Documento *Direccion *Email *Password *Fecha Nacimiento *Sponsor *Apellido *Genero");
                return response;
            }

            // Validacion de Ciudad
            Ciudad ciudad = null;
            if (!Util.isEmpty(dto.getIdCiudad())) {

                ciudad = ciudadController.getCiudad(Long.valueOf(dto.getIdCiudad()));
                if (ciudad == null) {
                    LOG.warning("editarUsuario -> Ciudad invalida");
                    response.setCodigo(302);
                    response.setMensaje("Ciudad invalida");
                    return response;
                }
            }

            // Validación de email
            if (!controller.getPersonasByEmailExcludeId(dto.getEmail(), userId).isEmpty()) {
                LOG.warning("editarUsuario -> Email ya existe en la base de datos");
                response.setCodigo(304);
                response.setMensaje("Email ya existe en la base de datos");
                return response;
            }
            
            // Validación de Edad
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            Date fechaNacimiento = sdf.parse(dto.getFechaNacimiento());

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
                LOG.warning("editarUsuario -> Edad invalida");
                response.setCodigo(305);
                response.setMensaje("Edad invalida");
                return response;
            }

            // Validación de ci
            if (!controller.getPersonaByCIExcludeId(Long.valueOf(dto.getNroDocumento()), userId).isEmpty()) {
                LOG.warning("editarUsuario -> CI ya existe en la base de datos");
                response.setCodigo(306);
                response.setMensaje("Nro. de Documento ya existe en la base de datos");
                return response;
            }

            Persona persona = controller.getPersonaByUsuarioId(userId);
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
            persona.setCiudad(ciudad);

            em.persist(persona);
            LOG.log(Level.INFO, "editarUsuario -> Persona Actualizada: {0}", persona.getNombres());

            response.setCodigo(200);
            response.setMensaje("Usuario actualizado correctamente");  

        } catch (Exception e) {
            e.printStackTrace();
            response.setCodigo(400);
            response.setMensaje("Ocurrió un error en el servidor");
        }
        return response;
    }
}
