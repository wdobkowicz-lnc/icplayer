export class RecordButton {

    constructor($button, state, timer, recorder, recordTimerLimiter, soundIntensity) {
        this.$button = $button;
        this.state = state;
        this.timer = timer;
        this.recorder = recorder;
        this.recordTimerLimiter = recordTimerLimiter;
        this.soundIntensity = soundIntensity;
    }

    activate() {
        this.$button.click(event => this.eventHandler(event));
    }

    deactivate() {
        this.$button.unbind();
    }

    forceClick() {
        this.$button.click();
    }

    eventHandler(event) {
        if (this.state.isNew() || this.state.isLoaded())
            this.onStartRecording(event);
        else if (this.state.isRecording())
            this.onStopRecording(event)
    }

    onStartRecording(event) {
        this.recorder.startRecording(() => {
            $(event.target).addClass("selected");
            this.state.setRecording();
            this.timer.reset();
            this.timer.startCountdown();
            this.recordTimerLimiter.startCountdown();
        });
    }

    onStopRecording(event) {
        $(event.target).removeClass("selected");
        this.state.setLoaded();
        this.timer.stopCountdown();
        this.recorder.stopRecording();
        this.recordTimerLimiter.stopCountdown();
        this.soundIntensity.closeStream();
    }
}