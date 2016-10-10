package py.minicubic.enem.services.util;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import py.minicubic.enem.services.annotations.LoggedIn;
import py.minicubic.enem.services.ejb.UsuariosController;
import py.minicubic.enem.services.model.Usuarios;

/**
 *
 * @author xergio
 */
@RequestScoped
public class UsuarioProducer {
    
    @Produces
    @RequestScoped
    @LoggedIn
    private Usuarios usuario;
    
    @Inject
    private UsuariosController controller;
    
    @PersistenceContext
    private EntityManager eMgr;
    
    public void handleLoggedInEvent(@Observes @LoggedIn Long id) {
        this.usuario = getUsuario(id);
    }
    
    private Usuarios getUsuario(Long id) {
        return controller.getUsuario(id);
    }
}
