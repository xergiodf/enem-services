/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package py.minicubic.enem.services.model;

import java.io.Serializable;
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

@Entity(name = "usuario_rol")
public class UsuarioRol implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario_rol")
    @Getter
    @Setter
    private Long idUsuarioRol;
    
    @ManyToOne
    @JoinColumn(name = "idrol", referencedColumnName = "idrol")
    @Getter
    @Setter
    private Rol idRol;
    
    @ManyToOne
    @JoinColumn(name = "idusuario", referencedColumnName = "idusuario")
    @Getter
    @Setter
    private Usuarios idUsuario;
}
