package com.example.mavenproject;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;

import de.westnordost.osmapi.ApiResponseReader;
import de.westnordost.osmapi.map.data.Node;
import de.westnordost.osmapi.map.data.OsmNode;

public class NodeReader implements ApiResponseReader<Node[]> {

	@Override
	public Node[] parse(InputStream in) throws Exception {
		Document doc = DocumentReader.getData(in);
		ArrayList<Node> nodes = new ArrayList<>();
		
	    NodeList nl = doc.getElementsByTagName("node");
	    for (int i = 0; i < nl.getLength(); i++) {
	    	org.w3c.dom.Node item = nl.item(i);
			NamedNodeMap attributes = item.getAttributes();
			long id = Long.parseLong(attributes.getNamedItem("id").getNodeValue());
	    	double lat = Double.parseDouble(attributes.getNamedItem("lat").getNodeValue());
	    	double lon = Double.parseDouble(attributes.getNamedItem("lon").getNodeValue());
	    	NodeList children = item.getChildNodes();
	    	LinkedHashMap<String, String> tags = new LinkedHashMap<>(children.getLength());
	    	for (int j = 0; j < children.getLength(); j++) {
	    		NamedNodeMap tag = children.item(j).getAttributes();
	    		if (tag != null)
	    			tags.put(tag.getNamedItem("k").getNodeValue(), tag.getNamedItem("v").getNodeValue());
	    	}
	    	nodes.add(new OsmNode(id, 0, lat, lon, tags));
	    }

		Node[] ans = new Node[nodes.size()];
		nodes.toArray(ans);
		return ans;
	}
	
}
