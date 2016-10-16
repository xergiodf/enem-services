/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package py.minicubic.enem.services.model;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author hectorvillalba
 */

@Entity
public class Usuarios implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idusuario", nullable = false)
    @Getter
    @Setter
    private Long idUsuario;
    
    @Basic
    @Column(name = "username")
    @Getter
    @Setter
    private String username;
    
    @Basic
    @Column(name = "password")
    @Getter
    @Setter
    private String password;
    
    @Basic
    @Column(name = "estado")
    @Getter
    @Setter
    private String estado;
    
    @Column(name = "fechaexpiracion")
    @Temporal(TemporalType.DATE)
    @Getter
    @Setter
    private Date fechaExpiracion;

    @Column(name = "fecharegistro", columnDefinition="TIMESTAMP DEFAULT CURRENT_TIMESTAMP", insertable = false, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    @Getter
    @Setter
    private Date fechaRegistro;
    
    @Basic
    @Column(name="tokencambiopass")
    @Getter
    @Setter
    private String tokenCambioPass;
    
    @Basic
    @Column(name="tokenconfirmacionemail")
    @Getter
    @Setter
    private String tokenConfirmacionEmail;
    
    @Basic
    @Column(name="direccionred")
    @Getter
    @Setter
    private String direccionRed;
}
