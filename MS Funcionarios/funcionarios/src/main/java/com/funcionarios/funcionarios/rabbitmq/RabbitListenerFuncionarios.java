package com.funcionarios.funcionarios.rabbitmq;

import java.io.IOException;
import java.util.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.*;
import org.springframework.stereotype.Service;
import org.springframework.amqp.core.Message;

import com.funcionarios.funcionarios.models.dto.LoginDTO;
import com.funcionarios.funcionarios.services.FuncionarioService;
import com.funcionarios.funcionarios.services.VerificationResult;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.rabbitmq.client.Channel;


@Service
public class RabbitListenerFuncionarios {

    private static final Logger logger = LoggerFactory.getLogger(RabbitListenerFuncionarios.class);

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private FuncionarioService funcionarioService;

    @RabbitListener(queues = "employee.verification", ackMode = "MANUAL")
    public void verifyEmployee(LoginDTO loginDTO, Message message, Channel channel) {
        try {
            logger.info("Received employee verification request for email: {}", loginDTO.getEmail());

            VerificationResult verificationResult = funcionarioService.verificarFuncionario(loginDTO.getEmail(), loginDTO.getSenha());

            if (!verificationResult.isSuccess()) {
                String error = verificationResult.getError();
                if ("email_not_found".equals(error)) {
                    logger.info("Email not found: {}", loginDTO.getEmail());
                    sendVerificationResponse("failure", "Email not found", null, message, channel);
                } else if ("wrong_password".equals(error)) {
                    logger.info("Wrong password for email: {}", loginDTO.getEmail());
                    sendVerificationResponse("failure", "Wrong password", null, message, channel);
                }
                return;
            }

            LoginDTO employeeInfo = verificationResult.getLoginDTO();
            Map<String, String> response = new HashMap<>();
            response.put("id", String.valueOf(employeeInfo.getId()));
            response.put("email", employeeInfo.getEmail());
            response.put("perfil", employeeInfo.getPerfil());
            response.put("statusFunc", employeeInfo.getStatus());
            response.put("status", "success");
            response.put("message", "Authentication successful");

            rabbitTemplate.convertAndSend("saga-exchange", "employee.verification.response", response);
            logger.info("Sent employee verification response for email: {}", employeeInfo.getEmail());

            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            logger.error("Error verifying employee", e);
            try {
                channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, false);
            } catch (IOException ioException) {
                logger.error("Error during message rejection", ioException);
            }
        }
    }


    private void sendVerificationResponse(String status, String message, LoginDTO loginDTO, Message messageObj, Channel channel) {
        Map<String, String> response = new HashMap<>();
        if (loginDTO != null) {
            response.put("id", String.valueOf(loginDTO.getId()));
            response.put("email", loginDTO.getEmail());
            response.put("perfil", loginDTO.getPerfil());
        }
        response.put("status", status);
        response.put("message", message);

        rabbitTemplate.convertAndSend("saga-exchange", "employee.verification.response", response);
        logger.info("Sent employee verification response to saga-exchange with routing key 'employee.verification.response'");
        try {
            channel.basicAck(messageObj.getMessageProperties().getDeliveryTag(), false);
        } catch (IOException e) {
            logger.error("Error during message acknowledgment", e);
        }
    }
}

