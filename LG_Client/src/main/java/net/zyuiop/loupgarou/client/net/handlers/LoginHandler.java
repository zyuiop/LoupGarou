package net.zyuiop.loupgarou.client.net.handlers;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import net.zyuiop.loupgarou.client.LGClient;
import net.zyuiop.loupgarou.client.gui.LoginWindow;
import net.zyuiop.loupgarou.client.net.PacketHandler;
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
			Platform.runLater(() -> {
				LGClient.logger.info("Error : " + packet.getErrorMessage());
				Alert error = new Alert(Alert.AlertType.ERROR, "Une erreur s'est produite lors de la connexion :\n" + packet.getErrorMessage(), new ButtonType("Fermer", ButtonBar.ButtonData.CANCEL_CLOSE));
				error.show();
				LoginWindow.getInstance().setConnecting(false);
			});
		}
	}
}
