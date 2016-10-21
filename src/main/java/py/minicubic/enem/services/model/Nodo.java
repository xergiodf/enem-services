package py.minicubic.enem.services.model;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author sfernandez
 */
@Entity(name = "nodo")
public class Nodo implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idnodo", nullable = false)
    @Getter
    @Setter
    private Long idNodo;

    @ManyToOne
    @JoinColumn(name = "idnododer", referencedColumnName = "idnodo")
    @Getter
    @Setter
    private Nodo nodoDerecha;

    @ManyToOne
    @JoinColumn(name = "idnodoizq", referencedColumnName = "idnodo")
    @Getter
    @Setter
    private Nodo nodoIzquierda;

    @ManyToOne
    @JoinColumn(name = "idpersona", referencedColumnName = "idpersona")
    @Getter
    @Setter
    private Persona persona;
}
