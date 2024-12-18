package br.ufpr.dac.MSClientes.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import br.ufpr.dac.MSClientes.models.Milhas;

public interface MilhasRepository extends JpaRepository<Milhas, Long> {

	List<Milhas> findByClienteId(Long clienteId);

}
