package net.zyuiop.loupgarou.client.net.handlers;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
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
			Label label = new Label(msg);
			label.setFont(Font.font(label.getFont().getFamily(), FontWeight.BOLD, label.getFont().getSize()));
			label.setTextFill(Color.web("#993928"));
			manager.getGameWindow().writeText(label);
			return;
		} else if (packet.getType() == MessageType.USER) {
			msg = "(" + packet.getSender() + ") " + packet.getMessage();
		} else {
			msg = "[Jeu] " + packet.getMessage();
			Label label = new Label(msg);
			label.setFont(Font.font(label.getFont().getFamily(), FontWeight.SEMI_BOLD, label.getFont().getSize()));
			label.setTextFill(Color.web("#63a353"));
			manager.getGameWindow().writeText(label);
			return;
		}
		manager.getGameWindow().writeText(msg + "\n");
	}
}
