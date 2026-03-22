package com.authentication.api.service;

import com.authentication.api.form.file.DeleteListFileForm;
import com.authentication.api.model.Permission;
import com.authentication.api.service.feign.FeignFileMediaService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class BaseApiService {
    @Autowired
    BaseOTPService baseOTPService;

    @Autowired
    CommonAsyncService commonAsyncService;

    @Autowired
    FeignFileMediaService feignFileMediaService;

    private Map<String, Long> storeQRCodeRandom = new ConcurrentHashMap<>();

    public void deleteFile(DeleteListFileForm deleteListFileForm) {
        //call to mediaService for delete
        if (deleteListFileForm != null && !deleteListFileForm.getFiles().isEmpty()) {
            List<String> filesToDelete = new ArrayList<>();
            for (String filePath : deleteListFileForm.getFiles()) {
                if (StringUtils.isNotBlank(filePath)) {
                    filesToDelete.add(filePath);
                }
            }
            if (!filesToDelete.isEmpty()) {
                feignFileMediaService.deleteListFile(new DeleteListFileForm(filesToDelete));
            }
        }
    }


    public String getOTPForgetPassword() {
        return baseOTPService.generate(4);
    }

    public synchronized Long getOrderHash() {
        return Long.parseLong(baseOTPService.generate(9)) + System.currentTimeMillis();
    }


    public void sendEmail(String email, String msg, String subject, boolean html) {
        commonAsyncService.sendEmail(email, msg, subject, html);
    }


    public String convertGroupToUri(List<Permission> permissions) {
        if (permissions != null) {
            StringBuilder builderPermission = new StringBuilder();
            for (Permission p : permissions) {
                builderPermission.append(p.getAction().trim().replace("/v1", "") + ",");
            }
            return builderPermission.toString();
        }
        return null;
    }

    public String getOrderStt(Long storeId) {
        return baseOTPService.orderStt(storeId);
    }


    public synchronized boolean checkCodeValid(String code) {
        //delelete key has valule > 60s
        Set<String> keys = storeQRCodeRandom.keySet();
        Iterator<String> iterator = keys.iterator();
        while (iterator.hasNext()) {
            String key = iterator.next();
            Long value = storeQRCodeRandom.get(key);
            if ((System.currentTimeMillis() - value) > 60000) {
                storeQRCodeRandom.remove(key);
            }
        }

        if (storeQRCodeRandom.containsKey(code)) {
            return false;
        }
        storeQRCodeRandom.put(code, System.currentTimeMillis());
        return true;
    }
}
