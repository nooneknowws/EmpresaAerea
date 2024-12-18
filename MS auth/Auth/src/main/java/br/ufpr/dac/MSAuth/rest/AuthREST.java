package br.ufpr.dac.MSAuth.rest;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import br.ufpr.dac.MSAuth.model.dto.LoginDTO;
import br.ufpr.dac.MSAuth.service.JwtService;
import br.ufpr.dac.MSAuth.model.AuthSession;
import br.ufpr.dac.MSAuth.repository.AuthSessionRepository;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@CrossOrigin
@RestController
public class AuthREST {
    private static final Logger logger = LoggerFactory.getLogger(AuthREST.class);
    private static final int RABBIT_TIMEOUT_SECONDS = 10;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private AuthSessionRepository authSessionRepository;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginDTO loginDTO) {
        if (!isValidLoginRequest(loginDTO)) {
            logger.warn("Invalid login request received");
            return createErrorResponse("Invalid login credentials", 400);
        }

        logger.info("Processing login request for email: {}", loginDTO.getEmail());

        Optional<AuthSession> existingSession = authSessionRepository.findByEmail(loginDTO.getEmail());
        if (existingSession.isPresent()) {
            authSessionRepository.delete(existingSession.get());
            logger.info("Cleaned up existing session for email: {}", loginDTO.getEmail());
        }

        Map<String, String> authResponse = authenticateViaRabbitMQ(loginDTO);
        if (authResponse == null) {
            return createErrorResponse("Authentication service unavailable", 504);
        }

        if ("success".equals(authResponse.get("status"))) {
            return handleSuccessfulAuth(authResponse);
        } else {
            return createErrorResponse(
                authResponse.getOrDefault("message", "Authentication failed"), 
                401
            );
        }
    }
    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader("x-access-token") String token) {
        if (token == null || token.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Token is required"));
        }

        try {
            if (jwtService.validateAccessToken(token)) {
                String userEmail = jwtService.getEmailFromToken(token);
                Optional<AuthSession> session = authSessionRepository.findByEmail(userEmail);
                
                if (session.isPresent()) {
                    authSessionRepository.delete(session.get());
                    logger.info("Successfully deleted session for user: {}", userEmail);
                    return ResponseEntity.ok(Map.of(
                        "message", "Logout successful",
                        "sessionCleanedUp", true
                    ));
                }
            }
            return ResponseEntity.status(401).body(Map.of("message", "Invalid session"));
        } catch (Exception e) {
            logger.error("Error during logout", e);
            return ResponseEntity.status(500).body(Map.of("message", "Error processing logout"));
        }
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(@RequestBody Map<String, String> request) {
        String refreshToken = request.get("refreshToken");
        
        if (refreshToken == null || refreshToken.trim().isEmpty()) {
            return createErrorResponse("Refresh token is required", 400);
        }

        try {
            if (!jwtService.validateRefreshToken(refreshToken)) {
                return createErrorResponse("Invalid refresh token", 401);
            }

            String email = jwtService.getEmailFromToken(refreshToken);
            Optional<AuthSession> sessionOpt = authSessionRepository.findByEmail(email);
            
            if (sessionOpt.isPresent()) {
                AuthSession session = sessionOpt.get();
                if (refreshToken.equals(session.getRefreshToken())) {
                    String newAccessToken = jwtService.generateAccessToken(email);
                    String newRefreshToken = jwtService.generateRefreshToken(email);
                    Date currentTime = new Date();
                    
                    session.setToken(newAccessToken);
                    session.setRefreshToken(newRefreshToken);
                    session.setLastActivity(currentTime);
                    authSessionRepository.save(session);
                    
                    Map<String, Object> response = new HashMap<>();
                    response.put("status", "success");
                    response.put("token", newAccessToken);
                    response.put("refreshToken", newRefreshToken);
                    response.put("email", email);
                    response.put("id", session.getId());
                    response.put("perfil", session.getPerfil());
                    response.put("statusFunc", session.getStatusFunc());
                    response.put("expiresIn", jwtService.getAccessTokenExpirationMs() / 1000);
                    
                    logger.info("Tokens refreshed successfully for user: {}", email);
                    return ResponseEntity.ok(response);
                }
            }
            
            return createErrorResponse("Invalid session", 401);
        } catch (Exception e) {
            logger.error("Error during token refresh: {}", e.getMessage());
            return createErrorResponse("Error processing refresh token", 500);
        }
    }

    @GetMapping("/session/check")
    public ResponseEntity<?> checkSession(@RequestHeader("x-access-token") String token) {
        if (token == null) {
            return ResponseEntity.status(401).body(Map.of("status", "error", "message", "No token provided"));
        }

        try {
            if (jwtService.validateAccessToken(token)) {
                String email = jwtService.getEmailFromToken(token);
                Optional<AuthSession> session = authSessionRepository.findByEmail(email);
                
                if (session.isPresent() && session.get().getToken().equals(token)) {
                    session.get().setLastActivity(new Date());
                    authSessionRepository.save(session.get());
                    
                    Map<String, Object> response = new HashMap<>();
                    response.put("status", "success");
                    response.put("token", token);
                    response.put("user", session.get());
                    return ResponseEntity.ok(response);
                }
            }
            return ResponseEntity.status(401).body(Map.of("status", "error", "message", "Invalid session"));
        } catch (Exception e) {
            logger.error("Error checking session: {}", e.getMessage());
            return ResponseEntity.status(500).body(Map.of("status", "error", "message", "Error checking session"));
        }
    }

    private ResponseEntity<?> handleSuccessfulAuth(Map<String, String> authResponse) {
        try {
            String id = authResponse.get("id");
            String email = authResponse.get("email");
            String perfil = authResponse.get("perfil");
            String statusFunc = authResponse.get("statusFunc");
            
            if ("Funcionario".equals(perfil) && "INATIVO".equals(statusFunc)) {
                logger.warn("Login attempt by inactive employee: {}", email);
                return createErrorResponse("Funcionario está INATIVO", 404);
            }
            
            String accessToken = jwtService.generateAccessToken(email);
            String refreshToken = jwtService.generateRefreshToken(email);
            Date currentTime = new Date();

            
            AuthSession newSession = new AuthSession(
                id,          
                id,          
                email,       
                accessToken, 
                perfil,      
                statusFunc,  
                refreshToken,
                currentTime,
                currentTime.getTime() 
            );

            authSessionRepository.save(newSession);
            logger.info("Created new session for user: {}", email);

            Map<String, Object> response = new HashMap<>();
            response.put("id", id);
            response.put("status", "success");
            response.put("token", accessToken);
            response.put("refreshToken", refreshToken);
            response.put("email", email);
            response.put("perfil", perfil);
            response.put("statusFunc", statusFunc);
            response.put("expiresIn", jwtService.getAccessTokenExpirationMs() / 1000);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error creating user session: {}", e.getMessage());
            return createErrorResponse("Error creating user session", 500);
        }
    }
    private boolean isValidLoginRequest(LoginDTO loginDTO) {
        return loginDTO != null 
            && loginDTO.getEmail() != null 
            && !loginDTO.getEmail().trim().isEmpty()
            && loginDTO.getSenha() != null 
            && !loginDTO.getSenha().trim().isEmpty();
    }

    private Map<String, String> authenticateViaRabbitMQ(LoginDTO loginDTO) {
        try {
            rabbitTemplate.convertAndSend("saga-exchange", "auth.request", loginDTO);
            logger.info("Sent authentication request to RabbitMQ");

            Message responseMessage = rabbitTemplate.receive(
                "auth.response", 
                TimeUnit.SECONDS.toMillis(RABBIT_TIMEOUT_SECONDS)
            );

            if (responseMessage == null) {
                logger.error("No response received from authentication service");
                return null;
            }

            @SuppressWarnings("unchecked")
            Map<String, String> response = (Map<String, String>) rabbitTemplate
                .getMessageConverter()
                .fromMessage(responseMessage);

            logger.info("Received authentication response for email: {}", loginDTO.getEmail());
            return response;

        } catch (Exception e) {
            logger.error("Error during RabbitMQ communication: {}", e.getMessage());
            return null;
        }
    }

    private ResponseEntity<?> createErrorResponse(String message, int statusCode) {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("status", "error");
        errorResponse.put("message", message);
        return ResponseEntity.status(statusCode).body(errorResponse);
    }
}