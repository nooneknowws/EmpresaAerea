package br.ufpr.dac.MSClientes.rest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.modelmapper.ModelMapper;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import br.ufpr.dac.MSClientes.models.Usuario;
import br.ufpr.dac.MSClientes.models.dto.UsuarioDTO;
import br.ufpr.dac.MSClientes.repository.ClienteRepo;
import br.ufpr.dac.MSClientes.services.ClienteService;

@RestController
@CrossOrigin
public class ClienteRest {
    private static final Logger logger = LoggerFactory.getLogger(ClienteRest.class);
    
    @Autowired
    private ClienteRepo clienteRepository;
    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Autowired
    private ClienteService clienteService;
    
    private final ModelMapper modelMapper = new ModelMapper();
    
    @RabbitListener(queues = "client.email.check")  
    public void checkExistence(Map<String, String> request) {
        String email = request.get("email");
        String cpf = request.get("cpf");
        
        boolean emailExists = clienteRepository.findByEmail(email).isPresent();
        boolean cpfExists = clienteRepository.findByCpf(cpf).isPresent();
        
        Map<String, String> response = new HashMap<>();
        response.put("email", email);
        response.put("emailExists", String.valueOf(emailExists));
        response.put("cpfExists", String.valueOf(cpfExists));
        rabbitTemplate.convertAndSend("saga-exchange", "client.email.check.response", response);
    }
    
    @PostMapping("/clientes/cadastro")
    public ResponseEntity<?> inserirCliente(@RequestBody @Validated UsuarioDTO usuarioDTO) {
        try {
            Map<String, Boolean> availability = clienteService
                .verifyRegistrationAvailability(usuarioDTO.getEmail(), usuarioDTO.getCpf())
                .get(30, TimeUnit.SECONDS);

            List<String> conflicts = new ArrayList<>();
            
            if (!availability.get("emailAvailable")) {
                logger.warn("Email already exists: {}", usuarioDTO.getEmail());
                conflicts.add("Email já está em uso");
            }

            if (!availability.get("cpfAvailable")) {
                logger.warn("CPF already exists: {}", usuarioDTO.getCpf());
                conflicts.add("CPF já está cadastrado");
            }

            if (!conflicts.isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("status", "CONFLICT");
                response.put("messages", conflicts);
                return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
            }

            Usuario usuario = modelMapper.map(usuarioDTO, Usuario.class);
            usuario.setInitialPassword(usuarioDTO.getSenha());

            Usuario savedUsuario = clienteRepository.save(usuario);
            UsuarioDTO savedClienteDTO = modelMapper.map(savedUsuario, UsuarioDTO.class);
            savedClienteDTO.setSenha(null);

            return ResponseEntity.status(HttpStatus.CREATED).body(savedClienteDTO);

        } catch (TimeoutException e) {
            logger.error("Verification timeout for email: {}", usuarioDTO.getEmail());
            throw new ResponseStatusException(
                HttpStatus.REQUEST_TIMEOUT, 
                "Timeout na verificação: " + e.getMessage()
            );
        } catch (Exception e) {
            logger.error("Error in registration: {}", e.getMessage(), e);
            throw new ResponseStatusException(
                HttpStatus.INTERNAL_SERVER_ERROR, 
                "Erro durante o cadastro: " + e.getMessage()
            );
        }
    }
    
    @GetMapping("/clientes/busca")
    List<UsuarioDTO> listarTodos() {
        List<Usuario> lista = clienteRepository.findAll();
        return lista.stream()
            .map(e -> modelMapper.map(e, UsuarioDTO.class))
            .collect(Collectors.toList());
    }

    @GetMapping("/clientes/busca/{id}")
    public ResponseEntity<UsuarioDTO> buscarPorId(@PathVariable("id") Long id) {
        return clienteRepository.findById(id)
            .map(usuario -> ResponseEntity.ok(modelMapper.map(usuario, UsuarioDTO.class)))
            .orElse(ResponseEntity.notFound().build());
    }
}