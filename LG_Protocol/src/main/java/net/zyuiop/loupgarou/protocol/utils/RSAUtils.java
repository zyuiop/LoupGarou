package net.zyuiop.loupgarou.protocol.utils;

import com.google.common.base.Joiner;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;

/**
 * @author zyuiop
 */
public class RSAUtils {
	private static KeyFactory getFactory() {
		try {
			return KeyFactory.getInstance("RSA");
		} catch (NoSuchAlgorithmException e) {
			return null;
		}
	}

	public static PrivateKey getPrivateKey(String source) {
		String[] parts = source.split("/");
		BigInteger modulus = new BigInteger(parts[0], 36);
		BigInteger exponent = new BigInteger(parts[1], 36);

		RSAPrivateKeySpec spec = new RSAPrivateKeySpec(modulus, exponent);
		try {
			return getFactory().generatePrivate(spec);
		} catch (InvalidKeySpecException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static PublicKey getPublicKey(String source) {
		String[] parts = source.split("/");
		BigInteger modulus = new BigInteger(parts[0], 36);
		BigInteger exponent = new BigInteger(parts[1], 36);

		RSAPublicKeySpec spec = new RSAPublicKeySpec(modulus, exponent);
		try {
			return getFactory().generatePublic(spec);
		} catch (InvalidKeySpecException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static String keyToString(KeySpec key) {
		String[] parts = new String[2];

		if (key instanceof RSAPublicKeySpec) {
			parts[0] = ((RSAPublicKeySpec) key).getModulus().toString(36);
			parts[1] = ((RSAPublicKeySpec) key).getPublicExponent().toString(36);
		} else if (key instanceof RSAPrivateKeySpec) {
			parts[0] = ((RSAPrivateKeySpec) key).getModulus().toString(36);
			parts[1] = ((RSAPrivateKeySpec) key).getPrivateExponent().toString(36);
		}

		return Joiner.on("/").join(parts);
	}
}
