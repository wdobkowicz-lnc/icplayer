package com.lorepo.icplayer.client.module.api.player;

import com.google.gwt.core.client.JsArray;
import com.lorepo.icplayer.client.model.adaptive.AdaptiveConnection;
import com.lorepo.icplayer.client.model.page.Page;

public interface IAdaptiveLearningService {
	public JsArray<AdaptiveConnection> getConnectionsForPage(String pageID);
	public JsArray<AdaptiveConnection> getConnectionsForPage();
	public void addNextPage(String pageID);
	public boolean isNextPageAvailable();
	public void moveToNextPage();
	public void moveToPrevPage();
	public void resetHistory();
	
	public String getStateAsString();
	public void loadFromString(String state);
}
