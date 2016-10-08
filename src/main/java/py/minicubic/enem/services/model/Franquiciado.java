/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
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
 * @author hectorvillalba
 */

@Entity(name= "franquiciado")
public class Franquiciado implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idfranquiciado", nullable = false)
    @Getter
    @Setter
    private Long idFranquiciado;
    
    @ManyToOne
    @JoinColumn(name = "idpersona" , referencedColumnName = "idPersona")
    @Getter
    @Setter
    private Persona persona;
    
    @ManyToOne
    @JoinColumn(name = "idsponsor" , referencedColumnName = "idPersona")
    @Getter
    @Setter
    private Persona sponsor;
    
    @Column(name = "brazo")
    @Getter
    @Setter
    private String brazo;
    
    @Column(name = "numeracion")
    @Getter
    @Setter
    private Integer numeracion;
    
}
