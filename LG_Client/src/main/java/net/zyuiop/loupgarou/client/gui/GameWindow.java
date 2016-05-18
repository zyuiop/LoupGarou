package net.zyuiop.loupgarou.client.gui;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import net.zyuiop.loupgarou.client.net.NetworkManager;

/**
 * @author zyuiop
 */
public class GameWindow extends Stage {
	private final NetworkManager networkManager;
	private final TextArea       mainArea;
	private final VBox           votes;
	private final VBox			 gameData;

	public GameWindow(NetworkManager networkManager) {
		this.networkManager = networkManager;
		setTitle("Loup Garou - " + networkManager.getName() + " @ " + networkManager.getIp() + ":" + networkManager.getPort());
		setHeight(768);
		setWidth(1024);

		mainArea = new TextArea();
		mainArea.setEditable(false);
		mainArea.setPadding(Insets.EMPTY);
		mainArea.setFont(Font.font("Consolas"));

		votes = new VBox();
		gameData = new VBox();

		GridPane main = new GridPane();
		ColumnConstraints col1 = new ColumnConstraints();
		col1.setPercentWidth(20);
		ColumnConstraints col2 = new ColumnConstraints();
		col2.setPercentWidth(60);
		RowConstraints row = new RowConstraints();
		row.setPercentHeight(100);

		main.add(votes, 0, 0);
		main.add(mainArea, 1, 0);
		main.add(gameData, 2, 0);
		main.getColumnConstraints().addAll(col1, col2, col1);
		main.getRowConstraints().addAll(row);

		setScene(new Scene(main));
	}

	public void writeText(String text) {
		if (Platform.isFxApplicationThread()) {
			mainArea.appendText(text);
		} else {
			Platform.runLater(() -> writeText(text));
		}
	}
}
