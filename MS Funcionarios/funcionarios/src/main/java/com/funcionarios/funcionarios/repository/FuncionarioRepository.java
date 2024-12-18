package com.funcionarios.funcionarios.repository;

import java.util.Optional;
import com.funcionarios.funcionarios.models.Funcionario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import jakarta.transaction.Transactional;

@Repository
public interface FuncionarioRepository extends JpaRepository<Funcionario, Long> {

  Optional<Funcionario> findByEmail(String email);

Optional<Funcionario> findByCpf(String cpf);
}
