import {Player} from "./Player.jsm";

export class BasePlayer extends Player {

    constructor($view) {
        super();
        if (this.constructor === BasePlayer)
            throw new Error("Cannot create an instance of BasePlayer abstract class");

        this.$view = $view;
        this.hasRecording = false;
        this.duration = null;
        this.mediaNode = this._createMediaNode();
        this.mediaNode.controls = false;
        this.$view.append(this.mediaNode);
        this.eventBus = null;
        this.sourceID = '';
        this.item = '';

        this._enableEventsHandling();
    }

    setRecording(source) {
        this.mediaNode.src = source;
        this._getDuration()
            .then(duration => {
                this.onDurationChangeCallback(duration);
                this.duration = duration;
                this.hasRecording = true;
            });
    }


    startPlaying() {
        return new Promise(resolve => {
            this.mediaNode.muted = false;
            console.log("start playing");
            if (this.onTimeUpdateCallback) {
                console.log("set event listener");
                //this.onTimeUpdateCallback("hello world");
                this.mediaNode.addEventListener('timeupdate', this.onTimeUpdateCallback);
                this.mediaNode.addEventListener('ended', this.onTimeUpdateCallback);
            }
            if (this._isNotOnlineResources(this.mediaNode.src))
                resolve(this.mediaNode);
            this.mediaNode.play();
        });
    }

    stopPlaying() {
        console.log("stopPlaying");
        return new Promise(resolve => {
            this.mediaNode.pause();
            this.mediaNode.currentTime = 0;
            if (this.onTimeUpdateCallback) {
                this.mediaNode.removeEventListener('timeupdate', this.onTimeUpdateCallback);
                this.mediaNode.removeEventListener('ended', this.onTimeUpdateCallback);
            }
            resolve();
        });
    }

    pausePlaying() {
        console.log("stopPlaying");
        return new Promise(resolve => {
            this.mediaNode.pause();
            if (this.onTimeUpdateCallback) {
                this.mediaNode.removeEventListener('timeupdate', this.onTimeUpdateCallback);
                this.mediaNode.removeEventListener('ended', this.onTimeUpdateCallback);
            }
            resolve();
        });
    }

    startStreaming(stream) {
        this._disableEventsHandling();
        setSrcObject(stream, this.mediaNode);
        this.mediaNode.muted = true;
        this.mediaNode.play();
    }

    stopStreaming() {
        // for some reason Edge doesn't send pause event in stopPlaying
        // and setting stopNextStopEvent to true will cause it to not send stop event after finishing playing recorded sound
        if (!this.mediaNode.paused && !DevicesUtils.isEdge()) {
            this.stopNextStopEvent = true;
        }

        this.stopPlaying();
        this._enableEventsHandling();
    }

    reset() {
        this._disableEventsHandling();
        this.mediaNode.src = "";
        this.mediaNode.remove();
        this.hasRecording = false;
        this.duration = null;
        this.mediaNode = this._createMediaNode();
        this.mediaNode.controls = false;
        this.$view.append(this.mediaNode);
        this._enableEventsHandling();
    }

    destroy() {
        this._disableEventsHandling();
        this.stopPlaying();
        this.mediaNode.src = "";
        this.mediaNode.remove();
        this.hasRecording = null;
        this.duration = null;
        this.$view.remove();
        this.mediaNode = null;
        this.$view = null;
    }

    setEventBus(eventBus, sourceID, item) {
        this.eventBus = eventBus;
        this.sourceID = sourceID;
        this.item = item;
    }

    getCurrentTime() {
        return this.mediaNode.currentTime;
    }

    _enableEventsHandling() {
        const self = this;
        this.mediaNode.onloadstart = () => this.onStartLoadingCallback();
        this.mediaNode.onended = () => this.onEndPlayingCallback();
        this.mediaNode.onplay = () => this._onPlayCallback();
        this.mediaNode.onpause = () => this._onPausedCallback();

        if (this._isMobileSafari())
            this.mediaNode.onloadedmetadata = function () {
                self.onEndLoadingCallback();
            };
        else
            this.mediaNode.oncanplay = () => this.onEndLoadingCallback();
    }

    _disableEventsHandling() {
        this.mediaNode.onloadstart = null;
        this.mediaNode.oncanplay = null;
        this.mediaNode.onended = null;
        this.mediaNode.onplay = () => null;
        this.mediaNode.onpause = () => null;
        this.mediaNode.onloadedmetadata = function () {
        };
    }

    _getDuration() {
        // faster resolution then
        // this.mediaNode.ondurationchange = () => this.onDurationChangeCallback(this.mediaNode.duration)
        return new Promise(resolve => {
                let playerMock = new Audio(this.mediaNode.src);
                playerMock.addEventListener("durationchange", function () {
                    if (this.duration != Infinity) {
                        resolve(this.duration);
                        playerMock.src = "";
                        playerMock.remove();
                    }
                }, false);
                playerMock.load();
                playerMock.currentTime = 24 * 60 * 60; // fake big time
                playerMock.volume = 0;
            }
        )
    }

    _isMobileSafari() {
        return window.DevicesUtils.getBrowserVersion().toLowerCase().indexOf("safari") > -1 && window.MobileUtils.isSafariMobile(navigator.userAgent);
    }

    _isNotOnlineResources(source) {
        return !(source.startsWith("www.")
            || source.startsWith("http://")
            || source.startsWith("https://"));
    }

    _onPlayCallback() {
        this._sendEventCallback('playing');
    }

    _onPausedCallback() {
        if (this.stopNextStopEvent) {
            this.stopNextStopEvent = false;
        } else {
            this._sendEventCallback('stop');
        }
    }

    _sendEventCallback(value) {
        if (this.eventBus) {
            let eventData = {
                'source': this.sourceID,
                'item': this.item,
                'value': value,
                'score': ''
            };
            this.eventBus.sendEvent('ValueChanged', eventData);
        }
    }

    _createMediaNode() {
        throw new Error("GetMediaNode accessor is not implemented");
    }

    _onTimeUpdateEvent(event) {
        console.log("_onTimeUpdateEvent");
        console.log(this);
        if (this.onTimeUpdateCallback) {
            this.onTimeUpdateCallback(event);
        }
    }
}