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
            return em.createQuery("select c from ciudad c order by c.nombre").getResultList();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    
    public Ciudad getCiudad(Long id){
        try {
           Ciudad ciudad = (Ciudad) em.createQuery("select c from ciudad c where c.idCiudad = :id")
                .setParameter("id", id)
                .getSingleResult();
        return ciudad;
        } catch (Exception e) {
            return null;
        }
    }
}
