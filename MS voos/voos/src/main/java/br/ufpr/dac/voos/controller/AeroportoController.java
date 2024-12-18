package br.ufpr.dac.voos.controller;

import br.ufpr.dac.voos.models.Aeroporto;
import br.ufpr.dac.voos.enums.EstadosBrasil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

import org.springframework.validation.annotation.Validated;

@RestController
@RequestMapping("/api/aeroportos")
@Validated
public class AeroportoController {

    @Autowired
    private EntityManager entityManager;

    @PostMapping("/")
    @Transactional
    public ResponseEntity<?> createAeroporto(@RequestBody Aeroporto aeroporto) {
        try {
            if (aeroporto.getCodigo() == null || aeroporto.getNome() == null || 
                aeroporto.getCidade() == null || aeroporto.getEstado() == null || 
                aeroporto.getPais() == null) {
                return ResponseEntity
                    .badRequest()
                    .body("Todos os campos são obrigatórios");
            }
            if ("Brasil".equalsIgnoreCase(aeroporto.getPais()) || "Brazil".equalsIgnoreCase(aeroporto.getPais())) {
                try {
                    EstadosBrasil estado = EstadosBrasil.valueOf(aeroporto.getEstado().toUpperCase());
                    aeroporto.setEstado(estado.getSigla());
                } catch (IllegalArgumentException e) {
                    return ResponseEntity
                        .badRequest()
                        .body("Estado inválido para aeroporto brasileiro. Use uma das siglas válidas dos estados.");
                }
            }
            if (aeroporto.getCodigo().length() < 3 || aeroporto.getCodigo().length() > 4) {
                return ResponseEntity
                    .badRequest()
                    .body("Código do aeroporto deve ter entre 3 e 4 caracteres.");
            }
            aeroporto.setCodigo(aeroporto.getCodigo().toUpperCase());
            entityManager.persist(aeroporto);
            
            return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(aeroporto);

        } catch (Exception e) {
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Erro ao salvar aeroporto: " + e.getMessage());
        }
    }

    @GetMapping("/{codigo}")
    public ResponseEntity<?> getAeroporto(@PathVariable String codigo) {
        Aeroporto aeroporto = entityManager
            .createQuery("SELECT a FROM Aeroporto a WHERE a.codigo = :codigo", Aeroporto.class)
            .setParameter("codigo", codigo.toUpperCase())
            .getResultList()
            .stream()
            .findFirst()
            .orElse(null);

        if (aeroporto == null) {
            return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body("Aeroporto não encontrado");
        }
        
        return ResponseEntity.ok(aeroporto);
    }

    @GetMapping
    public ResponseEntity<?> listAeroportos() {
        var aeroportos = entityManager
            .createQuery("SELECT a FROM Aeroporto a", Aeroporto.class)
            .getResultList();
        return ResponseEntity.ok(aeroportos);
    }
}