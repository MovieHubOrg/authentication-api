package com.authentication.api.form.file;

import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import java.util.List;

@Getter
@Setter
@ApiModel
@AllArgsConstructor
public class DeleteListFileForm {
    @NotNull(message = "files cannot be null!")
    private List<String> files;
}
