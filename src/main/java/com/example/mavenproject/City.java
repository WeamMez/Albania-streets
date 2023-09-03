package com.example.mavenproject;

import java.util.LinkedHashMap;

import de.westnordost.osmapi.*;
import de.westnordost.osmapi.map.data.Node;
import de.westnordost.osmapi.map.data.Relation;
import de.westnordost.osmapi.overpass.OverpassMapDataApi;

public class City {
	private String name;
	private String district;
	private String prefecture;
	private String region;
	private Node node;

	String postalCode = null;

	public static String toPascalCase(String s) {
		return Character.toUpperCase(s.charAt(0)) + s.substring(1);
	}

	public City(Node node) {
//		System.out.println(node.getTags());
		name = node.getTags().get("name");
		this.node = node;

	}

	private void _getDistrict() {
		String dst = node.getTags().get("addr:district");
		if (dst == null)
			district = null;
		else
			district = toPascalCase(dst);
	}

	private void _getPrefecture() {
		String[] names = { node.getTags().get("prefecture"), node.getTags().get("PREFECTURE") };
		if (names[0] != null)
			prefecture = toPascalCase(names[0]);
		else if (names[1] != null)
			prefecture = toPascalCase(names[1]);
		else
			prefecture = null;
	}

	private void _getRegion() {
		try {
			OsmConnection connection = new OsmConnection("https://overpass-api.de/api/", "my user agent");
			OverpassMapDataApi overpass = new OverpassMapDataApi(connection);
			RelationReader relationReader = new RelationReader();
			Relation[] result = overpass.query(
					String.format("is_in(%s, %s); relation(pivot)[boundary=\"administrative\"][admin_level=4]; out;",
							node.getPosition().getLatitude(), node.getPosition().getLongitude()),
					relationReader);
//			System.out.println(result);
			if (result.length > 0 && result[0] != null)
				region = result[0].getTags().get("name");
			else
				region = null;
		} catch (Exception e) {
			System.out.printf("Couldn't download region for %s. Skipping...\n" + e.getLocalizedMessage() + "\n", name);
			return;
		}

	}

	public void fill() {
		_getDistrict();
		_getPrefecture();
		_getRegion();
	}

	public String toString() {
		return String.format("City(name=\"%s\", district=\"%s\", region=\"%s\")", name, district, region);
	}

	public int hashCode() {
		return name == null ? 0 : name.hashCode();
	}

	public boolean equals(City arg0) {
		return this.hashCode() == arg0.hashCode();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDistrict() {
		return district;
	}

	public void setDistrict(String district) {
		this.district = district;
	}

	public String getPrefecture() {
		return prefecture;
	}

	public void setPrefecture(String prefecture) {
		this.prefecture = prefecture;
	}

	public String getRegion() {
		return region;
	}

	public void setRegion(String region) {
		this.region = region;
	}

	public Node getNode() {
		return node;
	}

	public void setNode(Node node) {
		this.node = node;
	}

	public String getPostalCode() {
		if (this.postalCode == null) {
			Relation[] result;
			LinkedHashMap<Long, Relation> relations;
			try {
				result = App.downloadWithRetry(String.format("relation[name=\"%s\"]; out;", name), 1,
						new RelationReader());
				relations = new LinkedHashMap<>();
				for (Relation r : result)
					if (r.getTags().get("name").equals(this.name))
						relations.put(r.getId(), r);
			} catch (Exception e) {
				System.out.printf("Postal code search request failed for city %s. Skipping...\n", name);
				return null;
			}
			if (result.length > 0)
				this.postalCode = relations.get(relations.keySet().iterator().next()).getTags().get("postal_code");
		}

		return this.postalCode;
	}
}
