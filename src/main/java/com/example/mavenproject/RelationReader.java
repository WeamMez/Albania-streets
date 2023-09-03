package com.example.mavenproject;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;
import org.w3c.dom.Element;

import de.westnordost.osmapi.ApiResponseReader;
import de.westnordost.osmapi.map.data.Element.Type;
import de.westnordost.osmapi.map.data.OsmRelation;
import de.westnordost.osmapi.map.data.OsmRelationMember;
import de.westnordost.osmapi.map.data.Relation;
import de.westnordost.osmapi.map.data.RelationMember;

public class RelationReader implements ApiResponseReader<Relation[]> {

	@Override
	public Relation[] parse(InputStream in) throws Exception {
		Document doc = DocumentReader.getData(in);
		ArrayList<Relation> relations = new ArrayList<>();
//		System.out.println(doc.);
		
	    NodeList nl = doc.getElementsByTagName("relation");
	    for (int i = 0; i < nl.getLength(); i++) {
	    	Element item = (Element) nl.item(i);
			NamedNodeMap attributes = item.getAttributes();
			long id = Long.parseLong(attributes.getNamedItem("id").getNodeValue());
//	    	Node center = item.getElementsByTagName("center").item(0);
	    	NodeList tagList = item.getElementsByTagName("tag");
	    	NodeList memberList = item.getElementsByTagName("member");
	    	LinkedHashMap<String, String> tags = new LinkedHashMap<>();
	    	for (int j = 0; j < tagList.getLength(); j++) {
	    		NamedNodeMap tagAttr = tagList.item(j).getAttributes();
	    		tags.put(tagAttr.getNamedItem("k").getNodeValue(), tagAttr.getNamedItem("v").getNodeValue());
	    	}
	    	ArrayList<RelationMember> members = new ArrayList<>();
	    	for (int j = 0; j < memberList.getLength(); j++) {
	    		NamedNodeMap memAttr = memberList.item(j).getAttributes();
	    		members.add(new OsmRelationMember(Long.parseLong(memAttr.getNamedItem("ref").getNodeValue()), memAttr.getNamedItem("role").getNodeValue(), Type.valueOf(memAttr.getNamedItem("type").getNodeValue().toUpperCase())));
	    	}
	    	OsmRelation curr = new OsmRelation(id, 0, members, tags);
	    	relations.add(curr);
	    }

		Relation[] ans = new Relation[relations.size()];
		relations.toArray(ans);
		return ans;
	}

}
