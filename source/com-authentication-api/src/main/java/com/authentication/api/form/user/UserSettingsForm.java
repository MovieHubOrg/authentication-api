package com.authentication.api.form.user;

import com.authentication.api.validation.ResolutionConstraint;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

@Getter
@Setter
@ApiModel
public class UserSettingsForm {
    @ApiModelProperty(name = "autoSkipIntro")
    private Boolean autoSkipIntro = false;

    @ApiModelProperty(name = "autoNextEpisode")
    private Boolean autoNextEpisode = false;

    @ResolutionConstraint(allowNull = true)
    @ApiModelProperty(name = "resolution")
    private Integer resolution;

    @Min(value = 0, message = "Brightness must be at least 0")
    @Max(value = 100, message = "Brightness must be at most 100")
    @ApiModelProperty(name = "brightness")
    private Integer brightness;

    @Min(value = 0, message = "Audio must be at least 0")
    @Max(value = 100, message = "Audio must be at most 100")
    @ApiModelProperty(name = "audio")
    private Integer audio;

    @Min(value = 0, message = "playbackSpeed must be at least 0")
    @Max(value = 2, message = "playbackSpeed must be at most 2.0")
    @ApiModelProperty(name = "playbackSpeed")
    private Double playbackSpeed;
}
