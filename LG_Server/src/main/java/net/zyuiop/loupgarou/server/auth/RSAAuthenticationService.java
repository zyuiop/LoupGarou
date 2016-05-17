package net.zyuiop.loupgarou.server.auth;

import com.google.common.base.Charsets;
import net.zyuiop.loupgarou.protocol.utils.RSAUtils;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.security.*;

/**
 * @author zyuiop
 */
public class RSAAuthenticationService implements AuthenticationService {
	private final File       keysDirectory;
	private final KeyFactory factory;

	public RSAAuthenticationService(File keysDirectory) throws NoSuchAlgorithmException {
		this.keysDirectory = keysDirectory;
		this.factory = KeyFactory.getInstance("RSA");
		if (!keysDirectory.exists())
			keysDirectory.mkdirs();
	}

	@Override
	public String getStoredPublicKey(String name) {
		File file = new File(keysDirectory, name + ".pub");
		if (file.exists()) {
			try {
				return IOUtils.toString(new FileReader(file));
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			}
		}
		return null;
	}

	@Override
	public boolean isSignatureValid(String name, String key, long timestamp, byte[] signature) {
		String messageToSign = "==AuthTest==" + name + "==" + key + "==" + timestamp;

		try {
			PublicKey publicKey = RSAUtils.getPublicKey(key);
			Signature sign = Signature.getInstance("SHA256withRSA");
			sign.initVerify(publicKey);
			sign.update(messageToSign.getBytes(Charsets.UTF_8));
			return sign.verify(signature);
		} catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public void saveKey(String name, String key) {
		File file = new File(keysDirectory, name + ".pub");
		if (file.exists())
			file.delete();

		try {
			FileWriter fw = new FileWriter(file);
			IOUtils.write(key, fw);
			fw.flush();
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
