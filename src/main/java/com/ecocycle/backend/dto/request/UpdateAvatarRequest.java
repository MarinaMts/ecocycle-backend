package com.ecocycle.backend.dto.request;

import jakarta.validation.constraints.NotNull;

public class UpdateAvatarRequest {

    @NotNull(message = "O avatar e obrigatorio")
    private String avatar;

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }
}
