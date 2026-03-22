package com.authentication.api.service.feign;

import com.authentication.api.config.CustomFeignConfig;
import com.authentication.api.dto.ApiMessageDto;
import com.authentication.api.form.file.DeleteListFileForm;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "file-media-svr", url = "${media.internal.base.url}", configuration = CustomFeignConfig.class)
public interface FeignFileMediaService {
    @PostMapping(value = "/v1/file/delete-list-file")
    ApiMessageDto<String> deleteListFile(@RequestBody DeleteListFileForm deleteListFileForm);
}
