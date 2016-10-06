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

@Entity(name = "permisos")
public class Permisos implements Serializable {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idpermiso", nullable = false)
    @Getter
    @Setter
    private Long idpermiso;
    
    @ManyToOne
    @JoinColumn(name = "idUsuarioRol", referencedColumnName = "id_usuario_rol")
    @Getter
    @Setter
    private UsuarioRol usuarioRol;
    
    @Column(name = "pantalla")
    @Getter
    @Setter
    private String pantalla;
    
    @Column(name = "ver")
    @Getter
    @Setter
    private Boolean ver;
    
    @Column(name = "insertar")
    @Getter
    @Setter
    private String insertar;
    
    @Column(name = "modificar")
    @Getter
    @Setter
    private String modificar;
    
    @Column(name = "eliminar")
    @Getter
    @Setter
    private String eliminar;
}
