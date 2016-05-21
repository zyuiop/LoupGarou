package net.zyuiop.loupgarou.client.gui;

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import net.zyuiop.loupgarou.client.LGClient;
import net.zyuiop.loupgarou.client.data.SavedServer;
import net.zyuiop.loupgarou.client.data.SavedServers;

/**
 * @author zyuiop
 */
public class LoginWindow extends Stage {
	private static LoginWindow instance;
	private final TableView<SavedServer> serversView;
	private final LGClient               client;
	private final SavedServers           manager;
	private       Button                 connect;
	private       Button                 directConnect;
	private       Button                 deleteButton;
	private Label status;
	private       boolean                isConnecting;

	public LoginWindow(LGClient client, SavedServers manager) {
		this.client = client;
		this.manager = manager;

		instance = this;

		setTitle("Loup Garou - Connexion");

		serversView = new TableView<>();
		serversView.setPrefHeight(150);

		status = new Label("Prêt.");

		VBox statusBar = new VBox(new Separator(Orientation.HORIZONTAL), status);
		statusBar.setSpacing(5);
		statusBar.setPadding(new Insets(5, 5, 5, 5));

		BorderPane main = new BorderPane();
		main.setRight(constructRightBox());
		main.setLeft(constructLeftBox());
		main.setBottom(statusBar);

		serversView.getItems().addAll(manager.getServers());

		setScene(new Scene(main));
	}

	private Pane constructLeftBox() {
		TextField ip = new TextField();
		ip.setPromptText("IP");

		TextField port = new TextField();
		port.setText("2325");
		port.textProperty().addListener((observable, oldValue, newValue) -> {
			try {
				if (!newValue.matches("\\d*")) {
					port.setText(oldValue);
				} else if (Integer.parseInt(newValue) > (Short.MAX_VALUE * 2) + 1) {
					port.setText(oldValue);
				}
			} catch (Exception e) {
				port.setText(oldValue);
			}
		});

		TextField name = new TextField();
		name.setPromptText("Pseudo");

		Button create = new Button("Ajouter le serveur");
		connect = new Button("Connexion directe");

		GridPane.setHgrow(create, Priority.ALWAYS);
		GridPane.setFillWidth(create, true);
		GridPane.setHgrow(connect, Priority.ALWAYS);
		GridPane.setFillWidth(connect, true);
		create.setMaxWidth(Double.MAX_VALUE);
		connect.setMaxWidth(Double.MAX_VALUE);

		create.setOnMouseClicked(event -> {
			SavedServer server = getServer(name, ip, port);
			manager.addServer(server);
			serversView.getItems().add(server);
		});

		connect.setOnMouseClicked(event -> {
			setConnecting(true);
			getServer(name, ip, port).connect(client);
		});

		GridPane layout = new GridPane();
		layout.add(new Label("Nouveau serveur"), 0, 0, 4, 1);
		layout.add(new Label("IP / Port"), 0, 1);
		layout.add(ip, 1, 1, 2, 1);
		layout.add(port, 3, 1);
		layout.add(new Label("Pseudo"), 0, 2);
		layout.add(name, 1, 2, 3, 1);
		layout.add(create, 0, 3, 2, 1);
		layout.add(connect, 2, 3, 2, 1);
		layout.setVgap(7);
		layout.setHgap(10);

		ColumnConstraints row4 = new ColumnConstraints();
		row4.setPercentWidth(20);
		ColumnConstraints row3 = new ColumnConstraints();
		row3.setPercentWidth(50);
		ColumnConstraints row2 = new ColumnConstraints();
		row2.setPercentWidth(30);
		ColumnConstraints row1 = new ColumnConstraints();
		row1.setPercentWidth(20);

		layout.getColumnConstraints().addAll(row1, row2, row3, row4);

		layout.setPadding(new Insets(10, 5, 10, 10));

		layout.setMaxWidth(350);

		return layout;
	}

	private static SavedServer getServer(TextField nameField, TextField ipField, TextField portField) {
		String name = nameField.getText();
		int port = getPort(portField);
		String ip = ipField.getText();

		return new SavedServer(ip, port, name);
	}

	private static int getPort(TextField field) {
		try {
			int val = Integer.parseInt(field.getText());
			return val > (Short.MAX_VALUE * 2) + 1 ? 2325 : val;
		} catch (NumberFormatException e) {
			return 2325;
		}
	}

	private Pane constructRightBox() {
		TableColumn<SavedServer, String> ipCol = new TableColumn<>("IP");
		ipCol.setPrefWidth(100);
		ipCol.setCellValueFactory((TableColumn.CellDataFeatures<SavedServer, String> param) ->
				new ReadOnlyStringWrapper(param.getValue().getIp())
		);

		TableColumn<SavedServer, String> portCol = new TableColumn<>("Port");
		portCol.setPrefWidth(70);
		portCol.setCellValueFactory((TableColumn.CellDataFeatures<SavedServer, String> param) ->
				new ReadOnlyStringWrapper(param.getValue().getPort() + "")
		);

		TableColumn<SavedServer, String> nameCol = new TableColumn<>("Pseudo");
		nameCol.setPrefWidth(100);
		nameCol.setCellValueFactory((TableColumn.CellDataFeatures<SavedServer, String> param) ->
				new ReadOnlyStringWrapper(param.getValue().getUsername())
		);

		deleteButton = new Button("Supprimer");
		deleteButton.setDisable(true);

		directConnect = new Button("Rejoindre");
		directConnect.setDisable(true);

		serversView.getColumns().addAll(ipCol, portCol, nameCol);
		serversView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
		serversView.setOnMouseClicked(event -> {
			checkButtonEnable();
		});

		deleteButton.setOnMouseClicked(event -> {
			SavedServer selected = serversView.getSelectionModel().getSelectedItem();
			if (selected != null) {
				manager.removeServer(selected);
				serversView.getItems().remove(selected);
				serversView.getOnMouseClicked().handle(null);
			}
		});

		directConnect.setOnMouseClicked(event -> {
			SavedServer selected = serversView.getSelectionModel().getSelectedItem();
			if (selected != null) {
				setConnecting(true);
				selected.connect(client);
			}
		});

		deleteButton.setMaxWidth(Double.MAX_VALUE);
		directConnect.setMaxWidth(Double.MAX_VALUE);
		HBox.setHgrow(deleteButton, Priority.ALWAYS);
		HBox.setHgrow(directConnect, Priority.ALWAYS);

		HBox downBox = new HBox(10, deleteButton, directConnect);
		VBox vBox = new VBox(10, serversView, downBox);
		vBox.setPadding(new Insets(10, 10, 10, 5));
		return vBox;
	}

	private void checkButtonEnable() {
		if (serversView.getSelectionModel().getSelectedItem() == null || isConnecting) {
			deleteButton.setDisable(true);
			directConnect.setDisable(true);
		} else {
			deleteButton.setDisable(false);
			directConnect.setDisable(false);
		}
	}

	public void setConnecting(boolean connecting) {
		isConnecting = connecting;
		if (connecting) {
			directConnect.setDisable(true);
			connect.setDisable(true);
		} else {
			setStatus("Prêt.");
			checkButtonEnable();
			connect.setDisable(false);
		}
	}

	@Override
	public void close() {
		super.close();
		setConnecting(false);
	}

	public static LoginWindow getInstance() {
		return instance;
	}

	public void setStatus(String text) {
		if (!Platform.isFxApplicationThread())
			Platform.runLater(() -> setStatus(text));
		else
			status.setText(text);
	}
}
