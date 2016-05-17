package net.zyuiop.loupgarou.server.auth;

/**
 * @author zyuiop
 */
public interface AuthenticationService {
	String getStoredPublicKey(String name);

	boolean isSignatureValid(String name, String key, long timestamp, byte[] signature);

	void saveKey(String name, String key);
}
