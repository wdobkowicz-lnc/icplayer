TestCase("[Coloring] gradualShowAnswers button", {
    setUp: function () {
        this.presenter = AddonColoring_create();
        this.presenter.isShowAnswersActive = false;
        this.presenter.currentAreaIdInGSAMode = 0;
        this.areas = [
            {x: 1, y: 1, defaultColor: [1, 1, 1, 1], colorToFill: [9, 9, 9, 9], type: this.presenter.AREA_TYPE.NORMAL},
            {x: 2, y: 2, defaultColor: [2, 2, 2, 2], colorToFill: [9, 9, 9, 9], type: this.presenter.AREA_TYPE.NORMAL},
            {x: 3, y: 3, defaultColor: [3, 3, 3, 3], colorToFill: [9, 9, 9, 9], type: this.presenter.AREA_TYPE.NORMAL},
            {x: 4, y: 4, defaultColor: [4, 4, 4, 4], colorToFill: [9, 9, 9, 9], type: this.presenter.AREA_TYPE.NORMAL}
        ];
        this.presenter.configuration = {
            isActivity: true,
            areas: this.areas
        };

        this.stubs = {
            fillAreaWithCorrectColorStub: sinon.stub(),
            activateShowAnswersModeStub: sinon.stub()
        };

        this.presenter.fillAreaWithCorrectColor = this.stubs.fillAreaWithCorrectColorStub;
        this.presenter.activateShowAnswersMode = this.stubs.activateShowAnswersModeStub;
    },

    'test should call fillAreaWithCorrectColor once for the first area when showAllAnswersInGradualShowAnswersMode is inactive': function () {
        this.presenter.configuration.showAllAnswersInGradualShowAnswersMode = false;

        this.presenter.gradualShowAnswers();

        var expectedFirstCallArg = this.areas[0];

        assertTrue(this.stubs.fillAreaWithCorrectColorStub.calledOnce);
        assertTrue(this.stubs.fillAreaWithCorrectColorStub.calledWithExactly(expectedFirstCallArg));
    },

    'test should call fillAreaWithCorrectColor multiple times for each area separately when showAllAnswersInGradualShowAnswersMode is active': function () {
        this.presenter.configuration.showAllAnswersInGradualShowAnswersMode = true;

        this.presenter.gradualShowAnswers();

        assertTrue(this.stubs.fillAreaWithCorrectColorStub.callCount === this.areas.length);
        for (var i = 0; i < this.areas.size; i++) {
            var expectedArg = this.areas[i];
            assertTrue(this.stubs.fillAreaWithCorrectColorStub.calledWithExactly(expectedArg));
        }
    },
});
