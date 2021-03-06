package de.pesacraft.cannonfight.util.shop.upgrade;

import java.util.Map;

import com.mongodb.BasicDBObject;

public class Upgrade<T> extends BasicDBObject {
	/**
	 * 1: The first version extending BasicDBObject
	 */
	private static final long serialVersionUID = 1L;

	public Upgrade(int price, T value) {
		put("price", price);
		put("value", value);
	}
	
	public Upgrade(Map<String, Object> map) {
		put("price", map.get("price"));
		put("value", map.get("value"));
	}

	public int getPrice() {
		return getInt("price");
	}
	
	@SuppressWarnings("unchecked")
	public T getValue() {
		return (T) get("value");
	}
	
	public void setPrice(int price) {
		put("price", price);
	}
	
	public void setValue(T value) {
		put("value", value);
	}
	
	@Override
	public Upgrade<T> clone() {
		return new Upgrade<T>(getPrice(), getValue());
	}
}
