package py.minicubic.enem.services.ejb;

import java.util.ArrayList;
import java.util.List;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import py.minicubic.enem.services.model.Franquiciado;
import py.minicubic.enem.services.model.Persona;
import py.minicubic.enem.services.model.Rol;
import py.minicubic.enem.services.model.UsuarioRol;
import py.minicubic.enem.services.model.Usuarios;
import py.minicubic.enem.services.util.Constants;
import py.minicubic.enem.services.util.Util;

/**
 *
 * @author hectorvillalba
 * @author xergio
 */
@Stateless
public class UsuariosController {

    @PersistenceContext
    EntityManager em;
    
    public Persona getPersona(Long id) {
        try {
            return em.find(Persona.class, id);
        } catch (NoResultException nre) {
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public Usuarios getUsuario(Long id) {
        try {
            return em.find(Usuarios.class, id);
        } catch (NoResultException nre) {
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public Usuarios getUsuarios(String user, String pass) {
        try {
            return (Usuarios) em.createQuery("select u from Usuarios u where u.username = :username and u.password = :pass")
                    .setParameter("username", user)
                    .setParameter("pass", pass)
                    .getSingleResult();
        } catch (NoResultException nre) {
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Usuarios> getUsuarioByUsername(String user) {
        try {
            return em.createQuery("select u from Usuarios u where u.username = :user and u.estado = '" + Constants.ESTADO_ACTIVO + "' ")
                    .setParameter("user", user)
                    .getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList();
        }
    }

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
     * Obtiene una persona en base a un Id de Usuario
     *
     * @param id
     * @return
     */
    public Persona getPersonaByUsuarioId(Long id) {
        try {
            return (Persona) em.createQuery("select p from persona p where p.usuario.idUsuario = :id")
                    .setParameter("id", id)
                    .getSingleResult();
        } catch (NoResultException nre) {
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Persona> getListPersonaUsuarios(Long idUsuario) {
        try {
            return em.createQuery("SELECT p from persona p where p.usuario is not null and p.usuario.idUsuario != :idUsuario and p.usuario.estado != '" + Constants.ESTADO_SINCONFIRMAR + "' order by p.usuario.fechaRegistro DESC")
                    .setParameter("idUsuario", idUsuario)
                    .getResultList();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Usuarios> getListaUsuarios() {
        try {
            return em.createQuery("from Usuarios u order by u.fechaRegistro DESC").getResultList();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public Usuarios getUsuarioByUsernameByEmailConfirmToken(String username, String tokenEmail) {
        try {
            return (Usuarios) em.createQuery("select u from Usuarios u where u.username = :username and u.tokenConfirmacionEmail = :tokenEmail")
                    .setParameter("username", username)
                    .setParameter("tokenEmail", tokenEmail)
                    .getSingleResult();
        } catch (NoResultException nre) {
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public Usuarios getUsuarioByUsernameByCambioPassToken(String username, String tokenCambioPass) {
        try {
            return (Usuarios) em.createQuery("select u from Usuarios u where u.username = :username and u.tokenCambioPass = :tokenCambioPass")
                    .setParameter("username", username)
                    .setParameter("tokenCambioPass", tokenCambioPass)
                    .getSingleResult();
        } catch (NoResultException nre) {
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public Usuarios getUsuarioByTokenCambioPass(String tokenCambioPass) {
        try {
            return (Usuarios) em.createQuery("select u from Usuarios u where u.tokenCambioPass = :tokenCambioPass")
                    .setParameter("tokenCambioPass", tokenCambioPass)
                    .getSingleResult();
        } catch (NoResultException nre) {
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Usuarios> getListaNoActivos() {
        try {
            return em.createQuery("select u from Usuarios u where u.estado = '" + Constants.ESTADO_INACTIVO + "' order by u.idUsuario ").getResultList();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Usuarios> getListaActivos() {
        try {
            return em.createQuery("select u from Usuarios u where u.estado = '" + Constants.ESTADO_ACTIVO + "' order by u.idUsuario ").getResultList();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean isUserAdmin(Long id) {
        List<UsuarioRol> list = this.getUsuarioRolesByUserId(id);
        if (list.isEmpty()) {
            return false;
        } else {
            for (UsuarioRol ur : list) {
                Rol rol = ur.getRol();
                if (Constants.ROL_ADMIN.equals(rol.getIdRol())) {
                    return true;
                }
            }
            return false;
        }
    }

    public List<UsuarioRol> getUsuarioRolesByUserId(Long id) {
        try {

            return em.createQuery("select u from usuario_rol u where u.usuario.idUsuario = :id")
                    .setParameter("id", id)
                    .getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList();
        }
    }

    public Persona getPersonaByUsername(String username) {
        try {

            return (Persona) em.createQuery("select p from persona p where p.usuario.username = :username and p.usuario.estado = '" + Constants.ESTADO_ACTIVO + "'")
                    .setParameter("username", username)
                    .getSingleResult();
        } catch (NoResultException nre) {
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public Franquiciado getFranquiciado(Long idFranquiciado, Long idSponsor) {
        try {
            return (Franquiciado) em.createQuery("select f from franquiciado f where f.persona.idPersona = :idFranquiciado and f.sponsor.idPersona = :idSponsor")
                    .setParameter("idFranquiciado", idFranquiciado)
                    .setParameter("idSponsor", idSponsor)
                    .getSingleResult();
        } catch (NoResultException nre) {
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public Integer getUltimoFranquiciadoBySponsor(Long idSponsor, String brazo) {
        try {
            Integer ultimo = (Integer) em.createQuery("select max(f.numeracion) from franquiciado f where f.sponsor.idPersona = :idSponsor and f.brazo = :brazo group by f.brazo")
                    .setParameter("idSponsor", idSponsor)
                    .setParameter("brazo", brazo)
                    .getSingleResult();         
            
            if ( Util.isEmpty(ultimo) ) {
                ultimo = 0;
            }
            
            return ultimo;
        } catch (NoResultException nre) {
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }
}
