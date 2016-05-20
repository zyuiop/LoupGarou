package net.zyuiop.loupgarou.protocol.utils;

import java.util.concurrent.TimeUnit;

/**
 * @author zyuiop
 */
public class Expirable<T> {
	private long expiration;
	private T value;

	public Expirable(T value, long ttl) {
		this.value = value;
		this.expiration = System.currentTimeMillis() + ttl;
	}

	public Expirable(T value, long ttl, TimeUnit unit) {
		this(value, unit.toMillis(ttl));
	}

	public T getValue() {
		return System.currentTimeMillis() < expiration ? value : null;
	}
}
