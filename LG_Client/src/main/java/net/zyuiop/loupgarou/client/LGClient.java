package net.zyuiop.loupgarou.client;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.stage.Stage;
import net.zyuiop.loupgarou.client.auth.AuthenticationService;
import net.zyuiop.loupgarou.client.auth.RSAAuthenticationService;
import net.zyuiop.loupgarou.client.data.SavedServers;
import net.zyuiop.loupgarou.client.gui.LoginWindow;
import net.zyuiop.loupgarou.client.net.NetworkManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;

/**
 * @author zyuiop
 */
public class LGClient extends Application {
	public static final Logger                logger      = LogManager.getRootLogger();
	public static final AuthenticationService authService = new RSAAuthenticationService(new File(System.getProperty("user.dir")));
	private static LGClient instance;

	public static void main(String[] args) throws NoSuchAlgorithmException {
		launch(args);
	}

	private NetworkManager networkManager = null;

	public LGClient() {
		instance = this;
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		try {
			SavedServers savedServers = new SavedServers(new File(System.getProperty("user.dir")));
			new LoginWindow(this, savedServers).show();
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	@Override
	public void stop() throws Exception {
		super.stop();
		if (networkManager != null)
			networkManager.stop();
		System.exit(0);
	}

	public void connect(String ip, int port, String username) {
		logger.info("Trying to connect to " + ip + ":" + port + " with username " + username);
		LoginWindow.getInstance().setStatus("Connexion à " + ip + ":" + port + "...");

		if (!authService.hasKey(username)) {
			Dialog keyInfo = new Dialog();
			keyInfo.setTitle("Clés RSA");
			ButtonType ok = new ButtonType("Compris !", ButtonBar.ButtonData.OK_DONE);
			keyInfo.setContentText("LoupGarou utilise des clés RSA pour vous\nidentifier sur les serveurs en ligne.\nSi vous changez d'ordinateur, assurez vous de\nles récuperer !");
			keyInfo.getDialogPane().getButtonTypes().add(ok);
			keyInfo.showAndWait();

			LoginWindow.getInstance().setStatus("Génération des clés RSA...");
			authService.generateKeyPair(username);
		}

		networkManager = new NetworkManager(username, ip, port);
		try {
			networkManager.connect();
		} catch (Exception e) {
			networkManager.closeWindow();
			logger.error("Exception while connecting to " + ip + ":" + port + " : ", e);

			ButtonType cancel = new ButtonType("Annuler", ButtonBar.ButtonData.CANCEL_CLOSE);
			ButtonType retry = new ButtonType("Réessayer", ButtonBar.ButtonData.YES);

			Alert error = new Alert(Alert.AlertType.ERROR, "Impossible de se connecter au serveur : \n" + e.getClass().getName(), retry, cancel);
			Optional<ButtonType> result = error.showAndWait();
			if (result.isPresent() && result.get() == retry) {
				connect(ip, port, username);
			} else {
				LoginWindow.getInstance().setConnecting(false);
			}
		}
	}

	public void disconnect() {
		if (networkManager != null) {
			networkManager.stop();
			networkManager = null;
		}

		if (LoginWindow.getInstance() != null) {
			if (!Platform.isFxApplicationThread()) {
				Platform.runLater(LoginWindow.getInstance()::show);
			} else {
				LoginWindow.getInstance().show();
			}
		}
	}

	public static LGClient getInstance() {
		return instance;
	}
}
