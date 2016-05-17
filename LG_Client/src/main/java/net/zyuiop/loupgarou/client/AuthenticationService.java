package net.zyuiop.loupgarou.client;

/**
 * @author zyuiop
 */
public interface AuthenticationService {
	boolean hasKey();

	String getPublicKey();

	byte[] signData(long timestamp, String name);

	boolean generateKeyPair();
}
