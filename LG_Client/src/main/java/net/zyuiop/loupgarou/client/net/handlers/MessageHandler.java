package net.zyuiop.loupgarou.client.net.handlers;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
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
		Label label = null;
		if (packet.getType() == MessageType.SYSTEM) {
			msg = "[Système] " + packet.getMessage();
			label = new Label(msg);
			label.setFont(Font.font(label.getFont().getFamily(), FontWeight.BOLD, label.getFont().getSize()));
			label.setTextFill(Color.web("#993928"));
			manager.getGameWindow().writeText(label);
		} else if (packet.getType() == MessageType.USER) {
			msg = "(" + packet.getSender() + ") " + packet.getMessage();
		} else {
			msg = "[Jeu] " + packet.getMessage();
			label = new Label(msg);
			label.setFont(Font.font(label.getFont().getFamily(), FontWeight.SEMI_BOLD, label.getFont().getSize()));
			label.setTextFill(Color.web("#63a353"));
		}

		if (label != null) {
			if (packet.isCustomStyle()) {
				switch (packet.getModifier()) {
					case BOLD:
						label.setFont(Font.font(label.getFont().getFamily(), FontWeight.SEMI_BOLD, label.getFont().getSize()));
						break;
					case ITALIC:
						label.setFont(Font.font(label.getFont().getFamily(), FontPosture.ITALIC, label.getFont().getSize()));
						break;
					case BOLD_AND_ITALIC:
						label.setFont(Font.font(label.getFont().getFamily(), FontWeight.SEMI_BOLD, FontPosture.ITALIC, label.getFont().getSize()));
						break;
				}

				label.setTextFill(Color.rgb(packet.getRed(), packet.getGreen(), packet.getBlue()));
			}

			manager.getGameWindow().writeText(label);
			return;
		}
		manager.getGameWindow().writeText(msg + "\n");
	}
}
