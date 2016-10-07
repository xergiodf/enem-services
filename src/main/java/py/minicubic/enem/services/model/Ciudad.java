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

@Entity(name = "ciudad")
public class Ciudad implements Serializable{
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idciudad", nullable = false)
    @Getter
    @Setter
    private Long idCiudad;
    
    @Column(name="nombre")
    @Getter
    @Setter
    private String nombre;
    
    @ManyToOne
    @JoinColumn(name = "iddepartamento", referencedColumnName = "idDepartamento")
    @Getter
    @Setter
    private Departamento departamento;
    
    
}
