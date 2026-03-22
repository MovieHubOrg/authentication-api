package com.authentication.api.service;

import com.authentication.api.form.file.DeleteListFileForm;
import com.authentication.api.service.feign.FeignFileMediaService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
public class MediaService {
    @Autowired
    private FeignFileMediaService feignFileMediaService;

    public void deleteFile(String filePath) {
        if (StringUtils.isNotBlank(filePath)) {
            feignFileMediaService.deleteListFile(new DeleteListFileForm(Collections.singletonList(filePath)));
        }
    }

    public void deleteFiles(DeleteListFileForm deleteListFileForm) {
        if (deleteListFileForm != null && !deleteListFileForm.getFiles().isEmpty()) {
            feignFileMediaService.deleteListFile(deleteListFileForm);
        }
    }

    public void deleteFiles(List<String> filePaths) {
        if (filePaths != null && !filePaths.isEmpty()) {
            List<String> filesToDelete = new ArrayList<>();
            for (String filePath : filePaths) {
                if (StringUtils.isNotBlank(filePath)) {
                    filesToDelete.add(filePath);
                }
            }
            if (!filesToDelete.isEmpty()) {
                feignFileMediaService.deleteListFile(new DeleteListFileForm(filesToDelete));
            }
        }
    }
}