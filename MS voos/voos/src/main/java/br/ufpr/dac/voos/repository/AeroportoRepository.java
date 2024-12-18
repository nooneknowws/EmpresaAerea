package br.ufpr.dac.voos.repository;

import br.ufpr.dac.voos.models.Aeroporto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AeroportoRepository extends JpaRepository<Aeroporto, Long> {
    Optional<Aeroporto> findByCodigo(String codigo);
}
