package com.lorepo.icplayer.client;

import java.util.HashMap;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.user.client.Window;
import com.lorepo.icf.utils.JavaScriptUtils;
import com.lorepo.icf.utils.URLUtils;
import com.lorepo.icplayer.client.content.services.ScoreService;
import com.lorepo.icplayer.client.content.services.StateService;
import com.lorepo.icplayer.client.model.Content;
import com.lorepo.icplayer.client.model.Page;
import com.lorepo.icplayer.client.model.PageList;
import com.lorepo.icplayer.client.module.api.player.IPlayerServices;
import com.lorepo.icplayer.client.module.api.player.IScoreService;
import com.lorepo.icplayer.client.module.api.player.IStateService;
import com.lorepo.icplayer.client.page.PageController;
import com.lorepo.icplayer.client.page.PagePopupPanel;
import com.lorepo.icplayer.client.ui.PlayerView;
import com.lorepo.icplayer.client.utils.ILoadListener;
import com.lorepo.icplayer.client.utils.XMLLoader;

public class PlayerController implements IPlayerController{

	private	Content				contentModel;
	private PageController		pageController1;
	private PageController		pageController2;
	private PageController		headerController;
	private PageController		footerController;
	private	PlayerView			playerView;
	private long				timeStart = 0;
	private ScoreService		scoreService;
	private StateService		stateService;
	private ILoadListener		pageLoadListener;
	private PagePopupPanel		popupPanel;
	
	
	public PlayerController(Content content, PlayerView view, boolean bookMode){
		
		contentModel = content;
		playerView = view;
		scoreService = new ScoreService();
		stateService = new StateService();
		createPageControllers(bookMode);
	}
	
	
	private void createPageControllers(boolean bookMode) {

		pageController1 = new PageController(this);
		pageController1.setView(playerView.getPageView(0));
		if(bookMode){
			playerView.showTwoPages();
			pageController2 = new PageController(this);
			pageController2.setView(playerView.getPageView(1));
		}
	}

	
	public void initHeaders() {
		if(contentModel.getHeader() != null){
			playerView.showHeader();
			headerController = new PageController(pageController1.getPlayerServices());
			headerController.setView(playerView.getHeaderView());
			headerController.setPage(contentModel.getHeader());
		}
		if(contentModel.getFooter() != null){
			playerView.showFooter();
			footerController = new PageController(pageController1.getPlayerServices());
			footerController.setView(playerView.getFooterView());
			footerController.setPage(contentModel.getFooter());
		}
	}


	public void addPageLoadListener(ILoadListener l){
		pageLoadListener = l;
	}
	
	/**
	 * get current loaded page index
	 * @return
	 */
	public int getCurrentPageIndex(){
		
		int index = 0;
		for(int i = 0; i < contentModel.getPageCount(); i++){
			if(contentModel.getPage(i) == pageController1.getPage()){
				index = i;
				break;
			}
		}
		return index;
	}
	
	
	/**
	 * 
	 * @return model
	 */
	public Content	getModel(){
		return contentModel;
	}
	
	
	public PlayerView getView(){
		return playerView;
	}
	
	
	/**
	 * Przełączenie się na stronę o podanej nazwie
	 * @param pageName
	 * @return true if page found
	 */
	public void switchToPage(String pageName) {

		int index = getModel().getPages().findPageIndexByName(pageName);
		if(index > -1){
			switchToPage(index);
		}
		else{
			Window.alert("Missing page:\n<" + pageName + ">");
		}
	}


	public void switchToPrevPage() {

		PageList pages = contentModel.getPages();
		for(int i = 0; i < pages.size(); i++){
			if(pages.get(i) == pageController1.getPage()){
				int index = i-1;
				if(pageController2 != null){
					index -= 1;
				}
				if(index >= 0){
					switchToPage(index);
				}
				break;
			}
		}
	}


	public void switchToNextPage() {

		PageList pages = contentModel.getPages();
		for(int i = 0; i < pages.size(); i++){
			if(pages.get(i) == pageController1.getPage()){
				int index = i+1;
				if(pageController2 != null){
					index += 1;
				}
				if(index < pages.size()){
					switchToPage(index);
				}
				break;
			}
		}
	}


	/**
	 * Switch to page at given index
	 * @param index
	 */
	public void switchToPage(int index){
		closeCurrentPages();
		Page page;
		if(pageController2 != null && index%2 > 0){
			index -= 1;
		}
		if(index < contentModel.getPages().size()){
			page = contentModel.getPages().get(index);
		}
		else{
			page = contentModel.getPages().get(0);
		}
		switchToPage(page, pageController1);
		if(pageController2 != null && index+1 < contentModel.getPages().size()){
			page = contentModel.getPages().get(index+1);
			switchToPage(page, pageController2);
		}
	}
	
	
	private void switchToPage(Page page, final PageController pageController){

		// Load new page
		String baseUrl = contentModel.getBaseUrl();
		XMLLoader reader = new XMLLoader(page);
		String url = URLUtils.resolveURL(baseUrl, page.getHref());
		playerView.showWaitDialog();
		reader.load(url, new ILoadListener() {
			
			@Override
			public void onFinishedLoading(Object obj) {
				Page page = (Page) obj;
				pageLoaded(page, pageController);
				if(pageLoadListener != null){
					pageLoadListener.onFinishedLoading(obj);
				}
				playerView.hideWaitDialog();
				if(timeStart == 0){
					timeStart = System.currentTimeMillis();
				}
				scrollViewToBeggining();
			}

			@Override
			public void onError(String error) {
				playerView.hideWaitDialog();
				JavaScriptUtils.log("Can't load page: " + error);
			}
		});
			
	}


	private void pageLoaded(Page page, PageController pageController) {
		pageController.setPage(page);
		if(headerController != null){
			headerController.setPage(contentModel.getHeader());
		}
		if(footerController != null){
			footerController.setPage(contentModel.getFooter());
		}
	}

	
	private void scrollViewToBeggining() {
		 
		Scheduler.get().scheduleFinally(new ScheduledCommand() {
			public void execute() {
				Window.scrollTo(0, 0);
			}
		});
	}

	
	private void closeCurrentPages() {
		pageController1.updateScore();
		HashMap<String, String> state = pageController1.getState();
		stateService.addState(state);
		pageController1.closePage();
		
		if(pageController2 != null){
			pageController2.closePage();
		}
	}


	public IPlayerServices getPlayerServices() {
		return pageController1.getPlayerServices();
	}


	public long getTimeElapsed() {
		return (System.currentTimeMillis()-timeStart)/1000;
	}


	public IScoreService getScoreService() {
		return scoreService;
	}


	public IStateService getStateService() {
		return stateService;
	}


	@Override
	public void showPopup(String pageName) {
		Page page  = contentModel.findPageByName(pageName);
		PageController popupPageControler = new PageController(this);
		popupPanel = new PagePopupPanel(getView(), popupPageControler);
		popupPanel.showPage(page, contentModel.getBaseUrl());
	}


	@Override
	public void closePopup() {
		if(popupPanel != null){
			popupPanel.hide();
		}
	}

}
