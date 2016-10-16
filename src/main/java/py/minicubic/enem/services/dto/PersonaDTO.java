/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package py.minicubic.enem.services.dto;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author hectorvillalba
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class PersonaDTO<T> {

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
    private String fechaNacimiento;
    
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
    private String sponsorUsername;
    
    @Getter
    @Setter
    private String username;
    
    @Getter
    @Setter
    private String password;
    
    @Getter
    @Setter
    private String idCiudad;
    
    @Getter
    @Setter
    private String genero;
           

}
