/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package py.minicubic.enem.services.model;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
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

@Entity(name = "persona")
public class Persona implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idpersona", nullable = false)
    @Getter
    @Setter
    private Long idPersona;
    
    @Column(name = "nombres", nullable = false)
    @Getter
    @Setter
    private String nombres;
    
    @Column(name = "apellidos")
    @Getter
    @Setter
    private String apellidos;
    
    @Column(name = "nrodocumento")
    @Getter
    @Setter
    private Long nroDocumento;
    
    @Column(name = "fechanacimiento")
    @Getter
    @Setter
    private Date fechaNacimiento;
    
    @Column(name = "email")
    @Getter
    @Setter
    private String email;
    
    @Column(name = "direccion")
    @Getter
    @Setter
    private String direccion;
    
    @Column(name = "celular")
    @Getter
    @Setter
    private String celular;
    
    @Column(name = "telefono")
    @Getter
    @Setter
    private String telefono;
    
    @Column(name = "ruc")
    @Getter
    @Setter
    private String ruc;
    
    @Column(name = "idsponsor", nullable = false)
    @Getter
    @Setter
    private Long idSponsor;
    
    @Column(name = "genero")
    @Getter
    @Setter
    private String genero;
    
    @ManyToOne
    @JoinColumn(name = "idusuario" ,referencedColumnName = "idusuario")
    @Getter
    @Setter
    private Usuarios usuario;
    
    @ManyToOne
    @JoinColumn(name = "idciudad" ,referencedColumnName = "idCiudad")
    @Getter
    @Setter
    private Ciudad ciudad;
}
