package com.example.mavenproject;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;

import de.westnordost.osmapi.ApiResponseReader;
import de.westnordost.osmapi.map.data.OsmWay;
import de.westnordost.osmapi.map.data.Way;

class StreetReader implements ApiResponseReader<Way[]> {

	@Override
	public Way[] parse(InputStream in) throws Exception {
		Document doc = DocumentReader.getData(in);
		ArrayList<Way> ways = new ArrayList<>();
		
		NodeList nl = doc.getElementsByTagName("way");
		for (int i = 0; i < nl.getLength(); i++) {
	    	Element item = (Element) nl.item(i);
			NamedNodeMap attributes = item.getAttributes();
			long id = Long.parseLong(attributes.getNamedItem("id").getNodeValue());
//	    	Node center = item.getElementsByTagName("center").item(0);
	    	NodeList tagList = item.getElementsByTagName("tag");
	    	NodeList nds = item.getElementsByTagName("nd");
	    	LinkedHashMap<String, String> tags = new LinkedHashMap<>();
	    	for (int j = 0; j < tagList.getLength(); j++) {
	    		NamedNodeMap tagAttr = tagList.item(j).getAttributes();
	    		tags.put(tagAttr.getNamedItem("k").getNodeValue(), tagAttr.getNamedItem("v").getNodeValue());
	    	}
	    	ArrayList<Long> nodes = new ArrayList<>();
	    	for (int j = 0; j < nds.getLength(); j++) {
	    		Long nid = Long.parseLong(nds.item(j).getAttributes().getNamedItem("ref").getNodeValue());
	    		nodes.add(nid);
	    	}
	    	OsmWay curr = new OsmWay(id, 0, nodes, tags);
	    	ways.add(curr);
	    }
		
		Way[] ans = new Way[ways.size()];
		ways.toArray(ans);
		return ans;
	}

}
