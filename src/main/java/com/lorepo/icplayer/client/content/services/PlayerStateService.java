package com.lorepo.icplayer.client.content.services;

import com.lorepo.icplayer.client.module.api.IPlayerStateService;
import com.lorepo.icplayer.client.module.api.player.IPlayerServices;

public class PlayerStateService implements IPlayerStateService {
    public enum Mode {
        WORK,
        CHECK_ANSWERS,
        SHOW_ANSWERS,
        LIMITED_CHECK_ANSWERS,
        LIMITED_SHOW_ANSWERS,
        GRADUAL_SHOW_ANSWERS,
    }

    private Mode currentMode = Mode.WORK;
    private IPlayerServices services;

    public PlayerStateService(IPlayerServices services) {
        this.services = services;
    }

    @Override
    public boolean isCheckErrorsMode() {
        return currentMode == Mode.CHECK_ANSWERS;
    }

    @Override
    public boolean isShowAnswersMode() {
        return currentMode == Mode.SHOW_ANSWERS;
    }

    @Override
    public boolean isLimitedShowAnswersMode() {
        return currentMode == Mode.LIMITED_SHOW_ANSWERS;
    }

    @Override
    public boolean isLimitedCheckAnswersMode() {
        return currentMode == Mode.LIMITED_CHECK_ANSWERS;
    }

    @Override
    public boolean isGradualShowAnswersMode() {
        return currentMode == Mode.GRADUAL_SHOW_ANSWERS;
    }

    @Override
    public boolean isWorkMode() {
        return currentMode == Mode.WORK;
    }

    @Override
    public void setCheckErrorsMode(boolean value) {
        if (value) {
            this.currentMode = Mode.CHECK_ANSWERS;
        } else if (this.isCheckErrorsMode()) {
            this.currentMode = Mode.WORK;
        }
    }

    @Override
    public void setShowAnswersMode(boolean value) {
        if (value) {
            this.currentMode = Mode.SHOW_ANSWERS;
        } else if (this.isShowAnswersMode()) {
            this.currentMode = Mode.WORK;
        }
    }

    @Override
    public void setLimitedShowAnswersMode(boolean value) {
        if (value) {
            this.currentMode = Mode.LIMITED_SHOW_ANSWERS;
        } else if (this.isLimitedShowAnswersMode()) {
            this.currentMode = Mode.WORK;
        }
    }

    @Override
    public void setLimitedCheckAnswersMode(boolean value) {
        if (value) {
            this.currentMode = Mode.LIMITED_CHECK_ANSWERS;
        } else if (this.isLimitedCheckAnswersMode()) {
            this.currentMode = Mode.WORK;
        }
    }

    @Override
    public void setGradualShowAnswersMode(boolean value) {
        if (value) {
            this.currentMode = Mode.GRADUAL_SHOW_ANSWERS;
        } else if (this.isGradualShowAnswersMode()) {
            this.currentMode = Mode.WORK;
        }
    }

    @Override
    public void setWorkMode() {
        this.currentMode = Mode.WORK;
    }
}
