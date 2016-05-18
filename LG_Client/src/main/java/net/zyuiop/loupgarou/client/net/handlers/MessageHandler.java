package net.zyuiop.loupgarou.client.net.handlers;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import net.zyuiop.loupgarou.client.net.PacketHandler;
import net.zyuiop.loupgarou.client.net.NetworkManager;
import net.zyuiop.loupgarou.protocol.network.MessageType;
import net.zyuiop.loupgarou.protocol.packets.clientbound.MessagePacket;

/**
 * @author zyuiop
 */
public class MessageHandler implements PacketHandler<MessagePacket> {
	private final NetworkManager manager;

	public MessageHandler(NetworkManager manager) {
		this.manager = manager;
	}

	@Override
	public void handle(MessagePacket packet) {
		if (packet.getType() == MessageType.ERROR) {
			Platform.runLater(() -> new Alert(Alert.AlertType.ERROR, packet.getMessage(), new ButtonType("Ok", ButtonBar.ButtonData.OK_DONE)).show());
			return;
		}

		String msg;
		if (packet.getType() == MessageType.SYSTEM) {
			msg = "[Système] " + packet.getMessage();
		} else if (packet.getType() == MessageType.USER) {
			msg = "(" + packet.getSender() + ") " + packet.getMessage();
		} else {
			msg = "[Jeu] " + packet.getMessage();
		}
		manager.getGameWindow().writeText(msg + "\n");
	}
}
