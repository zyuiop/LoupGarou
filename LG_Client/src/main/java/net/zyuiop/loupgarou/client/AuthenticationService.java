package net.zyuiop.loupgarou.client;

/**
 * @author zyuiop
 */
public interface AuthenticationService {
	boolean hasKey(String username);

	String getPublicKey(String username);

	byte[] signData(long timestamp, String username);

	boolean generateKeyPair(String username);
}
