package net.zyuiop.loupgarou.client;

import javafx.application.Application;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.stage.Stage;
import net.zyuiop.loupgarou.client.gui.LoginWindow;
import net.zyuiop.loupgarou.client.net.NetworkManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayDeque;
import java.util.Optional;

/**
 * @author zyuiop
 */
public class LGClient extends Application {
	public static final Logger logger = LogManager.getRootLogger();
	public static final AuthenticationService authService = new RSAAuthenticationService(new File(System.getProperty("user.dir")));

	public static void main(String[] args) throws NoSuchAlgorithmException {
		launch(args);
	}

	private NetworkManager networkManager = null;

	@Override
	public void start(Stage primaryStage) throws Exception {
		new LoginWindow(this).show();
	}

	@Override
	public void stop() throws Exception {
		super.stop();
		if (networkManager != null)
			networkManager.stop();
		System.exit(0);
	}

	public void connect(String address, String username) {
		logger.info("Trying to connect to " + address + " with username " + username);

		if (!authService.hasKey(username)) {
			Dialog keyInfo = new Dialog();
			keyInfo.setTitle("Clés RSA");
			ButtonType ok = new ButtonType("Compris !", ButtonBar.ButtonData.OK_DONE);
			keyInfo.setContentText("LoupGarou utilise des clés RSA pour vous\nidentifier sur les serveurs en ligne.");
			keyInfo.getDialogPane().getButtonTypes().add(ok);
			// TODO : more info about what key does
			keyInfo.show();

			authService.generateKeyPair(username);
		}

		String[] parts = address.split(":");
		String ip = parts[0];
		int port = 2325;
		if (parts.length > 1) {
			try {
				port = Integer.parseInt(parts[1]);
			} catch (Exception e) {
				Alert error = new Alert(Alert.AlertType.ERROR, "Le numéro de port entré est invalide !", new ButtonType("Fermer", ButtonBar.ButtonData.CANCEL_CLOSE));
				error.showAndWait();
				System.exit(0);
				return;
			}
		}

		networkManager = new NetworkManager(username, ip, port);
		try {
			networkManager.connect();
		} catch (Exception e) {
			networkManager.closeWindow();
			logger.error("Exception while connecting to " + address + " : ", e);

			ButtonType cancel = new ButtonType("Annuler", ButtonBar.ButtonData.CANCEL_CLOSE);
			ButtonType retry = new ButtonType("Réessayer", ButtonBar.ButtonData.YES);

			Alert error = new Alert(Alert.AlertType.ERROR, "Impossible de se connecter au serveur : \n" + e.getMessage(), retry, cancel);
			Optional<ButtonType> result = error.showAndWait();
			if (result.isPresent() && result.get() == retry) {
				connect(address, username);
			} else {
				System.exit(0);
			}
		}
	}
}
