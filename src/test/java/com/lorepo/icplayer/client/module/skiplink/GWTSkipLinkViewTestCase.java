package com.lorepo.icplayer.client.module.skiplink;


import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.user.client.ui.Widget;
import com.googlecode.gwt.test.GwtModule;
import com.googlecode.gwt.test.GwtTest;
import com.lorepo.icplayer.client.mockup.services.SpeechControllerMockup;
import com.lorepo.icplayer.client.module.skiplink.mocks.SkipLinkModuleMockup;
import com.lorepo.icplayer.client.module.skiplink.mocks.SkipLinkViewListenerMock;
import com.lorepo_patchers.icfoundation.TextToSpeechVoicePatcher;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;


@GwtModule("com.lorepo.icplayer.Icplayer")
public class GWTSkipLinkViewTestCase extends GwtTest {
    SkipLinkModuleMockup moduleMockup;
    SkipLinkView view;
    SkipLinkViewListenerMock listenerMock;
    SpeechControllerMockup speechControllerMockup;

    @Test
    public void viewCreation() {
        givenModelWithoutItems();
        givenViewCreated();

        thenViewInvisible();
        thenChildrenCountEqualTo(0);
    }

    @Test
    public void viewAndItemCreation() {
        givenModelWithOneItem();
        givenViewCreated();

        thenViewInvisible();
        thenChildrenCountEqualTo(1);
    }

    @Test
    public void viewAndItemsCreation() {
        givenModelWithThreeItems();
        givenViewCreated();

        thenViewInvisible();
        thenChildrenCountEqualTo(3);
    }

    @Test
    public void enterPressing() {
        givenModelWithOneItem();
        givenViewCreated();

        whenEnterPressed();

        thenViewVisible();
        thenOnlySelectedItemVisible(0);
    }

    @Test
    public void enterTwicePressing() {
        givenModelWithOneItem();
        givenViewCreated();
        givenViewWithListener();

        whenEnterPressed();
        whenEnterPressed();

        thenViewVisible();
        thenOnlySelectedItemVisible(0);
        thenListenerCalledWithModuleId(moduleMockup.getItemId(0));
    }

    @Test
    public void changingSelectedItemTwice() {
        givenModelWithThreeItems();
        givenViewCreated();
        givenViewWithListener();

        whenEnterPressed();
        whenTabPressed();
        whenTabPressed();
        whenEnterPressed();

        thenViewVisible();
        thenOnlySelectedItemVisible(2);
        thenListenerCalledWithModuleId(moduleMockup.getItemId(2));
    }

    @Test
    public void cyclingSelectedItem() {
        givenModelWithThreeItems();
        givenViewCreated();
        givenViewWithListener();

        whenEnterPressed();
        whenTabPressed();
        whenTabPressed();
        whenShiftTabPressed();
        whenShiftTabPressed();
        whenEnterPressed();

        thenViewVisible();
        thenOnlySelectedItemVisible(0);
        thenListenerCalledWithModuleId(moduleMockup.getItemId(0));
    }

    @Test
    public void exiting() {
        givenModelWithThreeItems();
        givenViewCreated();
        givenViewWithListener();

        whenEnterPressed();
        whenExitPressed();

        thenViewInvisible();
        thenAllItemsInvisible();
    }

    @Test
    public void exitingWithDifferentPaths() {
        givenModelWithThreeItems();
        givenViewCreated();
        givenViewWithListener();

        whenEnterPressed();
        whenShiftTabPressed();
        whenShiftTabPressed();
        whenShiftTabPressed();
        whenTabPressed();
        whenExitPressed();

        thenViewInvisible();
        thenAllItemsInvisible();
    }

    @Test
    public void shiftTabbingWhenInFirstItem() {
        givenModelWithThreeItems();
        givenViewCreated();
        givenViewWithListener();

        whenEnterPressed();
        whenShiftTabPressed();

        thenViewVisible();
        thenOnlySelectedItemVisible(0);
    }

    @Test
    public void readingFirstItem() {
        givenModelWithThreeItems();
        givenViewCreated();

        whenSpeechControllerSet();
        wcagStatusSetTo(true);

        whenEnterPressed();

        thenSpeechTextCreatedWithExpectedText(moduleMockup.getItemText(0));
    }


    @Test
    public void readingSecondItem() {
        givenModelWithThreeItems();
        givenViewCreated();

        whenSpeechControllerSet();
        wcagStatusSetTo(true);

        whenEnterPressed();
        whenTabPressed();

        thenSpeechTextCreatedWithExpectedText(moduleMockup.getItemText(1));
    }

    @Test
    public void notReadingFirstItemWhenWcagFalse() {
        givenModelWithThreeItems();
        givenViewCreated();

        whenSpeechControllerSet();
        wcagStatusSetTo(false);

        whenEnterPressed();

        thenSpeechTextCreatedWithExpectedText(null);
    }

    @Test
    public void userHasShiftedIntoModule() {
        givenModelWithThreeItems();
        givenViewCreated();

        whenShowNavigation();

        thenViewVisible();
        thenOnlySelectedItemVisible(0);
    }

    @Test
    public void userHasShiftedAndLeft() {
        givenModelWithThreeItems();
        givenViewCreated();

        whenShowNavigation();
        whenHideNavigation();

        thenViewInvisible();
        thenAllItemsInvisible();
    }

    @Test
    public void userHasActivatedModuleButControlWasNotPassed() {
        givenModelWithThreeItems();
        givenViewCreated();

        whenShowNavigation();
        whenActivateNavigation();

        thenViewVisible();
        thenAllItemsInvisible();
    }

    @Test
    public void userHasActivatedModule() {
        givenModelWithThreeItems();
        givenViewCreated();

        whenShowNavigation();
        whenActivateNavigation();
        whenEnterPressed();

        thenViewVisible();
        thenOnlySelectedItemVisible(0);
    }

    // this handles KeyboardNavigationController::restoreClasses
    @Test
    public void userHasActivatedModuleAndClassesWereRestored() {
        givenModelWithThreeItems();
        givenViewCreated();

        whenShowNavigation();
        whenActivateNavigation();
        whenEnterPressed();
        whenShowNavigation();
        whenActivateNavigation();


        thenViewVisible();
        thenOnlySelectedItemVisible(0);
    }

    @Test
    public void userHasActivatedModuleTabbedAndClassesWereRestored() {
        givenModelWithThreeItems();
        givenViewCreated();

        whenShowNavigation();
        whenActivateNavigation();

        whenEnterPressed();
        whenShowNavigation();
        whenActivateNavigation();

        whenTabPressed();
        whenShowNavigation();
        whenActivateNavigation();


        thenViewVisible();
        thenOnlySelectedItemVisible(1);
    }

    @Test
    public void userHasActivatedModuleAndDeactivated() {
        givenModelWithThreeItems();
        givenViewCreated();

        whenShowNavigation();
        whenActivateNavigation();
        whenEnterPressed();
        whenDeactivateNavigation();

        thenViewVisible();
        thenOnlySelectedItemVisible(0);
    }

    @Test
    public void userHasActivatedModuleAndLeft() {
        givenModelWithThreeItems();
        givenViewCreated();

        whenShowNavigation();
        whenActivateNavigation();
        whenEnterPressed();
        whenDeactivateNavigation();
        whenHideNavigation();

        thenViewInvisible();
        thenAllItemsInvisible();
    }

    @Test
    public void userHasActivatedModuleAndTabbed() {
        givenModelWithThreeItems();
        givenViewCreated();

        whenShowNavigation();
        whenActivateNavigation();
        whenEnterPressed();
        whenTabPressed();
        whenDeactivateNavigation();
        whenHideNavigation();

        thenViewInvisible();
        thenAllItemsInvisible();
    }


    private void givenModelWithoutItems() {
        moduleMockup = new SkipLinkModuleMockup();
    }

    private void givenModelWithOneItem() {
        moduleMockup = new SkipLinkModuleMockup();
        moduleMockup.addItem(new SkipLinkItem("module1", "module text", "lang tag"));
    }

    private void givenModelWithThreeItems() {
        givenModelWithOneItem();
        moduleMockup.addItem(new SkipLinkItem("module2", "module 2 text", "lang tag"));
        moduleMockup.addItem(new SkipLinkItem("module3", "module 3 text", "lang tag"));
    }

    private void whenSpeechControllerSet() {
        speechControllerMockup = new SpeechControllerMockup();
        view.setSpeechController(speechControllerMockup);
    }

    private void wcagStatusSetTo(boolean value) {
        view.setWCAGStatus(value);
    }

    private void givenViewWithListener() {
        listenerMock = new SkipLinkViewListenerMock();
        view.addListener(listenerMock);
    }

    private void givenViewCreated() {
        view = new SkipLinkView(moduleMockup, false);
    }

    private void whenEnterPressed() {
        KeyDownEvent eventMock = mock(KeyDownEvent.class);

        view.enter(eventMock, false);
    }

    private void whenTabPressed() {
        KeyDownEvent eventMock = mock(KeyDownEvent.class);

        view.tab(eventMock);
    }

    private void whenShiftTabPressed() {
        KeyDownEvent eventMock = mock(KeyDownEvent.class);

        view.shiftTab(eventMock);
    }

    private void whenExitPressed() {
        KeyDownEvent eventMock = mock(KeyDownEvent.class);

        view.escape(eventMock);
    }

    private void whenActivateNavigation() {
        view.activateNavigation("active_navigation");
    }

    private void whenDeactivateNavigation() {
        view.deactivateNavigation("active_navigation");
    }

    private void whenShowNavigation() {
        view.showNavigation("show_navigation");
    }

    private void whenHideNavigation() {
        view.hideNavigation("show_navigation");
    }

    private void thenViewInvisible() {
        assertFalse(view.isVisible());
    }

    private void thenChildrenCountEqualTo(int expectedItemCount) {
        assertEquals(expectedItemCount, view.getWidgetCount());
    }

    private void thenViewVisible() {
        assertTrue(view.isVisible());
    }

    private void thenOnlySelectedItemVisible(int expectedVisibleItemIndex) {

        for (int i = 0; i < view.getWidgetCount(); i++) {
            Widget w = view.getWidget(i);

            if (i == expectedVisibleItemIndex) {
                assertTrue(w.isVisible());
            } else {
                assertFalse(w.isVisible());
            }
        }
    }

    private void thenListenerCalledWithModuleId(String expectedModuleId) {
        assertEquals(expectedModuleId, listenerMock.selectedModuleId);
    }

    private void thenAllItemsInvisible() {
        for (int i = 0; i <view.getWidgetCount(); i++) {
            assertFalse(view.getWidget(i).isVisible());
        }
    }

    private void thenSpeechTextCreatedWithExpectedText(String expectedText) {
        assertEquals(expectedText, TextToSpeechVoicePatcher.lastCreatedItemText());
    }
}
