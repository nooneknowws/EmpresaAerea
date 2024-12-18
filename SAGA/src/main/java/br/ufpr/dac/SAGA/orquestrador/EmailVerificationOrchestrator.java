package br.ufpr.dac.SAGA.orquestrador;

import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.amqp.core.Message;
import com.rabbitmq.client.Channel;

import io.netty.util.concurrent.ScheduledFuture;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class EmailVerificationOrchestrator {

    @Autowired
    private AmqpTemplate rabbitTemplate;
    private Map<String, VerificationRequest> pendingVerifications = new HashMap<>();
    private static final Logger logger = LoggerFactory.getLogger(EmailVerificationOrchestrator.class);

    private static class VerificationRequest {
        final String correlationId;
        final String originQueue;
        final long startTime;
        boolean clientChecked;
        boolean employeeChecked;
        boolean cpfAvailable = true;
        boolean emailAvailable = true;

        public VerificationRequest(String correlationId, String originQueue) {
            this.correlationId = correlationId;
            this.originQueue = originQueue;
            this.startTime = System.currentTimeMillis();
        }
    }

    @RabbitListener(queues = "registration.request", ackMode = "MANUAL")
    public void handleRegistrationRequest(Map<String, Object> request, Message message, Channel channel) {
        String email = (String) request.get("email");
        String correlationId = null;
        try {
            String cpf = (String) request.get("cpf");
            correlationId = message.getMessageProperties().getCorrelationId();
            String replyQueue = message.getMessageProperties().getReplyTo();

            logger.info("[{}] Starting verification flow - email: {}, cpf: {}, replyQueue: {}", 
                correlationId, email, cpf, replyQueue);

            if (pendingVerifications.containsKey(email)) {
                logger.warn("[{}] Duplicate verification request for email: {}", correlationId, email);
                VerificationRequest existingRequest = pendingVerifications.get(email);
                long timePending = System.currentTimeMillis() - existingRequest.startTime;
                logger.info("[{}] Existing request pending for {}ms", correlationId, timePending);
            }

            pendingVerifications.put(email, new VerificationRequest(correlationId, replyQueue));
            
            Map<String, String> checkRequest = new HashMap<>();
            checkRequest.put("email", email);
            checkRequest.put("cpf", cpf);

            logger.info("[{}] Sending check requests to client/employee services", correlationId);
            rabbitTemplate.convertAndSend("saga-exchange", "client.email.check", checkRequest);
            rabbitTemplate.convertAndSend("saga-exchange", "employee.email.check", checkRequest);

            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            logger.error("[{}] Error processing registration request for email: {} - {}", 
                correlationId, email, e.getMessage(), e);
            handleError(message, channel);
        }
    }

    

    @RabbitListener(queues = "client.email.check.response", ackMode = "MANUAL")
    public void handleClientResponse(Map<String, String> response, Message message, Channel channel) {
        processServiceResponse(response, "client", message, channel);
    }

    @RabbitListener(queues = "employee.email.check.response", ackMode = "MANUAL")
    public void handleEmployeeResponse(Map<String, String> response, Message message, Channel channel) {
        processServiceResponse(response, "employee", message, channel);
    }

    private void processServiceResponse(Map<String, String> response, String service, 
            Message message, Channel channel) {
        String email = response.get("email");
        String correlationId = null;
        try {
            VerificationRequest request = pendingVerifications.get(email);
            if (request == null) {
                logger.warn("[Unknown] Received {} response for unknown email: {}", service, email);
                channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
                return;
            }
            correlationId = request.correlationId;
            long elapsedTime = System.currentTimeMillis() - request.startTime;

            logger.info("[{}] Received {} response after {}ms - email: {}", 
                correlationId, service, elapsedTime, email);

            boolean emailExists = Boolean.parseBoolean(response.get("emailExists"));
            boolean cpfExists = Boolean.parseBoolean(response.get("cpfExists"));

            if ("client".equals(service)) {
                request.clientChecked = true;
                logger.info("[{}] Client service check complete - emailExists: {}, cpfExists: {}", 
                    correlationId, emailExists, cpfExists);
            } else {
                request.employeeChecked = true;
                logger.info("[{}] Employee service check complete - emailExists: {}, cpfExists: {}", 
                    correlationId, emailExists, cpfExists);
            }

            if (emailExists) request.emailAvailable = false;
            if (cpfExists) request.cpfAvailable = false;

            if (request.clientChecked && request.employeeChecked) {
                logger.info("[{}] Both services responded - sending final response after {}ms", 
                    correlationId, elapsedTime);
                    
                Map<String, Object> finalResponse = new HashMap<>();
                finalResponse.put("email", email);
                finalResponse.put("emailAvailable", request.emailAvailable);
                finalResponse.put("cpfAvailable", request.cpfAvailable);
                finalResponse.put("correlationId", request.correlationId);

                rabbitTemplate.convertAndSend("", request.originQueue, finalResponse);
                pendingVerifications.remove(email);
            } else {
                logger.info("[{}] Waiting for other service response - client: {}, employee: {}", 
                    correlationId, request.clientChecked, request.employeeChecked);
            }

            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            logger.error("[{}] Error processing {} response for email: {} - {}", 
                correlationId, service, email, e.getMessage(), e);
            handleError(message, channel);
        }
    }

    private void handleError(Message message, Channel channel) {
        try {
            channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, false);
        } catch (IOException e) {
            logger.error("Error handling message rejection", e);
        }
    }
}