package br.ufpr.dac.voos.controller;

import br.ufpr.dac.voos.dto.CreateVooDTO;
import br.ufpr.dac.voos.dto.UpdateVooDTO;
import br.ufpr.dac.voos.dto.VooDTO;
import br.ufpr.dac.voos.models.Voo;
import br.ufpr.dac.voos.services.VooService;
import jakarta.persistence.EntityNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/voos")
public class VooController {
    
        @Autowired
        private VooService vooService;
    
        @GetMapping
        public List<VooDTO> listarTodosVoos() {
            List<Voo> voos = vooService.listarTodosVoos();
            return voos.stream().map(VooDTO::new).toList();
        }
        
        @GetMapping("/filter")
        public List<VooDTO> getVoosFiltrados(
            @RequestParam("origem") String origemCodigo,
            @RequestParam("destino") String destinoCodigo
        ) {
            List<Voo> voos = vooService.findByOrigemAndDestino(origemCodigo, destinoCodigo);
            return voos.stream().map(VooDTO::new).toList();
        }
        
        @GetMapping("/{id}")
        public ResponseEntity<VooDTO> listarUmVoo(@PathVariable("id") Long id) {
            Voo voo = vooService.listarUmVoo(id);
            if (voo == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(new VooDTO(voo));
        }
    
        @PostMapping
        public ResponseEntity<Voo> inserirVoo(@RequestBody CreateVooDTO vooDTO) {
            if (vooDTO.getCodigoOrigem() == null || vooDTO.getCodigoDestino() == null) {
                throw new IllegalArgumentException("Códigos de origem e destino são obrigatórios");
            }
            
            Voo novoVoo = new Voo(null, null, null, null, null, null, 0, 0, null);
            novoVoo.setCodigoVoo(vooDTO.getCodigoVoo());
            novoVoo.setDataHoraPartida(vooDTO.getDataHoraPartida());
            novoVoo.setValorPassagem(vooDTO.getValorPassagem());
            novoVoo.setQuantidadeAssentos(vooDTO.getQuantidadeAssentos());
            novoVoo.setQuantidadePassageiros(vooDTO.getQuantidadePassageiros());
            novoVoo.setStatus(vooDTO.getStatus());

            Voo vooCriado = vooService.inserirVoo(novoVoo, vooDTO.getCodigoOrigem(), vooDTO.getCodigoDestino());
            return ResponseEntity.ok(vooCriado);
        }
    
        @PutMapping("/{id}")
        public ResponseEntity<VooDTO> editarVoo(@PathVariable("id") Long id, @RequestBody UpdateVooDTO vooDTO) {
            Voo vooAtualizado = vooService.editarVoo(id, vooDTO);
            return ResponseEntity.ok(new VooDTO(vooAtualizado));
        }
        
        @PatchMapping("/{id}/cancelar")
        public ResponseEntity<Void> cancelarVoo(@PathVariable("id") Long id) {
            try {
                vooService.atualizarStatus(id, "CANCELADO");
                return ResponseEntity.noContent().build();
            } catch (EntityNotFoundException e) {
                return ResponseEntity.notFound().build();
            } catch (IllegalArgumentException | IllegalStateException e) {
                return ResponseEntity.badRequest().build();
            }
        }

        @PatchMapping("/{id}/status")
        public ResponseEntity<Void> atualizarStatus(
            @PathVariable("id") Long id, 
            @RequestParam("status") String status
        ) {
            try {
                vooService.atualizarStatus(id, status);
                return ResponseEntity.noContent().build();
            } catch (EntityNotFoundException e) {
                return ResponseEntity.notFound().build();
            } catch (IllegalArgumentException | IllegalStateException e) {
                return ResponseEntity.badRequest().build();
            }
        }
    
}
