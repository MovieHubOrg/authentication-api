package com.authentication.api.service.feign;

import com.authentication.api.config.CustomFeignConfig;
import com.authentication.api.dto.ApiMessageDto;
import com.authentication.api.dto.setting.SettingDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "movie-svr", url = "${movie.internal.base.url}", configuration = CustomFeignConfig.class)
public interface FeignMovieService {
    @GetMapping(value = "/v1/setting/internal/find-by-key/{keyName}")
    ApiMessageDto<SettingDto> findByKey(@PathVariable("keyName") String keyName,
                                        @RequestHeader(FeignConstant.HEADER_X_API_KEY) String apiKey);
}
