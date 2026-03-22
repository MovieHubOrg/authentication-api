package com.authentication.api.service;

import com.authentication.api.form.sns.BaseSendSignalForm;
import com.authentication.api.service.rabbit.RabbitService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class SnsService {
    @Value("${rabbitmq.app}")
    private String appName;

    @Value("${rabbitmq.sns.broadcast.queue}")
    private String queueName;

    @Autowired
    private RabbitService rabbitService;

    public <T> void sendSignal(String cmd, T data) {
        BaseSendSignalForm<T> form = new BaseSendSignalForm<>();
        form.setCmd(cmd);
        form.setData(data);

        rabbitService.handleSendMsg(
                appName,
                1 + "_" + queueName,
                data,
                cmd,
                null,
                null,
                null,
                null
        );
    }
}
