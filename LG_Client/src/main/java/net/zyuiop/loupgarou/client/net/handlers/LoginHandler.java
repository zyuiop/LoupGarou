package net.zyuiop.loupgarou.client.net.handlers;

import javafx.application.Platform;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import net.zyuiop.loupgarou.client.LGClient;
import net.zyuiop.loupgarou.client.PacketHandler;
import net.zyuiop.loupgarou.client.net.NetworkManager;
import net.zyuiop.loupgarou.protocol.packets.clientbound.LoginResponsePacket;

/**
 * @author zyuiop
 */
public class LoginHandler implements PacketHandler<LoginResponsePacket> {
	private final NetworkManager networkManager;

	public LoginHandler(NetworkManager networkManager) {
		this.networkManager = networkManager;
	}

	@Override
	public void handle(LoginResponsePacket packet) {
		if (packet.isSuccess()) {
			networkManager.finishLogin();
		} else {
			Dialog error = new Dialog<>();
			error.setTitle("Erreur de connexion");
			error.setContentText("Une erreur s'est produite lors de la connexion :\n" + packet.getErrorMessage());
			ButtonType close = new ButtonType("Fermer", ButtonBar.ButtonData.CANCEL_CLOSE);
			error.getDialogPane().getButtonTypes().add(close);
			Platform.runLater(() -> {
				error.showAndWait();
				System.exit(0);
			});
		}
	}
}
