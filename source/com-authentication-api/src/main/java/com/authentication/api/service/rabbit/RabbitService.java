package com.authentication.api.service.rabbit;

import com.authentication.api.form.rabbit.BaseSendMsgForm;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class RabbitService {
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private RabbitSender rabbitSender;
    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Value("${rabbitmq.account.exchange}")
    private String accountExchange;

    public <T> void handleSendMsg(String appName, String queueName, T data, String cmd, String subCmd, String responseCode, String token, String tenantId) {
        BaseSendMsgForm<T> form = new BaseSendMsgForm<>();
        form.setApp(appName);
        form.setCmd(cmd);
        form.setSubCmd(subCmd);
        form.setResponseCode(responseCode);
        form.setData(data);
        form.setToken(token);
        form.setTenantId(tenantId);
        String msg;
        try {
            msg = objectMapper.writeValueAsString(form);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        // create queue if existed
        createQueueIfNotExist(queueName);

        // push msg
        rabbitSender.send(queueName, msg);
    }

    public <T> void handleSendFanout(String appName, T data, String cmd) {
        BaseSendMsgForm<T> form = new BaseSendMsgForm<>();
        form.setApp(appName);
        form.setCmd(cmd);
        form.setData(data);

        String msg;
        try {
            msg = objectMapper.writeValueAsString(form);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        rabbitTemplate.convertAndSend(accountExchange, "", msg);
        log.error("-------> Fanout sent to exchange: {}, cmd: {}", accountExchange, cmd);
    }

    private void createQueueIfNotExist(String queueName) {
        rabbitSender.createQueueIfNotExist(queueName);
    }

}
