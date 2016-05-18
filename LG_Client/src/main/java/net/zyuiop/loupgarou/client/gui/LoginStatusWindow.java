package net.zyuiop.loupgarou.client.gui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import net.zyuiop.loupgarou.client.LGClient;

/**
 * @author zyuiop
 */
public class LoginStatusWindow extends Stage {
	public LoginStatusWindow() {
		Label label = new Label("Connexion en cours...");
		label.setPadding(new Insets(2, 10, 2, 10));

		setScene(new Scene(label));
		setTitle("Connexion...");
	}
}
