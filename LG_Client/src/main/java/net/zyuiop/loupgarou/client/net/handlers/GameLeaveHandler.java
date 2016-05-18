package net.zyuiop.loupgarou.client.net.handlers;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import net.zyuiop.loupgarou.client.net.PacketHandler;
import net.zyuiop.loupgarou.client.net.NetworkManager;
import net.zyuiop.loupgarou.protocol.packets.clientbound.GameLeavePacket;

/**
 * @author zyuiop
 */
public class GameLeaveHandler implements PacketHandler<GameLeavePacket> {
	private final NetworkManager manager;

	public GameLeaveHandler(NetworkManager manager) {
		this.manager = manager;
	}

	@Override
	public void handle(GameLeavePacket packet) {
		Platform.runLater(() -> {
			manager.leaveGame();
			new Alert(Alert.AlertType.INFORMATION, "Vous avez quitté la partie :\n" + packet.getReason(), new ButtonType("Ok", ButtonBar.ButtonData.OK_DONE)).show();
		});
	}
}
