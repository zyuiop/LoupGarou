package net.zyuiop.loupgarou.client.auth;

import com.google.common.base.Charsets;
import net.zyuiop.loupgarou.protocol.utils.RSAUtils;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;

/**
 * @author zyuiop
 */
public class RSAAuthenticationService implements AuthenticationService {
	private final File       workingDirectory;
	private final KeyFactory factory;

	public RSAAuthenticationService(File workingDirectory) {
		this.workingDirectory = new File(workingDirectory, "keys");
		if (!this.workingDirectory.exists())
			this.workingDirectory.mkdir();

		KeyFactory factory;
		try {
			factory = KeyFactory.getInstance("RSA");
		} catch (NoSuchAlgorithmException e) {
			factory = null;
			e.printStackTrace();
		}
		this.factory = factory;
	}

	@Override
	public boolean hasKey(String username) {
		File publicKey = new File(workingDirectory, username + ".pub");
		File privateKey = new File(workingDirectory, username + ".priv");

		return publicKey.exists() && privateKey.exists();
	}

	@Override
	public String getPublicKey(String username) {
		File publicKey = new File(workingDirectory, username + ".pub");
		if (!publicKey.exists())
			generateKeyPair(username);
		if (publicKey.exists())
			try {
				return IOUtils.toString(new FileReader(publicKey));
			} catch (IOException e) {
				return null;
			}
		return null;
	}

	private String getPrivateKey(String username) {
		File privateKey = new File(workingDirectory, username + ".priv");
		if (!privateKey.exists())
			return null;
		if (privateKey.exists())
			try {
				return IOUtils.toString(new FileReader(privateKey));
			} catch (IOException e) {
				return null;
			}
		return null;
	}

	@Override
	public byte[] signData(long timestamp, String name) {
		String key = getPublicKey(name);
		String privateKey = getPrivateKey(name);
		if (key == null || privateKey == null)
			return null;

		String messageToSign = "==AuthTest==" + name + "==" + key + "==" + timestamp;

		try {
			PrivateKey pkey = RSAUtils.getPrivateKey(privateKey);
			Signature signature = Signature.getInstance("SHA256withRSA");
			signature.initSign(pkey);
			signature.update(messageToSign.getBytes(Charsets.UTF_8));
			return signature.sign();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public boolean generateKeyPair(String username) {
		KeyPairGenerator kpg;
		try {
			kpg = KeyPairGenerator.getInstance("RSA");
			kpg.initialize(2048);
			KeyPair kp = kpg.generateKeyPair();

			File publicKey = new File(workingDirectory, username + ".pub");
			File privateKey = new File(workingDirectory, username + ".priv");

			RSAPublicKeySpec pub = factory.getKeySpec(kp.getPublic(), RSAPublicKeySpec.class);
			RSAPrivateKeySpec priv = factory.getKeySpec(kp.getPrivate(), RSAPrivateKeySpec.class);

			FileWriter privWriter = new FileWriter(privateKey);
			FileWriter pubWriter = new FileWriter(publicKey);

			IOUtils.write(RSAUtils.keyToString(priv), privWriter);
			IOUtils.write(RSAUtils.keyToString(pub), pubWriter);

			privWriter.flush();
			privWriter.close();
			pubWriter.flush();
			pubWriter.close();

			return true;
		} catch (NoSuchAlgorithmException | InvalidKeySpecException | IOException e) {
			e.printStackTrace();
		}
		return false;
	}
}
