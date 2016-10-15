package py.minicubic.enem.services.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SignatureException;
import java.security.SecureRandom;
import java.util.Date;
import javax.xml.bind.DatatypeConverter;

/**
 *
 * @author xergio
 */
public class Util {

    public static boolean isEmpty(Object o) {
        return (o == null) || ("".equals(o));
    }

    /**
     * Crea un token de autenticación
     *
     * @param subject
     * @param name
     * @return
     */
    public static String createToken(Long id) {

        long nowMillis = System.currentTimeMillis();
        Date now = new Date(nowMillis);

        //Let's set the JWT Claims
        JwtBuilder builder = Jwts.builder()
                .setIssuedAt(now)
                .setSubject(id.toString())
                //.setExpiration(new Date(Constants.EXP_TOKEN))
                .signWith(SignatureAlgorithm.HS256, Constants.SECRET_KEY);

        //Builds the JWT and serializes it to a compact, URL-safe string
        return builder.compact();
    }

    /**
     * Verifica el token
     *
     * @param token
     * @return
     * @throws SignatureException
     */
    public static boolean verifyToken(String token) {

        boolean valid = true;

        try {
            Util.getClaims(token);
        } catch (Exception e) {
            //e.printStackTrace();
            valid = false;
        }

        return valid;
    }

    /**
     * Obtiene los claims del token
     *
     * @param token
     * @return
     * @throws SignatureException
     */
    public static Claims getClaims(String token) throws SignatureException, ExpiredJwtException {

        Claims claims = Jwts.parser()
                .setSigningKey(DatatypeConverter.parseBase64Binary(Constants.SECRET_KEY))
                .parseClaimsJws(token).getBody();

        return claims;
    }

    /**
     * Genera un random hash de longitud variable
     * @param len
     * @return 
     */
    public static String randomString(int len) {
        SecureRandom rnd = new SecureRandom();
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            sb.append(Constants.ABab.charAt(rnd.nextInt(Constants.ABab.length())));
        }
        return sb.toString();
    }
}
