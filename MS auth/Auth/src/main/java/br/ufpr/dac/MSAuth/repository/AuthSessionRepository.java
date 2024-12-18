package br.ufpr.dac.MSAuth.repository;

import br.ufpr.dac.MSAuth.model.AuthSession;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface AuthSessionRepository extends MongoRepository<AuthSession, String> {
    Optional<AuthSession> findByToken(String token);
    Optional<AuthSession> findByEmail(String email);
    void deleteByToken(String token);
    Optional<AuthSession> findByRefreshToken(String refreshToken);
}