package py.minicubic.enem.services.ejb;

import java.util.logging.Logger;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import py.minicubic.enem.services.model.Nodo;
import py.minicubic.enem.services.model.Persona;
import py.minicubic.enem.services.util.Constants;
import py.minicubic.enem.services.util.Util;

/**
 *
 * @author hectorvillalba
 */
@Stateless
public class ArbolController {

    static final Logger LOG = Logger.getLogger("UsuariosRest");

    @PersistenceContext
    private EntityManager em;

    public Nodo getArbolByPersona(Long idPersona) {
        try {
            return (Nodo) em.createQuery("select n from nodo n where n.persona.idPersona = :idPersona")
                    .setParameter("idPersona", idPersona)
                    .getSingleResult();
        } catch (NoResultException nre) {
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void guardarNodo(Long idPersona, String direccion, Nodo nodo) {
        if (Constants.DIR_DERECHA.equals(direccion)) {
            if (Util.isEmpty(nodo.getNodoDerecha())) {

                LOG.info("Entro en nodo derecha y guardo");

                // Si el nodo derecho está vacio, agregamos ahi
                // buscamos la persona
                Persona persona = (Persona) em.find(Persona.class, idPersona);

                //Creamos el nuevo nodo y guardamos
                Nodo newNodo = new Nodo();
                newNodo.setPersona(persona);

                em.persist(newNodo);

                // Actualizamos el nodo del sponsor
                nodo.setNodoDerecha(newNodo);
                em.merge(nodo);
            } else {

                LOG.info("Entro en nodo derecha y sigo buscando");

                // Bajamos un nivel
                guardarNodo(idPersona, direccion, nodo.getNodoDerecha());
            }
        } else {
            if (Util.isEmpty(nodo.getNodoIzquierda())) {

                LOG.info("Entro en nodo izquierda y guardo");

                // Si el nodo derecho está vacio, agregamos ahi
                // buscamos la persona
                Persona persona = (Persona) em.find(Persona.class, idPersona);

                //Creamos el nuevo nodo y guardamos
                Nodo newNodo = new Nodo();
                newNodo.setPersona(persona);

                em.persist(newNodo);

                // Actualizamos el nodo del sponsor
                nodo.setNodoIzquierda(newNodo);
                em.merge(nodo);
            } else {

                LOG.info("Entro en nodo izquierda y sigo buscando");

                // Bajamos un nivel
                guardarNodo(idPersona, direccion, nodo.getNodoIzquierda());
            }
        }
    }

    public String getNodoArbol(Long idPersonaActual, Long idPersonaPadre) {
        StringBuilder nodoArbol = new StringBuilder();
        Nodo nodo = this.getArbolByPersona(idPersonaActual);
        Persona personaPadre = null;
        String helper = "";

        if (Util.isEmpty(nodo)) {
            LOG.info(String.valueOf(idPersonaActual));
        }

        if (!Util.isEmpty(idPersonaPadre)) {
            personaPadre = (Persona) em.find(Persona.class, idPersonaPadre);
        }

        nodoArbol.append("[{").append(populateArbol(nodo, personaPadre));

        if (!Util.isEmpty(nodo.getNodoIzquierda())
                && !Util.isEmpty(nodo.getNodoDerecha())) {

            nodoArbol.append(",");
            nodoArbol.append("\"children\": ");

            helper = getNodoArbol(nodo.getNodoIzquierda().getPersona().getIdPersona(), nodo.getPersona().getIdPersona());
            nodoArbol.append(helper.substring(0, helper.length() - 1));
            nodoArbol.append(",");
            nodoArbol.append(getNodoArbol(nodo.getNodoDerecha().getPersona().getIdPersona(), nodo.getPersona().getIdPersona()).substring(1));

        } else {
            if (!Util.isEmpty(nodo.getNodoIzquierda())) {
                nodoArbol.append(",");
                nodoArbol.append("\"children\": ");

                helper = getNodoArbol(nodo.getNodoIzquierda().getPersona().getIdPersona(), nodo.getPersona().getIdPersona());
                nodoArbol.append(helper.substring(0, helper.length() - 1));
                nodoArbol.append(",{" + populateArbol(null, nodo.getPersona()) + "}]");
            }

            if (!Util.isEmpty(nodo.getNodoDerecha())) {
                nodoArbol.append(",");
                nodoArbol.append("\"children\": ");
                nodoArbol.append("[{" + populateArbol(null, nodo.getPersona()) + "},");
                nodoArbol.append(getNodoArbol(nodo.getNodoDerecha().getPersona().getIdPersona(), nodo.getPersona().getIdPersona()).substring(1));
            }
        }

        nodoArbol.append("}]");

        return nodoArbol.toString();
    }

    private String populateArbol(Nodo nodo, Persona persona) {
        StringBuilder salida = new StringBuilder();

        if (Util.isEmpty(nodo)) {
            salida.append("\"name\": \"\",");
        } else {
            salida.append("\"name\": \"").append(nodo.getPersona().getUsuario().getUsername()).append("\",");
        }

        if (Util.isEmpty(persona)) {
            salida.append("\"parent\": \"null\"");
        } else {
            salida.append("\"parent\": \"").append(persona.getUsuario().getUsername()).append("\"");
        }
        return salida.toString();
    }
}
