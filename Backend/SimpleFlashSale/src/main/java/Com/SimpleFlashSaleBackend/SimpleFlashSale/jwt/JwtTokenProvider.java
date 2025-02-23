package Com.SimpleFlashSaleBackend.SimpleFlashSale.jwt;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.security.PublicKey;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Base64;

import io.jsonwebtoken.Claims;

@Component
@Slf4j
public class JwtTokenProvider {

    private PublicKey publicKey;

    @Value("${keycloak.auth-server-url}")
    private String keycloakUrl;

    @Value("${keycloak.realm}")
    private String realm;

    @PostConstruct
    private void init() {
        String jwksUri = keycloakUrl + "/realms/" + realm + "/protocol/openid-connect/certs";
        log.info("Initializing JwtTokenProvider with JWKS URI: {}", jwksUri);
        try {
            this.publicKey = getPublicKeyFromJWKS(jwksUri);
            log.info("Public key successfully retrieved and parsed");
        } catch (Exception e) {
            log.error("Failed to initialize JwtTokenProvider: {}", e.getMessage(), e);
            throw new IllegalStateException("Could not initialize JwtTokenProvider", e);
        }
    }

    private PublicKey getPublicKeyFromJWKS(String jwksUri) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            String response = restTemplate.getForObject(jwksUri, String.class);
            log.debug("JWKS response: {}", response);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonNode = mapper.readTree(response);

            // Find the key with "use": "sig" and "alg": "RS256"
            JsonNode keyNode = null;
            for (JsonNode key : jsonNode.get("keys")) {
                if ("sig".equals(key.get("use").asText()) && "RS256".equals(key.get("alg").asText())) {
                    keyNode = key;
                    break;
                }
            }

            if (keyNode == null) {
                throw new IllegalArgumentException("No suitable key found in JWKS");
            }

            String publicKeyString = keyNode.get("x5c").get(0).asText();
            log.debug("Public key string: {}", publicKeyString);

            // Decode the Base64-encoded certificate and extract the public key
            byte[] decoded = Base64.getDecoder().decode(publicKeyString);
            CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
            X509Certificate certificate = (X509Certificate) certificateFactory.generateCertificate(new java.io.ByteArrayInputStream(decoded));
            return certificate.getPublicKey();
        } catch (Exception e) {
            log.error("Failed to fetch and parse public key from JWKS: {}", e.getMessage(), e);
            throw new IllegalArgumentException("Invalid public key provided", e);
        }
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(publicKey).build().parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            log.error("Invalid token: {}", e.getMessage(), e);
            return false;
        }
    }

    public String getEmailFromToken(String token) {
        Claims claims = Jwts.parserBuilder().setSigningKey(publicKey).build().parseClaimsJws(token).getBody();
        return claims.getSubject();
    }

    public String getRoleFromToken(String token) {
        Claims claims = Jwts.parserBuilder().setSigningKey(publicKey).build().parseClaimsJws(token).getBody();
        return claims.get("role", String.class);
    }

    public String extractUserIdFromToken(String token) {
        Claims claims = Jwts.parserBuilder().setSigningKey(publicKey).build().parseClaimsJws(token).getBody();
        log.info("Token claims: {}", claims);
        return claims.getSubject(); // Typically, the 'sub' claim is used as the user ID
    }
}
