function getValidPrintableModel (isVisible) {
    return {
        "Block wrong answers": "",
        "Bottom": "",
        "Height": "175",
        "ID": "multiplegap1",
        "ID repeated element": "",
        "Is Tabindex Enabled": "False",
        "Is Visible": "True",
        "Is not an activity": "",
        "Item height": "",
        "Item horizontal align": "",
        "Item spacing": "",
        "Item vertical align": "",
        "Item width": "",
        "Items": [
            {"Answer ID": "Source_list1-3"},
            {"Answer ID": "Source_list1-5"}
        ],
        "Layout": "LTWH",
        "Left": "625",
        "Maximum item count": "",
        "Number of repetitions": "",
        "Orientation": "",
        "Right": "",
        "Source type": "texts",
        "Stretch images?": "",
        "Top": "75",
        "Width": "150",
        "langAttribute": "",
        "printable": "Don't randomize",
        "wrapItems": ""
    }
}

TestCase("[Multiple Gap] Multiple gap printable HTML validation", {
    setUp: function () {
        this.presenter = Addonmultiplegap_create();
        this.model = getValidPrintableModel(true);
        this.showAnswers = false;
        this.printableController = {
            getPrintableContext: sinon.stub()
        };
        this.presenter.setPrintableController(this.printableController);
        this.printableController.getPrintableContext.withArgs("Source_list1").returns({
            items: ["donkey", "pigeon", "zebra", "swan", "dolphin", "duck", "seagull", "tiger"]
        });
        this.state = `{"placeholders":[{"item":"Source_list1-2","value":"pigeon","type":"string"},
        {"item":"Source_list1-3","value":"zebra","type":"string"},
        {"item":"Source_list1-1","value":"donkey","type":"string"}],"isVisible":true}`;
        this.state = this.state.replaceAll("\n","");
        this.state = this.state.replaceAll(" ","");
    },

    'test given printableState is undefined when !showAnswers then return empty': function() {
        //given
        var expectedHtmlValue = `<div id="multiplegap1" class="printable_addon_multiplegap">
                                    <div style="max-width: 150px; min-height: 175px; border: 1px solid; padding: 5px;"></div>
                                </div>`;
        expectedHtmlValue = expectedHtmlValue.replaceAll("\n","");
        expectedHtmlValue = expectedHtmlValue.replaceAll("    ","");

        //actual
        var actualHtmlValue = this.presenter.getPrintableHTML(this.model, this.showAnswers);

        //expected
        assertEquals(expectedHtmlValue, actualHtmlValue);
    },

    'test given printableState is undefined when showAnswers then return with correctAnswers': function() {
        //given
        var expectedHtmlValue = `<div id="multiplegap1" class="printable_addon_multiplegap">
                                    <div style="max-width: 150px; min-height: 175px; border: 1px solid; padding: 5px;">zebra, dolphin</div>
                                </div>`;
        expectedHtmlValue = expectedHtmlValue.replaceAll("\n","");
        expectedHtmlValue = expectedHtmlValue.replaceAll("    ","");

        //actual
        this.showAnswers = true;
        var actualHtmlValue = this.presenter.getPrintableHTML(this.model, this.showAnswers);

        //expected
        assertEquals(expectedHtmlValue, actualHtmlValue);
    },

    'test given printableState when !showAnswers then return with studentAnswers': function() {
        //given
        var expectedHtmlValue = `<div id="multiplegap1" class="printable_addon_multiplegap">
                                    <div style="max-width: 150px; min-height: 175px; border: 1px solid; padding: 5px;">pigeon, zebra, donkey</div>
                                </div>`;
        expectedHtmlValue = expectedHtmlValue.replaceAll("\n","");
        expectedHtmlValue = expectedHtmlValue.replaceAll("    ","");

        //actual
        this.presenter.setPrintableState(this.state);
        var actualHtmlValue = this.presenter.getPrintableHTML(this.model, this.showAnswers);

        //expected
        assertEquals(expectedHtmlValue, actualHtmlValue);
    },

    'test given printableState when showAnswers then return with marked studentAnswers': function() {
        //given
        var expectedHtmlValue = `<div id="multiplegap1" class="printable_addon_multiplegap">
                                    <div style="max-width: 150px; min-height: 175px; border: 1px solid; padding: 5px;">
                                        <div><span class="answerSpan">pigeon</span><span class="inCorrectAnswerSpan"></span>, </div><br>
                                        <div><span class="answerSpan">zebra</span><span class="correctAnswerSpan"></span>, </div><br>
                                        <div><span class="answerSpan">donkey</span><span class="inCorrectAnswerSpan"></span></div><br>
                                    </div>
                                </div>`;
        expectedHtmlValue = expectedHtmlValue.replaceAll("\n","");
        expectedHtmlValue = expectedHtmlValue.replaceAll("    ","");

        //actual
        this.showAnswers = true;
        this.presenter.setPrintableState(this.state);
        var actualHtmlValue = this.presenter.getPrintableHTML(this.model, this.showAnswers);

        //expected
        assertEquals(expectedHtmlValue, actualHtmlValue);
    },
});