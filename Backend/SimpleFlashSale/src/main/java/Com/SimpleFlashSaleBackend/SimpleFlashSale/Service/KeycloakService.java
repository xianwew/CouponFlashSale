package Com.SimpleFlashSaleBackend.SimpleFlashSale.Service;

import Com.SimpleFlashSaleBackend.SimpleFlashSale.Dto.UserDTO;
import com.nimbusds.jose.shaded.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class KeycloakService {

    private static final Logger log = LoggerFactory.getLogger(KeycloakService.class);

    @Value("${keycloak.auth-server-url}")
    private String keycloakUrl;

    @Value("${keycloak.realm}")
    private String realm;

    @Value("${keycloak.client-id}")
    private String clientId;

    @Value("${keycloak.client-secret}")
    private String clientSecret;

    @Value("${keycloak.admin-username}")
    private String adminUsername;

    @Value("${keycloak.admin-password}")
    private String adminPassword;
//    // ✅ User self-registration (No admin token required)

    private final RestTemplate restTemplate;

    public KeycloakService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    // ✅ Extract Keycloak User ID from JWT
    private String extractUserIdFromToken(String token) {
        String[] parts = token.split("\\.");
        if (parts.length < 2) {
            throw new RuntimeException("Invalid token format");
        }

        String payload = new String(Base64.getDecoder().decode(parts[1]));
        Map claims = new Gson().fromJson(payload, Map.class);
        return (String) claims.get("sub");
    }

    public String getAdminToken() {
        String url = keycloakUrl + "/realms/master/protocol/openid-connect/token";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "password");
        body.add("client_id", "admin-cli");
        body.add("username", adminUsername);
        body.add("password", adminPassword);

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(body, headers);

        try {
            log.info("Getting admin token from Keycloak: {}", url);
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, entity, Map.class);
            return (String) response.getBody().get("access_token");
        } catch (Exception e) {
            log.error("Failed to get admin token from Keycloak.", e);
            throw new RuntimeException("Failed to get admin token.");
        }
    }

    // ✅ Register User in Keycloak
    public String registerUser(UserDTO userDTO) {
        String token = getAdminToken();
        String url = keycloakUrl + "/admin/realms/" + realm + "/users";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);

        Map<String, Object> credentials = new HashMap<>();
        credentials.put("type", "password");
        credentials.put("value", userDTO.getPassword());
        credentials.put("temporary", false);

        Map<String, Object> body = new HashMap<>();
        body.put("username", userDTO.getUsername());
        body.put("email", userDTO.getEmail());
        body.put("enabled", true);
        body.put("credentials", new Map[]{credentials});

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        try {
            log.info("Creating user in Keycloak: {}", url);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                // ✅ Extract user ID from response headers
                String locationHeader = response.getHeaders().getFirst("Location");
                if (locationHeader != null) {
                    String userId = locationHeader.substring(locationHeader.lastIndexOf("/") + 1);
                    log.info("User created successfully in Keycloak. ID: {}", userId);
                    return userId;
                } else {
                    throw new RuntimeException("Failed to retrieve user ID from Keycloak response.");
                }
            } else {
                log.error("Failed to create user in Keycloak. Status: {}, Response: {}",
                        response.getStatusCode(), response.getBody());
                throw new RuntimeException("Failed to create user.");
            }
        } catch (Exception e) {
            log.error("Error while creating user in Keycloak.", e);
            throw new RuntimeException(e);
        }
    }

    // ✅ User login and get access token
    public String getUserToken(String username, String password) {
        String url = keycloakUrl + "/realms/" + realm + "/protocol/openid-connect/token";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("client_id", clientId);
        body.add("client_secret", clientSecret);
        body.add("grant_type", "password");
        body.add("username", username);
        body.add("password", password);

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(body, headers);

        try {
            log.info("Requesting user token from Keycloak. URL: {}", url);
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, entity, Map.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                log.info("Successfully retrieved user token.");
                return (String) response.getBody().get("access_token");
            } else {
                log.error("Failed to retrieve user token. Status: {}, Response: {}",
                        response.getStatusCode(), response.getBody());
                throw new RuntimeException("Failed to retrieve user token from Keycloak.");
            }
        } catch (Exception e) {
            log.error("Exception occurred while retrieving user token from Keycloak.", e);
            throw new RuntimeException("Exception occurred while retrieving user token from Keycloak.", e);
        }
    }

    // ✅ Call Keycloak API to Logout User using Admin Token
    public boolean logoutUser(String userToken) {
        String userId = extractUserIdFromToken(userToken);
        String adminToken = getAdminToken(); // Get Admin Token
        String url = keycloakUrl + "/admin/realms/" + realm + "/users/" + userId + "/logout";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(adminToken); // Use Admin Token

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
            return response.getStatusCode() == HttpStatus.NO_CONTENT;
        } catch (Exception e) {
            log.error("Error logging out user in Keycloak: {}", e.getMessage());
            return false;
        }
    }
}

