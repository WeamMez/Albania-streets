package com.example.mavenproject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.Set;

import com.opencsv.CSVWriter;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

import de.westnordost.osmapi.*;
import de.westnordost.osmapi.map.data.Element;
import de.westnordost.osmapi.map.data.Node;
import de.westnordost.osmapi.map.data.Way;
import de.westnordost.osmapi.overpass.*;

/**
 * Hello world!
 *
 */
public class App {

	static OverpassMapDataApi overpass;

	public static void main(String[] args) {
		OsmConnection connection = new OsmConnection("https://overpass-api.de/api/interpreter", "my user agent");
		overpass = new OverpassMapDataApi(connection);
		getResultsAndSave(getInt(args, 0), getInt(args, 1));

	}

	private static int getInt(String[] args, int i) {
		return (i < args.length) ? Integer.parseInt(args[i]) : Integer.MAX_VALUE;
	}

	static <T extends Element> T[] downloadWithRetry(String query, int retries, ApiResponseReader<T[]> reader) {
		if (retries == 0)
			return null;
		try {
			return overpass.query(query, reader);
		} catch (Exception e) {
			System.out.printf("Download for query %s failed. Retrying...\n", query);
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
			return downloadWithRetry(query, retries - 1, reader);
		}
	}

	private static CityList getCities() {
		Node[] result = downloadWithRetry(
				"area[\"ISO3166-1:alpha2\"=\"AL\"]; node[place~\"city|town|village\"](area); out;", 10,
				new NodeReader());
		if (result == null)
			return null;
		CityList cities = new CityList();

		for (Node n : result) {
			if (n != null) {
				City c = new City(n);
//				System.out.println(c);
				if (c.getName() != null)
					cities.add(c);
			}
		}

		System.out.printf("Search returned %d cities in Albania.\n", cities.size());

		return (cities);
	}

	private static String streetToString(LinkedHashMap<String, String> street) {
		StringBuilder sb = new StringBuilder("{");
		for (Entry<String, String> entry : street.entrySet()) {
			sb.append(String.format("'%s': '%s', ", entry.getKey(), entry.getValue()));
		}
		sb.delete(sb.length() - 2, sb.length());
		sb.append("}");
		return sb.toString();
	}

	private static void getResultsAndSave(int limitCities, int limitStreets) {
		CityList cities = getCities();
		if (cities == null || cities.isEmpty()) {
			System.out.println("Couldn't download data for cities. Exiting...");
			return;
		}

		ArrayList<LinkedHashMap<String, String>> streets = getStreetsForMultiple(cities, limitCities, limitStreets);
		if (streets.size() > 0) {
			System.out.printf("Saving %d rows to results.csv...\n", streets.size());
			try (FileOutputStream fos = new FileOutputStream("results.csv")) {
				OutputStreamWriter osw = new OutputStreamWriter(fos, StandardCharsets.UTF_8);
				CSVWriter writer = new CSVWriter(osw);
				Set<String> headerSet = streets.get(0).keySet();
				String[] headers = new String[headerSet.size()];
				headerSet.toArray(headers);
				writer.writeNext(headers);
				for (LinkedHashMap<String, String> st : streets) {
					Collection<String> stVals = st.values();
					String[] vals = new String[stVals.size()];
					stVals.toArray(vals);
					writer.writeNext(vals);
				}
				writer.close();
				System.out.println("Done.");
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			System.out.println("No streets to write. Exiting...");
		}
	}

	private static ArrayList<LinkedHashMap<String, String>> getStreetsForMultiple(CityList cities, int limitCities,
			int limitStreets) {

		ArrayList<LinkedHashMap<String, String>> streets = new ArrayList<>();
		int count = 0;
		Iterator<City> it = cities.iterator();
		while (it.hasNext() && count < limitCities) {
			streets.addAll(getStreetsFor(it.next(), limitStreets));
			count++;
		}

		return streets;
	}

	private static Collection<LinkedHashMap<String, String>> getStreetsFor(City city, int limitStreets) {
		Way[] result = downloadWithRetry(
				String.format("area[name=\"%s\"]; way(area)[highway][name]; out;", city.getName()), 10,
				new StreetReader());
		if (result == null)
			return new ArrayList<>();
		limitStreets = Math.min(limitStreets, result.length);

		System.out.printf("Search for streets in %s returned %d streets.\n", city.getName(), result.length);

		HashMap<String, LinkedHashMap<String, String>> ans = new HashMap<>();

		for (int i = 0; i < limitStreets; i++) {
			String name = result[i].getTags().get("name");
			if (name == null || name.isEmpty())
				continue;

			LinkedHashMap<String, String> street = new LinkedHashMap<>();

			street.put("name", City.toPascalCase(name));
			if (ans.containsKey(street.get("name")))
				continue;
			street.put("city", city.getName());
			street.put("postcode", getPostalCodeFor(city));
			street.put("region1", city.getDistrict());
			street.put("region2", city.getPrefecture());
			street.put("region3", city.getRegion());

			System.out.println(streetToString(street));
			ans.put(street.get("name"), street);
		}
		if (ans.size() > 0) {
			int numWithPostcode = 0;
			for (LinkedHashMap<String, String> st : ans.values())
				if (st.get("postcode") != null)
					numWithPostcode++;
			System.out.printf("Found %s postcodes out of %d.\n", numWithPostcode, ans.size());
		}
		return ans.values();
	}

	private static String getPostalCodeFor(City city) {
		String ans = city.getPostalCode();
		if (ans != null)
			System.out.printf("Found postal code %s for city %s.\n", ans, city.getName());
		else
			System.out.printf("No postal code found for city %s.\n", city.getName());
		return ans;
	}
}
