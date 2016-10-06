/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package py.minicubic.enem.services.dto;

import java.util.Date;
import javax.persistence.Column;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import lombok.Getter;
import lombok.Setter;
import py.minicubic.enem.services.model.Usuarios;

/**
 *
 * @author hectorvillalba
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class PersonaDTO<T> {

    @Getter
    @Setter
    private Long idpersona;
  
    @Getter
    @Setter
    private String nombres;
    
    @Getter
    @Setter
    private String apellidos;
   
    @Getter
    @Setter
    private String nroDocumento;
    
    @Getter
    @Setter
    private Date fechaNacimiento;
    
    @Getter
    @Setter
    private String email;
    
    @Getter
    @Setter
    private String direccion;
    
    @Getter
    @Setter
    private String celular;
    
    @Getter
    @Setter
    private String telefono;
    
    @Getter
    @Setter
    private String ruc;
    
    @Getter
    @Setter
    private String idSponsor;
    
    @Getter
    @Setter
    private String username;
    
    @Getter
    @Setter
    private String pasword;
    

}
