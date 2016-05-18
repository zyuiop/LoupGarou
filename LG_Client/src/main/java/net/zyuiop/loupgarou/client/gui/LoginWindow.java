package net.zyuiop.loupgarou.client.gui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import net.zyuiop.loupgarou.client.LGClient;
import net.zyuiop.loupgarou.protocol.packets.serverbound.SendMessagePacket;

/**
 * @author zyuiop
 */
public class LoginWindow extends Stage {
	public LoginWindow(LGClient client) {
		TextField ip = new TextField("127.0.0.1:2325");
		TextField name = new TextField("zyuiop");

		Button button = new Button("Connexion");
		button.setOnMouseClicked(event -> {
			close();
			client.connect(ip.getText(), name.getText());
		});

		name.setOnKeyReleased(event -> {
			if (event.getCode() == KeyCode.ENTER) {
				close();
				client.connect(ip.getText(), name.getText());
			}
		});

		ip.setOnKeyReleased(event -> {
			if (event.getCode() == KeyCode.ENTER) {
				close();
				client.connect(ip.getText(), name.getText());
			}
		});

		GridPane pane = new GridPane();
		pane.setHgap(10);
		pane.setVgap(5);
		pane.setPadding(new Insets(2, 10, 2, 10));

		pane.add(new Label("IP"), 0, 0);
		pane.add(ip, 1, 0);
		pane.add(new Label("Pseudo"), 0, 1);
		pane.add(name, 1, 1);
		HBox buttonBox = new HBox(button);
		buttonBox.setAlignment(Pos.CENTER);
		pane.add(buttonBox, 0, 2, 2, 1);

		setScene(new Scene(pane));
		setTitle("Connexion");
	}
}
