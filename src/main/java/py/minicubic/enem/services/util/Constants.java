package py.minicubic.enem.services.util;

/**
 *
 * @author xergio
 */
public final class Constants {
    
    public static final String SOCIO_DATA = "datosSocio";
    
    public static final String SECRET_KEY = "enemedu.com";
    
    public static final long EXP_TOKEN = System.currentTimeMillis() + 18000000; // 30 minutos 1800000 60000
    
    public static final String MSG_401 = "NO ESTA AUTORIZADO A VER ESTE RECURSO. INICIE SESION.";
    public static final String MSG_ERR_LOGIN = "ACCESO INCORRECTO. INTENTE NUEVAMENTE.";
    
    public static final String BOOLEAN_TRUE = "SI";
    public static final String BOOLEAN_FALSE = "NO";
    
    public static final String ABab = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    
    public static final String ESTADO_INACTIVO = "INACTIVO";
    public static final String ESTADO_SINCONFIRMAR = "SINCONFIRMAR";
    public static final String ESTADO_ACTIVO = "ACTIVO";
    public static final String HOST_NAME_SMTP = "server1.hostipy.com";
    public static final String PUERT0_SMTP = "465";
    public static final String USER_NAME_SMTP = "info@enemedu.com";
    public static final String PASS_SMTP = "SoyInfoEnem*";
    
}
