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
import py.minicubic.enem.services.model.Ciudad;

/**
 *
 * @author hectorvillalba
 */


@Stateless
public class CiudadController {
    @PersistenceContext
    private EntityManager em;
    
    
    public List<Ciudad> listaCiudad(){
        try {
            return em.createQuery("select c from ciudad c").getResultList();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
