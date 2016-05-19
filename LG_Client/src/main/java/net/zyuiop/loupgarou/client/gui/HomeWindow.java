package net.zyuiop.loupgarou.client.gui;

import com.google.common.collect.Lists;
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
	}

	private Pane constructLeftBox() {
		TextField name = new TextField();
		TextField maxPlayers = new TextField();
		maxPlayers.textProperty().addListener((observable, oldValue, newValue) -> {
			try {
				if (!newValue.matches("\\d*")) {
					maxPlayers.setText(oldValue);
				} else if (Integer.parseInt(newValue) > 255) {
					maxPlayers.setText(oldValue);
				}
			} catch (Exception e) {

			}
		});

		TextField maxWolves = new TextField();
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
			short maxPl = Short.parseShort(maxPlayers.getText());
			int maxWolf = Integer.parseInt(maxWolves.getText());
			List<Role> selectedRoles = roles.getSelectionModel().getSelectedItems();
			int roleSize = selectedRoles.size();
			if (selectedRoles.contains(Role.THIEF))
				roleSize -= 2;

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

				networkManager.send(new CreateGamePacket(nameVal, maxPl, r.toArray(new Role[r.size()])));
			//} TODO : uncomment to enable input check
		});

		GridPane layout = new GridPane();
		layout.add(new Label("Créer une nouvelle partie"), 0, 0, 2, 1);
		layout.add(new Label("Nom"), 0, 1);
		layout.add(name, 1, 1);
		layout.add(new Label("Joueurs"), 0, 2);
		layout.add(maxPlayers, 1, 2);
		layout.add(new Label("Loups"), 0, 3);
		layout.add(maxWolves, 1, 3);
		layout.add(new Label("Personnages"), 0, 4);
		layout.add(roles, 1, 4);
		layout.add(create, 0, 5, 2, 1);
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
				networkManager.send(new JoinGamePacket(selected.getId()));
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
		gameView.refresh();
	}
}
