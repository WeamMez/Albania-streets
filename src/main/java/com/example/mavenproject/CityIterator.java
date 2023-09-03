package com.example.mavenproject;

import java.util.Iterator;

public class CityIterator implements Iterator<City> {
	
	Iterator<City> it;
	
	CityList list;
	
	public CityIterator(CityList list) {
		it = list.innerIterator();
	}

	@Override
	public boolean hasNext() {
		return it.hasNext();
	}

	@Override
	public City next() {
		City ans = it.next();
		ans.fill();
		return ans;
	}
}
