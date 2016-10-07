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
    private String usuario;

    @Getter
    @Setter
    private String password;

}
