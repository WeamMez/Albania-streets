package com.example.mavenproject;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;

public class CityList implements Iterable<City> {

	private LinkedHashMap<String, City> list;

	public CityList(Collection<City> col) {
		this();
		for (City c : col)
			list.put(c.getName(), c);
	}

	public CityList() {
		list = new LinkedHashMap<>();
	}

	@Override
	public Iterator<City> iterator() {

		return new CityIterator(this);
	}

	Iterator<City> innerIterator() {
		return list.values().iterator();
	}
	
	public boolean isEmpty() {
		return list.isEmpty();
	}
	
	public City add(City city) {
		return list.put(city.getName(), city);
	}
	
	public int size() {
		return list.size();
	}
}
