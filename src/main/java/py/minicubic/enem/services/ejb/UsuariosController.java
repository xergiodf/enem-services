package py.minicubic.enem.services.ejb;

import java.util.ArrayList;
import java.util.List;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import py.minicubic.enem.services.model.Persona;
import py.minicubic.enem.services.model.Usuarios;

/**
 *
 * @author hectorvillalba
 * @author xergio
 */
@Stateless
public class UsuariosController {

    @PersistenceContext
    EntityManager em;

    /**
     * Obtiene usuario en base al Id
     * @param id
     * @return 
     */
    public Usuarios getUsuario(Long id) {
        try {
            return em.find(Usuarios.class, id);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Obtiene una lista de usuarios en base a un user y password
     * Si existe, debería de ser solamente 1 registro
     * @param user
     * @param pass
     * @return 
     */
    public List<Usuarios> getUsuarios(String user, String pass) {
        try {
            return em.createQuery("select u from Usuarios u where u.username like :username and u.password like :pass")
                    .setParameter("username", user)
                    .setParameter("pass", pass)
                    .getResultList();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Obtiene un usuario en base a un nombre de usuario.
     * Si existe, debería de ser solamente 1 registro
     * @param user
     * @return 
     */
    public List<Usuarios> getUsuarioByUsername(String user) {
        try {
            return em.createQuery("select u from Usuarios u where u.username like '%" + user + "' ")
                    .getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList();
        }
    }
    
    /**
     * Obtiene una persona en base a un email
     * Si existe, debería de ser solamente 1 registro
     * @param email
     * @return 
     */
    public List<Persona> getPersonasByEmail(String email) {
        try {
            return em.createQuery("select p from persona p where p.email = :email")
                    .setParameter("email", email)
                    .getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList();
        }
    }

    /** 
     * Obtiene una persona en base a un Id
     * @param id
     * @return 
     */
    public List<Persona> getPersona(Long id) {
        try {
            return em.createQuery("select p from persona p where p.usuario.idUsuario = :id")
                    .setParameter("id", id)
                    .getResultList();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    /**
     * Obtiene una lista de personas asociadas a un usuario
     * @return 
     */
    public List<Persona> getListPersonaUsuarios() {
        try {
            return em.createQuery("from persona p where p.usuario is not null order by p.usuario.fechaRegistro DESC").getResultList();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    /**
     * Obtiene una lista de usuarios registrados ordenados por fecha de registro
     * @return 
     */
    public List<Usuarios> getListaUsuarios() {
        try {
            return em.createQuery("from Usuarios u order by u.fechaRegistro DESC").getResultList();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    /**
     * Obtiene un usuario en base a un username y un token
     * @param username
     * @param tokenEmail
     * @return 
     */
    public Usuarios getUsuarioByUsernameByEmailConfirmToken(String username, String tokenEmail) {
        try {
            return (Usuarios) em.createQuery("select u from Usuarios u where u.username = :username and u.tokenConfirmacionEmail = :tokenEmail")
                    .setParameter("username", username)
                    .setParameter("tokenEmail", tokenEmail)
                    .getSingleResult();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    /**
     * Obtiene un usuario en base a un username y un token
     * @param username
     * @param tokenCambioPass
     * @return 
     */
    public Usuarios getUsuarioByUsernameByCambioPassToken(String username, String tokenCambioPass) {
        try {
            return (Usuarios) em.createQuery("select u from Usuarios u where u.username = :username and u.tokenCambioPass = :tokenCambioPass")
                    .setParameter("username", username)
                    .setParameter("tokenCambioPass", tokenCambioPass)
                    .getSingleResult();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Usuarios> getListaNoActivos() {
        try {
            return em.createQuery("select u from Usuarios u where u.estado = 'NOACTIVO' order by u.idUsuario ").getResultList();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Usuarios> getListaActivos() {
        try {
            return em.createQuery("select u from Usuarios u where u.estado = 'ACTIVO' order by u.idUsuario ").getResultList();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
