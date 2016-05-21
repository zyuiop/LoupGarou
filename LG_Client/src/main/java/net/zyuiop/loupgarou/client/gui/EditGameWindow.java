package net.zyuiop.loupgarou.client.gui;

import com.google.common.collect.Lists;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;
import javafx.util.Callback;
import net.zyuiop.loupgarou.protocol.data.Role;
import net.zyuiop.loupgarou.protocol.packets.serverbound.ChangeGameCompositionPacket;

import java.util.ArrayList;
import java.util.List;

/**
 * @author zyuiop
 */
public class EditGameWindow extends Stage {
	private static EditGameWindow instance;
	private final GameWindow parent;

	public EditGameWindow(GameWindow window) {
		this.parent = window;

		if (instance != null) {
			instance.close();
		}

		instance = this;

		setTitle("Éditer la partie");
		setHeight(400);

		setScene(new Scene(buildWindow()));
	}

	private Pane buildWindow() {
		TextField villagers = new TextField();
		villagers.setPromptText("Nombre de villageois");
		Integer i = parent.getRoleMap().get(Role.VILLAGER);
		villagers.setText((i == null ? 0 : i) + "");
		villagers.textProperty().addListener((observable, oldValue, newValue) -> {
			try {
				if (!newValue.matches("\\d*")) {
					villagers.setText(oldValue);
				} else if (Integer.parseInt(newValue) > 100) {
					villagers.setText(oldValue);
				}
			} catch (Exception ignored) {
			}
		});

		TextField maxWolves = new TextField();
		maxWolves.setPromptText("Nombre de loups");
		i = parent.getRoleMap().get(Role.WOLF);
		maxWolves.setText((i == null ? 0 : i) + "");
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

					if (parent.getRoleMap().containsKey(item) && parent.getRoleMap().get(item) > 0)
						updateSelected(true);
					else
						updateSelected(false);
				}
			}
		});

		List<Role> allowed = Lists.newArrayList(Role.values());
		allowed.remove(Role.VILLAGER);
		allowed.remove(Role.WOLF);
		roles.getItems().addAll(allowed);
		roles.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

		Button update = new Button("Modifier la partie");
		GridPane.setHgrow(update, Priority.ALWAYS);
		GridPane.setFillWidth(update, true);
		update.setMaxWidth(Double.MAX_VALUE);
		update.setOnMouseClicked(event -> {
			short maxVillagers = Short.parseShort(villagers.getText());
			int maxWolf = Integer.parseInt(maxWolves.getText());
			List<Role> selectedRoles = roles.getSelectionModel().getSelectedItems();

			int maxPl = maxVillagers + maxWolf + selectedRoles.size();
			if (selectedRoles.contains(Role.THIEF))
				maxPl -= 2;

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
			for (int t = 0; t < maxWolf; t++)
				r.add(Role.WOLF);

			parent.getNetworkManager().send(new ChangeGameCompositionPacket((short) maxPl, r.toArray(new Role[r.size()])));
			//} TODO : uncomment to enable input check
		});

		GridPane layout = new GridPane();
		layout.add(new Label("Modifier la partie"), 0, 0, 2, 1);
		layout.add(new Label("Villageois"), 0, 1);
		layout.add(villagers, 1, 1);
		layout.add(new Label("Loup-Garous"), 0, 2);
		layout.add(maxWolves, 1, 2);
		layout.add(new Label("Personnages"), 0, 3);
		layout.add(roles, 1, 3);
		layout.add(update, 0, 4, 2, 1);
		layout.setVgap(7);
		layout.setHgap(10);

		layout.setPadding(new Insets(10, 5, 10, 10));

		return layout;
	}

	public static void closeCurrent() {
		if (instance != null) {
			instance.close();
			instance = null;
		}
	}
}
