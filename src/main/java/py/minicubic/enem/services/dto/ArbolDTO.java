package py.minicubic.enem.services.dto;

import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author xergio
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class ArbolDTO {
    
    @Getter
    @Setter
    private String red;
    
    @Getter
    @Setter
    private Integer nivel;
}
