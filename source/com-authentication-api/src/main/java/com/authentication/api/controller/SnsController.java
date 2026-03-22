package com.authentication.api.controller;

import com.authentication.api.dto.ApiMessageDto;
import com.authentication.api.exception.NotFoundException;
import com.authentication.api.form.sns.PushNotiRequest;
import com.authentication.api.form.sns.SendSignalSnsForm;
import com.authentication.api.model.Account;
import com.authentication.api.repository.AccountRepository;
import com.authentication.api.service.SnsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/v1/sns")
@CrossOrigin(origins = "*", allowedHeaders = "*")
@Slf4j
public class SnsController extends ABasicController {
    @Autowired
    private SnsService snsService;

    @Autowired
    private AccountRepository accountRepository;

    @PostMapping(value = "/send-signal", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiMessageDto<Void> sendSignal(@Valid @RequestBody SendSignalSnsForm form, BindingResult bindingResult) {
        Account account = accountRepository.findById(form.getUserId())
                        .orElseThrow(() -> new NotFoundException("Account not found"));
        PushNotiRequest pushNotiRequest = new PushNotiRequest();
        pushNotiRequest.setUserId(form.getUserId());
        pushNotiRequest.setTenantId(form.getTenantId());
        pushNotiRequest.setUserKind(account.getKind());
        pushNotiRequest.setMessage(form.getPayload());
        snsService.sendSignal("BROADCAST", pushNotiRequest);
        return makeSuccessResponse("Send signal successfully");
    }
}
