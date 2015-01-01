package com.mmscn.utils;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class MapDecorer {
	private Map<String, String> mMap;

	public MapDecorer(Map<String, String> map) {
		this.mMap = map;
	}

	public final Map<String, String> getMap() {
		return mMap;
	}

	public static List<Map<String, String>> involute(
			List<? extends MapDecorer> decorers) {
		List<Map<String, String>> l = new ArrayList<Map<String, String>>();
		for (MapDecorer decorer : decorers)
			l.add(decorer.getMap());
		return l;
	}

	public static <T extends MapDecorer> List<T> createListFromMapList(
			Class<T> clazz, List<Map<String, String>> mapList) {
		List<T> l = new ArrayList<T>();
		if (null != mapList)
			try {
				Constructor<T> c = clazz.getDeclaredConstructor(Map.class);
				c.setAccessible(true);
				for (Map<String, String> map : mapList) {
					l.add(c.newInstance(map));
				}
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				e.printStackTrace();
			}
		return l;
	}

}
