package com.lorepo.icplayer.client.content.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.ResettableEventBus;
import com.google.gwt.event.shared.SimpleEventBus;
import com.lorepo.icf.utils.JavaScriptUtils;
import com.lorepo.icplayer.client.module.api.event.CustomEvent;
import com.lorepo.icplayer.client.module.api.event.DefinitionEvent;
import com.lorepo.icplayer.client.module.api.event.PageLoadedEvent;
import com.lorepo.icplayer.client.module.api.event.ShowErrorsEvent;
import com.lorepo.icplayer.client.module.api.event.ValueChangedEvent;
import com.lorepo.icplayer.client.module.api.event.dnd.DraggableImage;
import com.lorepo.icplayer.client.module.api.event.dnd.DraggableItem;
import com.lorepo.icplayer.client.module.api.event.dnd.DraggableText;
import com.lorepo.icplayer.client.module.api.event.dnd.ItemConsumedEvent;
import com.lorepo.icplayer.client.module.api.event.dnd.ItemReturnedEvent;
import com.lorepo.icplayer.client.module.api.event.dnd.ItemSelectedEvent;
import com.lorepo.icplayer.client.module.api.player.IPlayerEventBusService;

public class PlayerEventBusService implements IPlayerEventBusService {	
	private static final String ITEM_SELECTED_EVENT_NAME = "ItemSelected";
	private static final String ITEM_CONSUMED_EVENT_NAME = "ItemConsumed";
	private static final String ITEM_RETURNED_EVENT_NAME = "ItemReturned";
	private static final String VALUE_CHANGED_EVENT_NAME = "ValueChanged";
	private static final String DEFINITION_EVENT_NAME = "Definition";
	private static final String PAGE_LOADED_EVENT_NAME = "PageLoaded";
	private static final String SHOW_ERRORS_EVENT_NAME = "ShowErrors";
	
	private final PlayerEventBus eventBus;
	protected final PlayerServices playerServices;
	
	protected final HashMap<String, List<JavaScriptObject>> listeners = new HashMap<String, List<JavaScriptObject>>();
	protected final Map<String, List<JavaScriptObject>> pageLoadedListeners = new LinkedHashMap<String, List<JavaScriptObject>>();
	protected final Map<String, List<JavaScriptObject>> pageLoadedListenersDelayed = new LinkedHashMap<String, List<JavaScriptObject>>();
	protected final HashMap<String, List<JavaScriptObject>> listenersDelayed = new HashMap<String, List<JavaScriptObject>>();

	
	public PlayerEventBusService(PlayerServices services) {
		this.playerServices = services;

		eventBus = new PlayerEventBus(new ResettableEventBus(new SimpleEventBus()));
		eventBus.setPlayerServices(playerServices);

		connectEventHandlers();
	}
	
	@Override
	public PlayerEventBus getEventBus() {
		return this.eventBus;
	}
	
	
	public void fireEvent(String eventName, HashMap<String, String> data) {
		this.fireEvent(listeners, pageLoadedListeners, eventName, data);
		this.fireEvent(listenersDelayed, pageLoadedListenersDelayed, eventName, data);
	}
	
	@Override
	public void resetEventBus() {
		eventBus.removeHandlers();

        listeners.clear();
		listenersDelayed.clear();
		
		pageLoadedListeners.clear();
		pageLoadedListenersDelayed.clear();
		
		connectEventHandlers();
	}
	
	@Override
	public void addEventListener(String eventName, JavaScriptObject listener, boolean isDelayed) {
		if (isDelayed) {
			addEventListener(listenersDelayed, pageLoadedListenersDelayed, eventName, listener);
		} else {
			addEventListener(listeners, pageLoadedListeners, eventName, listener);
		}
	}
	
	@Override
	public void sendEvent(String eventName, JavaScriptObject eventData){

		DraggableItem item;
		GwtEvent<?> event = null;

		String source = JavaScriptUtils.getArrayItemByKey(eventData, "source");
		String type = JavaScriptUtils.getArrayItemByKey(eventData, "type");
		String id = JavaScriptUtils.getArrayItemByKey(eventData, "item");
		String value = JavaScriptUtils.getArrayItemByKey(eventData, "value");
		String score = JavaScriptUtils.getArrayItemByKey(eventData, "score");
		String moduleType = JavaScriptUtils.getArrayItemByKey(eventData, "moduleType");
		String pageId = JavaScriptUtils.getArrayItemByKey(eventData, "pageId");

		if(type.compareTo("image") == 0) {
			item = new DraggableImage(id, value);
		} else {
			item = new DraggableText(id, value);
		}

		if (ITEM_CONSUMED_EVENT_NAME.compareTo(eventName) == 0) {
			event = new ItemConsumedEvent(item);
		} else if (ITEM_RETURNED_EVENT_NAME.compareTo(eventName) == 0) {
			event = new ItemReturnedEvent(item);
		} else if (ITEM_SELECTED_EVENT_NAME.compareTo(eventName) == 0) {
			event = new ItemSelectedEvent(item);
		} else if (VALUE_CHANGED_EVENT_NAME.compareTo(eventName) == 0) {
			event = new ValueChangedEvent(source, id, value, score, pageId, moduleType);
		} else if (DEFINITION_EVENT_NAME.compareTo(eventName) == 0) {
			String word = JavaScriptUtils.getArrayItemByKey(eventData, "word");
			event = new DefinitionEvent(word);
		} else {
			String jsonString = JavaScriptUtils.toJsonString(eventData);
			event = new CustomEvent(eventName, (HashMap<String, String>)JavaScriptUtils.jsonToMap(jsonString));
		}

		if (event != null) {
			eventBus.fireEventFromSource(event, this);
		}
	}
	
	private void connectEventHandlers() {
		eventBus.addHandler(ItemSelectedEvent.TYPE, new ItemSelectedEvent.Handler() {
			@Override
			public void onItemSelected(ItemSelectedEvent event) {
				fireEvent(ITEM_SELECTED_EVENT_NAME, event.getData());
			}
		});

		eventBus.addHandler(ItemConsumedEvent.TYPE, new ItemConsumedEvent.Handler() {
			@Override
			public void onItemConsumed(ItemConsumedEvent event) {
				fireEvent(ITEM_CONSUMED_EVENT_NAME, event.getData());
			}
		});

		eventBus.addHandler(ItemReturnedEvent.TYPE, new ItemReturnedEvent.Handler() {
			@Override
			public void onItemReturned(ItemReturnedEvent event) {
				fireEvent(ITEM_RETURNED_EVENT_NAME, event.getData());
			}
		});

		eventBus.addHandler(ValueChangedEvent.TYPE, new ValueChangedEvent.Handler() {
			@Override
			public void onScoreChanged(ValueChangedEvent event) {
				fireEvent(VALUE_CHANGED_EVENT_NAME, event.getData());
			}
		});

		eventBus.addHandler(DefinitionEvent.TYPE, new DefinitionEvent.Handler() {
			@Override
			public void onDefinitionClicked(DefinitionEvent event) {
				fireEvent(DEFINITION_EVENT_NAME, event.getData());
			}
		});

		eventBus.addHandler(PageLoadedEvent.TYPE, new PageLoadedEvent.Handler() {
			@Override
			public void onPageLoaded(PageLoadedEvent event) {
				fireEvent(PAGE_LOADED_EVENT_NAME, event.getData());
			}
		});

		eventBus.addHandler(CustomEvent.TYPE, new CustomEvent.Handler() {
			@Override
			public void onCustomEventOccurred(CustomEvent event) {
				fireEvent(event.eventName, event.getData());
			}
		});

		eventBus.addHandler(ShowErrorsEvent.TYPE, new ShowErrorsEvent.Handler() {
			@Override
			public void onShowErrors(ShowErrorsEvent event) {
				fireEvent(SHOW_ERRORS_EVENT_NAME, event.getData());
			}
		});
	}

	private void fireEvent(
			HashMap<String, List<JavaScriptObject>> listeners, 
			Map<String, List<JavaScriptObject>> pageLoadedListeners,
			String eventName, HashMap<String, String> data) 
	{
		final JavaScriptObject jsData = JavaScriptUtils.createHashMap(data);

		List<JavaScriptObject> eventListeners = listeners.get(eventName);
		if (eventListeners != null) {
			if (eventName == PAGE_LOADED_EVENT_NAME) {
				final HashMap<String, String> pageLoadedData = new HashMap<String, String>();
				pageLoadedData.putAll(data);

				for (String eventSource : pageLoadedListeners.keySet()) {
					pageLoadedData.put("source", eventSource);
					final JavaScriptObject pageLoadedEventData = JavaScriptUtils.createHashMap(pageLoadedData);

					for (JavaScriptObject listener : pageLoadedListeners.get(eventSource)) {
						onEvent(listener, eventName, pageLoadedEventData);
					}

					pageLoadedListeners.get(eventSource).clear();
				}
				pageLoadedListeners.put(data.get("source"), new ArrayList<JavaScriptObject>());
			}

			for (JavaScriptObject listener : eventListeners) {
				onEvent(listener, eventName, jsData);
			}
		}

	}
	
	private void addEventListener(HashMap<String, List<JavaScriptObject>> listeners, Map<String, List<JavaScriptObject>> pageLoadedListeners,
			String eventName, 
			JavaScriptObject listener) 
	{
		List<JavaScriptObject> eventListeners = listeners.get(eventName);
		if (eventListeners == null) {
			eventListeners = new ArrayList<JavaScriptObject>();
			listeners.put(eventName, eventListeners);
		}

		if (eventName == PAGE_LOADED_EVENT_NAME) {
			for (String eventSource : pageLoadedListeners.keySet()) {
				pageLoadedListeners.get(eventSource).add(listener);
			}
		}

		eventListeners.add(listener);
	}


	private native void onEvent(JavaScriptObject listener, String name, JavaScriptObject data) /*-{
		listener.onEventReceived(name, data);
	}-*/;

}