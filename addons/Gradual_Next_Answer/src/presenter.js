function AddonGradual_Next_Answer_create() {
    var presenter = function () {
    };

    var classList = {
        HIDE_ANSWERS: "gradual-hide-answers-button",
        BUTTON: "gradual-show-answers-button",
        GRADUAL_ACTIVE: "gradual-show-answers-active"
    }

    presenter.state = {
        isVisible: true,
        isDisabled: false,
        isErrorMode: false,
        isGradualShowAnswers: false
    };
    presenter.playerController = null;

    presenter.validateModel = function (model) {
        var modelValidator = new ModelValidator();

        return modelValidator.validate(model, [
            ModelValidators.utils.FieldRename("Is Visible", "isVisible", ModelValidators.Boolean('isVisible')),
            ModelValidators.utils.FieldRename("Is Disabled", "isDisabled", ModelValidators.Boolean('isDisabled')),
            ModelValidators.utils.FieldRename("Is hide answers", "isHideAnswers", ModelValidators.Boolean('isHideAnswers')),
            ModelValidators.DumbString('ID'),
        ]);
    };

    presenter.run = function(view, model) {
        presenter.presenterLogic(view, model, presenter.isPreview)
    };

    presenter.isDisabled = function () {
        return presenter.state.isDisabled;
    }

    presenter.clickHandler = function AddonGradualNextAnswer_clickHandler(event) {
        if (event !== undefined) {
            event.stopPropagation();
        }

        if (presenter.isDisabled()) return;

        presenter.triggerButtonClickedEvent();
    };

    presenter.triggerButtonClickedEvent = function () {
        if (presenter.playerController == null) return;

        if (presenter.configuration.isHideAnswers) {
            presenter.playerController.getCommands().hideGradualAnswers();
        } else {
            presenter.playerController.getCommands().showNextAnswer();
        }
    }

    presenter.presenterLogic = function (view, model, isPreview) {
        presenter.addonID = model.ID;
        presenter.view = view;

        var validatedModel = presenter.validateModel(model);

        if (!validatedModel.isValid) {
            console.log(validatedModel);
            return;
        }

        presenter.viewElements = {
            button: presenter.view.getElementsByClassName(classList.BUTTON)[0]
        };

        presenter.configuration = validatedModel.value;

        if (presenter.configuration.isHideAnswers) {
            presenter.viewElements.button.classList.add(classList.HIDE_ANSWERS);
        }

        if (!isPreview) {
            presenter.addHandleOfMouseActions();
            presenter.addHandlingGradualShowAnswers();
        }
    }

    presenter.createPreview = function(view, model) {
        presenter.presenterLogic(view, model, true);
    };

    presenter.executeCommand = function (name, params) {
        if (presenter.isErrorCheckingMode) {
            return;
        }

        var commands = {
            'show': presenter.show,
            'hide': presenter.hide,
            'disable': presenter.disable
        };

        return Commands.dispatch(commands, name, params, presenter);
    };

    presenter.getState = function () {
        return {
            isVisible: this.state.isVisible
        }
    }

    presenter.setState = function (state) {
        var parsedState = JSON.parse(state);
        presenter.state.isVisible = parsedState.isVisible;

        if (presenter.state.isVisible) {
            presenter.show();
        } else {
            presenter.hide();
        }
    }

    presenter.show = function () {
        presenter.viewElements.button.style.visibility = 'visible';
    }

    presenter.hide = function () {
        presenter.viewElements.button.style.visibility = 'hidden';
    }

    presenter.disable = function () {
        presenter.state.isDisabled = true;
    }

    presenter.enable = function () {
        presenter.state.isDisabled = false;
    }


    presenter.addHandleOfMouseActions = function () {
        if (MobileUtils.isMobileUserAgent(navigator.userAgent)) {
            presenter.viewElements.button.addEventListener("touch", presenter.clickHandler);
        } else {
            presenter.viewElements.button.addEventListener("click", presenter.clickHandler);
        }
    }

    presenter.addHandlingGradualShowAnswers = function () {
        var eventBus = presenter.playerController.getEventBus();
        eventBus.addEventListener("GradualShowAnswers", this);
        eventBus.addEventListener("GradualHideAnswers", this);
    }

    presenter.onEventReceived = function (eventName, eventData) {
        if (eventName === "GradualShowAnswers") {
            presenter.viewElements.button.classList.add(classList.GRADUAL_ACTIVE);
            presenter.state.isGradualShowAnswers = true;
        } else if (eventName === "GradualHideAnswers") {
            presenter.state.isGradualShowAnswers = false;
            presenter.viewElements.button.classList.remove(classList.GRADUAL_ACTIVE);
        }
    }

    presenter.setPlayerController = function (controller) {
        presenter.playerController = controller;
    };

    presenter.destroy = function AddonGradual_Next_Answer__destroy() {
        presenter.playerController = null;
        presenter.viewElements.button.removeEventListener("click", presenter.clickHandler);
        presenter.viewElements.button.removeEventListener("touch", presenter.clickHandler);
    };

    return presenter;
}

AddonGradual_Next_Answer_create.__supported_player_options__ = {
    interfaceVersion: 2
};