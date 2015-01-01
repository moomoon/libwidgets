package com.mmscn.utils;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.util.Pair;
import android.view.View;
import android.widget.TextView;

public class ViewHolder {
	protected ViewHolder(View v) {
		for (Pair<Inject, Field> pair : getInjectableFields()) {
			final int id = pair.first.value();
			try {
				pair.second.set(ViewHolder.this, v.findViewById(id));
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}
	}

	private List<Pair<Inject, Field>> getInjectableFields() {
		List<Pair<Inject, Field>> l = new ArrayList<Pair<Inject, Field>>();
		for (Field f : ViewHolder.this.getClass().getDeclaredFields()) {
			if (View.class.isAssignableFrom(f.getType()) && f.isAnnotationPresent(Inject.class)) {
				f.setAccessible(true);
				l.add(new Pair<ViewHolder.Inject, Field>(f.getAnnotation(Inject.class), f));
			}
		}
		return l;
	}

	public void injectText(Map<?, ?> map) {
		for (Pair<Inject, Field> pair : getInjectableFields()) {
			String key = pair.first.key();
			if (key.length() == 0) {
				continue;
			}
			Field field = pair.second;
			if (!(TextView.class.isAssignableFrom(field.getType()))) {
				throw new RuntimeException("do not set name on non-text-view fields");
			}
			try {
				((TextView) field.get(ViewHolder.this)).setText(map.get(key).toString());
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}
	}

	@Target(ElementType.FIELD)
	@Retention(RetentionPolicy.RUNTIME)
	protected @interface Inject {
		int value();

		String key() default "";
	}

	@Target(ElementType.TYPE)
	@Retention(RetentionPolicy.RUNTIME)
	public @interface Ignore {

	}
}
