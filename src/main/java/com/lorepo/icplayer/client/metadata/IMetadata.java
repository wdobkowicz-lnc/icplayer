package com.lorepo.icplayer.client.metadata;
import java.util.Set;

import com.google.gwt.xml.client.Element;

public interface IMetadata {
	public String getValue(String key);
	public void clear();
	public void put(String key, String value);
	public void remove(String key);
	public boolean hasEntries();
	public Set<String> getKeys();
	
	public void parse(Element rootXML);
	public Element toXML();
	
}
