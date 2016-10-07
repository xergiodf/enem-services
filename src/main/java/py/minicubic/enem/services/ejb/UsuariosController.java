/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package py.minicubic.enem.services.ejb;

import java.util.List;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import py.minicubic.enem.services.model.Usuarios;

/**
 *
 * @author hectorvillalba
 */

@Stateless
public class UsuariosController {
    
    @PersistenceContext
    EntityManager em;
    
    public List<Usuarios> getUsuarios(String user, String pass){
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
    
    public List<Usuarios> getSponsor(String user){
        try {
                return em.createQuery("select u from Usuarios u where u.username like :username ")
                    .setParameter("username", user)
                    .getResultList();
        } catch (Exception e) {
            return null;
        }
    }
    
        public List<Usuarios> getListaNoActivos(){
        try {
            return em.createQuery("select u from Usuarios u where u.estado = 'NOACTIVO' ").getResultList();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
