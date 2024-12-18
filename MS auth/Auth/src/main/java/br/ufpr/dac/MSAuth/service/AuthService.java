package br.ufpr.dac.MSAuth.service;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.ufpr.dac.MSAuth.model.dto.LoginDTO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

@Service
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    @Autowired
    private RabbitTemplate rabbitTemplate;

    public boolean authenticate(String email, String password) {
        LoginDTO loginDTO = new LoginDTO(email, password);
        rabbitTemplate.convertAndSend("saga-exchange", "client.verification", loginDTO);

        logger.info("Sent client verification request for email: {}", loginDTO.getEmail());
        
        return false;
    }
}
