package net.zyuiop.loupgarou.client.gui;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.google.common.hash.Hashing;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.Callback;
import net.zyuiop.loupgarou.client.LGClient;
import net.zyuiop.loupgarou.client.net.NetworkManager;
import net.zyuiop.loupgarou.game.Role;
import net.zyuiop.loupgarou.game.tasks.RepeatableTask;
import net.zyuiop.loupgarou.game.tasks.TaskManager;
import net.zyuiop.loupgarou.protocol.network.GameInfo;
import net.zyuiop.loupgarou.protocol.packets.serverbound.CreateGamePacket;
import net.zyuiop.loupgarou.protocol.packets.serverbound.JoinGamePacket;
import net.zyuiop.loupgarou.protocol.packets.serverbound.RefreshGameListPacket;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @author zyuiop
 */
public class HomeWindow extends Stage {
	private final NetworkManager      networkManager;
	private final TableView<GameInfo> gameView;

	public HomeWindow(NetworkManager networkManager) {
		this.networkManager = networkManager;

		setTitle("Loup Garou - " + networkManager.getName() + " @ " + networkManager.getIp() + ":" + networkManager.getPort());
		setHeight(350);

		TaskManager.submit(new RepeatableTask(15, 15) {
			@Override
			public void run() {
				networkManager.send(new RefreshGameListPacket());
			}
		});

		gameView = new TableView<>();

		BorderPane main = new BorderPane();
		main.setRight(constructRightBox());
		main.setLeft(constructLeftBox());
		setScene(new Scene(main));

		setOnCloseRequest(event -> {
			Platform.exit();
		});
	}

	private Pane constructLeftBox() {
		TextField name = new TextField();
		name.setPromptText("Nom de partie");

		PasswordField passwordField = new PasswordField();
		passwordField.setPromptText("Mot de passe de la salle (facultatif)");

		TextField villagers = new TextField();
		villagers.setPromptText("Nombre de villageois");
		villagers.textProperty().addListener((observable, oldValue, newValue) -> {
			try {
				if (!newValue.matches("\\d*")) {
					villagers.setText(oldValue);
				} else if (Integer.parseInt(newValue) > 100) {
					villagers.setText(oldValue);
				}
			} catch (Exception e) {

			}
		});

		TextField maxWolves = new TextField();
		maxWolves.setPromptText("Nombre de loups");
		maxWolves.textProperty().addListener((observable, oldValue, newValue) -> {
			if (!newValue.matches("\\d*")) {
				maxWolves.setText(oldValue);
			}
		});

		ListView<Role> roles = new ListView<>();
		roles.setCellFactory(new Callback<ListView<Role>, ListCell<Role>>() {
			@Override
			public ListCell<Role> call(ListView<Role> param) {
				return new RoleCell();
			}

			class RoleCell extends ListCell<Role> {
				@Override
				protected void updateItem(Role item, boolean empty) {
					super.updateItem(item, empty);
					setText(item == null ? "" : item.getName());
				}
			}
		});
		List<Role> allowed = Lists.newArrayList(Role.values());
		allowed.remove(Role.VILLAGER);
		allowed.remove(Role.WOLF);
		roles.getItems().addAll(allowed);
		roles.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

		Button create = new Button("Créer la partie");
		GridPane.setHgrow(create, Priority.ALWAYS);
		GridPane.setFillWidth(create, true);
		create.setMaxWidth(Double.MAX_VALUE);
		create.setOnMouseClicked(event -> {
			String nameVal = name.getText();
			short maxVillagers = Short.parseShort(villagers.getText());
			int maxWolf = Integer.parseInt(maxWolves.getText());
			List<Role> selectedRoles = roles.getSelectionModel().getSelectedItems();

			int maxPl = maxVillagers + maxWolf + selectedRoles.size();
			if (selectedRoles.contains(Role.THIEF))
				maxPl -= 2;

			String password = passwordField.getText();
			if (password.length() == 0)
				password = null;
			else
				password = Hashing.sha256().hashString(password, Charsets.UTF_8).toString();

			LGClient.logger.info("Password : " + password);

			if (maxPl > 70) {
				new Alert(Alert.AlertType.WARNING, "Il est impossible d'accepter plus de 70 joueurs dans une même partie.", new ButtonType("Ok", ButtonBar.ButtonData.OK_DONE)).show();
			}
			/*else if (maxPl < 9) {
				new Alert(Alert.AlertType.WARNING, "Il faut au moins 9 joueurs !", new ButtonType("Ok", ButtonBar.ButtonData.OK_DONE)).show();
			} else if (maxWolf * 3 > maxPl) {
				new Alert(Alert.AlertType.WARNING, "Il y a trop de loups !", new ButtonType("Ok", ButtonBar.ButtonData.OK_DONE)).show();
			} else if (maxWolf < 1) {
				new Alert(Alert.AlertType.WARNING, "Il faut au moins un loup !", new ButtonType("Ok", ButtonBar.ButtonData.OK_DONE)).show();
			} else if (roleSize + maxWolf > maxPl) {
				new Alert(Alert.AlertType.WARNING, "Il y a plus de personnages et\nde loups que de joueurs !", new ButtonType("Ok", ButtonBar.ButtonData.OK_DONE)).show();
			} else {*/
			List<Role> r = new ArrayList<>();
			r.addAll(selectedRoles);
			for (int i = 0; i < maxWolf; i++)
				r.add(Role.WOLF);

			networkManager.send(new CreateGamePacket(nameVal, (short) maxPl, r.toArray(new Role[r.size()]), password));
			//} TODO : uncomment to enable input check
		});

		GridPane layout = new GridPane();
		layout.add(new Label("Créer une nouvelle partie"), 0, 0, 2, 1);
		layout.add(new Label("Nom"), 0, 1);
		layout.add(name, 1, 1);
		layout.add(new Label("Mot de passe"), 0, 2);
		layout.add(passwordField, 1, 2);
		layout.add(new Label("Villageois"), 0, 3);
		layout.add(villagers, 1, 3);
		layout.add(new Label("Loup-Garous"), 0, 4);
		layout.add(maxWolves, 1, 4);
		layout.add(new Label("Personnages"), 0, 5);
		layout.add(roles, 1, 5);
		layout.add(create, 0, 6, 2, 1);
		layout.setVgap(7);
		layout.setHgap(10);

		layout.setPadding(new Insets(10, 5, 10, 10));

		return layout;
	}

	private Pane constructRightBox() {
		TableColumn<GameInfo, String> nameCol = new TableColumn<>("Nom");
		nameCol.setPrefWidth(100);
		nameCol.setCellValueFactory((TableColumn.CellDataFeatures<GameInfo, String> param) ->
				new ReadOnlyStringWrapper(param.getValue().getGameName())
		);

		TableColumn<GameInfo, String> hostCol = new TableColumn<>("Host");
		hostCol.setPrefWidth(70);
		hostCol.setCellValueFactory((TableColumn.CellDataFeatures<GameInfo, String> param) ->
				new ReadOnlyStringWrapper(param.getValue().getHoster())
		);

		TableColumn<GameInfo, String> playersCol = new TableColumn<>("Joueurs");
		playersCol.setCellValueFactory((TableColumn.CellDataFeatures<GameInfo, String> param) ->
				new ReadOnlyStringWrapper(param.getValue().getCurrentPlayers() + " / " + param.getValue().getMaxPlayers())
		);

		TableColumn<GameInfo, String> stateCol = new TableColumn<>("État");
		stateCol.setCellValueFactory((TableColumn.CellDataFeatures<GameInfo, String> param) ->
				new ReadOnlyStringWrapper(param.getValue().getState().getHumanState())
		);

		gameView.getColumns().addAll(nameCol, hostCol, playersCol, stateCol);
		gameView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

		Button refreshButton = new Button("Rafraichir");
		refreshButton.setOnMouseClicked(event -> networkManager.send(new RefreshGameListPacket()));

		Button connectButton = new Button("Rejoindre");
		connectButton.setOnMouseClicked(event -> {
			GameInfo selected = gameView.getSelectionModel().getSelectedItem();
			if (selected == null) {
				Alert alert = new Alert(Alert.AlertType.WARNING, "Merci de sélectionner une partie pour pouvoir\nvous connecter dessus !", new ButtonType("Ok", ButtonBar.ButtonData.OK_DONE));
				alert.show();
			} else {
				String password = null;
				if (selected.isHasPassword()) {
					TextInputDialog dialog = new TextInputDialog();
					dialog.getEditor().getStyleClass().add("password-field");
					dialog.setHeaderText("Mot de passe requis");
					dialog.setContentText("Saisissez le mot de passe de la partie :");
					dialog.setTitle("Mot de passe requis");

					Optional<String> optional = dialog.showAndWait();
					if (!optional.isPresent())
						return;

					password = Hashing.sha256().hashString(optional.get(), Charsets.UTF_8).toString();
				}

				networkManager.send(new JoinGamePacket(selected.getId(), password));
			}
		});

		refreshButton.setMaxWidth(Double.MAX_VALUE);
		connectButton.setMaxWidth(Double.MAX_VALUE);
		HBox.setHgrow(refreshButton, Priority.ALWAYS);
		HBox.setHgrow(connectButton, Priority.ALWAYS);

		HBox downBox = new HBox(10, refreshButton, connectButton);
		VBox vBox = new VBox(10, gameView, downBox);
		vBox.setPadding(new Insets(10, 10, 10, 5));
		return vBox;
	}

	public void setContent(List<GameInfo> infoList) {
		if (!Platform.isFxApplicationThread()) {
			Platform.runLater(() -> setContent(infoList));
			return;
		}

		LGClient.logger.info("Got server list : " + infoList.toString());
		gameView.getItems().clear();
		gameView.getItems().addAll(infoList);
	}
}
