/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package py.minicubic.enem.services.dto;
import lombok.Getter;
import lombok.Setter;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author hectorvillalba
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class UsuariosDTO<T> {
        
    @Getter
    @Setter
    private Long idUsuario;

    @Getter
    @Setter
    private String usuario;

    @Getter
    @Setter
    private String password;
    
    @Getter
    @Setter
    private String newPassword;

    @Getter
    @Setter
    private boolean activo;
    
    @Getter
    @Setter
    private String fechaRegistro;
    
    @Getter
    @Setter
    private String avatar;
    
    @Getter
    @Setter
    private String nombreCompleto;
    
    @Getter
    @Setter
    private boolean admin;
    
    @Getter
    @Setter
    private String tokenCambioPass;
    
    @Getter
    @Setter
    private String token;
    
    @Getter
    @Setter
    private String direccion;
    
    @Getter
    @Setter
    private String sponsor;
    
    @Getter
    @Setter
    private String genero;
    
    @Getter
    @Setter
    private String fechaNacimiento;
    
    @Getter
    @Setter
    private String nroDocumento;
    
    @Getter
    @Setter
    private String ruc;
    
    @Getter
    @Setter
    private String telefono;
    
    @Getter
    @Setter
    private String celular;
    
    @Getter
    @Setter
    private String lugarDireccion;
    
    @Getter
    @Setter
    private String ciudad;
    
    @Getter
    @Setter
    private String mail;
}
