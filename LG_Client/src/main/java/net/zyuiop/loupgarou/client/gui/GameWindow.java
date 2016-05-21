package net.zyuiop.loupgarou.client.gui;

import com.google.common.collect.Lists;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import net.zyuiop.loupgarou.client.LGClient;
import net.zyuiop.loupgarou.client.net.NetworkManager;
import net.zyuiop.loupgarou.game.GamePhase;
import net.zyuiop.loupgarou.game.GameState;
import net.zyuiop.loupgarou.game.Role;
import net.zyuiop.loupgarou.game.tasks.RepeatableTask;
import net.zyuiop.loupgarou.game.tasks.TaskManager;
import net.zyuiop.loupgarou.protocol.packets.clientbound.*;
import net.zyuiop.loupgarou.protocol.packets.serverbound.JoinGamePacket;
import net.zyuiop.loupgarou.protocol.packets.serverbound.SendMessagePacket;
import net.zyuiop.loupgarou.protocol.packets.serverbound.VotePacket;

import javax.sound.sampled.AudioSystem;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author zyuiop
 */
public class GameWindow extends Stage {
	private final NetworkManager networkManager;
	private final VBox           mainArea;
	private final VBox           players;
	private final VBox           votes;
	private final Accordion      voteValues;
	private final Map<Integer, VoteHolder> voteMap = new HashMap<>();
	private final Label     roomNameLabel;
	private final Label     hostLabel;
	private final Label     stateLabel;
	private final Label     phaseLabel;
	private final Label     roleLabel;
	private final ImageView roleCard;
	private final VBox      gameState;
	private final VBox      presentRoles;
	private final TextField message;
	private final Button    sendMessage;
	private Role               role    = null;
	private Map<Role, Integer> roleMap = new HashMap<>();
	private MediaPlayer player = null;

	private GameJoinConfirmPacket joinData;

	private Pane setupMainArea() {
		mainArea.setPadding(Insets.EMPTY);
		mainArea.setBackground(new Background(new BackgroundFill(Color.WHITE, CornerRadii.EMPTY, null)));
		mainArea.getChildren().clear();

		ScrollPane pane = new ScrollPane(mainArea);
		pane.setPadding(Insets.EMPTY);
		pane.setMinHeight(600);
		pane.setMinViewportHeight(600);
		pane.setMaxHeight(Double.MAX_VALUE);
		pane.setStyle("-fx-background: white;");
		VBox.setVgrow(pane, Priority.ALWAYS);
		pane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

		DoubleProperty hProperty = new SimpleDoubleProperty();
		hProperty.bind(mainArea.heightProperty());
		hProperty.addListener((observable, oldValue, newValue) -> {
			pane.setVvalue(1D);
		});

		mainArea.setFillWidth(true);
		pane.setFitToWidth(true);

		message.clear();
		message.setDisable(false);
		message.setMaxWidth(Double.MAX_VALUE);
		HBox.setHgrow(message, Priority.ALWAYS);

		sendMessage.setDisable(false);

		HBox hBox = new HBox(message, sendMessage);
		hBox.setSpacing(5);

		VBox center = new VBox(pane, hBox);
		center.setSpacing(5);
		center.setPadding(new Insets(10, 5, 10, 5));
		center.setMaxHeight(Double.MAX_VALUE);
		center.setMinWidth(500);

		setOnCloseRequest(event -> {
			Platform.exit();
		});

		return center;
	}

	private Pane setupLeftSide(boolean host) {
		Label firstLabel = new Label("Êtat du jeu");
		firstLabel.setFont(Font.font(firstLabel.getFont().getFamily(), FontWeight.BOLD, 15));

		gameState.setSpacing(3);
		gameState.getChildren().setAll(firstLabel, roomNameLabel, hostLabel);

		Button disconnect = new Button("Quitter la partie");
		HBox.setHgrow(disconnect, Priority.ALWAYS);
		disconnect.setMaxWidth(Double.MAX_VALUE);

		disconnect.setOnMouseClicked(event -> {
			networkManager.send(new JoinGamePacket(-1));
			disconnect.setDisable(true);
		});

		Button edit = null;
		if (host) {
			edit = new Button("Modifier la composition");
			HBox.setHgrow(edit, Priority.ALWAYS);
			edit.setMaxWidth(Double.MAX_VALUE);
			edit.setOnMouseClicked(event -> new EditGameWindow(GameWindow.this).show());
		}

		TitledPane pane = new TitledPane("Composition", presentRoles);
		pane.setExpanded(false);
		presentRoles.setSpacing(3);

		VBox left = new VBox(gameState, pane, new Separator(Orientation.HORIZONTAL), disconnect);
		if (edit != null)
			left.getChildren().add(edit);
		left.getChildren().addAll(new Separator(Orientation.HORIZONTAL), votes);

		left.setPadding(new Insets(10, 5, 10, 10));
		left.setSpacing(7);
		left.setMaxHeight(Double.MAX_VALUE);
		VBox.setVgrow(left, Priority.ALWAYS);

		return left;
	}

	private Pane setupRightSide() {
		roleCard.setImage(null);
		players.getChildren().clear();
		voteValues.getPanes().clear();

		Label firstLabel = new Label("Votes des joueurs");
		firstLabel.setFont(Font.font(firstLabel.getFont().getFamily(), FontWeight.BOLD, 15));

		VBox right = new VBox(roleCard, players, new Separator(Orientation.HORIZONTAL), firstLabel, voteValues);
		right.setPadding(new Insets(10, 10, 10, 5));
		right.setSpacing(7);
		right.setMaxHeight(Double.MAX_VALUE);

		return right;
	}

	private void setupWindow(boolean host) {
		BorderPane main = new BorderPane();
		main.setLeft(setupLeftSide(host));
		main.setCenter(setupMainArea());
		main.setRight(setupRightSide());

		setMinWidth(900);
		setMinHeight(680);
		setMaximized(true);
		setScene(new Scene(main));
	}

	public GameWindow(NetworkManager networkManager) {
		this.networkManager = networkManager;

		setTitle("Not ready");
		//setResizable(false);

		mainArea = new VBox();
		message = new TextField();
		sendMessage = new Button("Envoyer");
		sendMessage.setOnMouseClicked(event -> {
			if (message.getText().length() > 1) {
				networkManager.send(new SendMessagePacket(message.getText()));
				message.clear();
			}
		});

		message.setOnKeyReleased(event -> {
			if (event.getCode() == KeyCode.ENTER) {
				if (message.getText().length() > 1) {
					networkManager.send(new SendMessagePacket(message.getText()));
					message.clear();
				}
			}
		});

		roomNameLabel = new Label();
		hostLabel = new Label();
		stateLabel = new Label();
		phaseLabel = new Label();
		roleLabel = new Label();
		roleCard = new ImageView();
		roleCard.maxWidth(300);
		roleCard.maxHeight(300);
		roleCard.setPreserveRatio(true);
		VBox.setVgrow(roleCard, Priority.ALWAYS);

		gameState = new VBox();
		presentRoles = new VBox();
		votes = new VBox();
		voteValues = new Accordion();

		players = new VBox();
		players.setSpacing(3);
		players.setPadding(new Insets(10, 10, 10, 5));
		setupRightSide();

		ColumnConstraints col1 = new ColumnConstraints();
		col1.setPercentWidth(20);
		ColumnConstraints col2 = new ColumnConstraints();
		col2.setPercentWidth(60);
		RowConstraints row = new RowConstraints();
		row.setPercentHeight(100);

		setupWindow(false);
	}

	public void writeText(String text) {
		writeText(text, null);
	}

	public void writeText(Label label) {
		if (Platform.isFxApplicationThread()) {
			label.setWrapText(true);
			if (mainArea.getChildren().size() > 100)
				mainArea.getChildren().remove(0);
			mainArea.getChildren().add(label);
		} else {
			Platform.runLater(() -> writeText(label));
		}
	}

	public void writeText(String text, String style) {
		if (Platform.isFxApplicationThread()) {
			Label label = new Label(text);
			label.setWrapText(true);
			if (style != null)
				label.setStyle(style);
			if (mainArea.getChildren().size() > 100)
				mainArea.getChildren().remove(0);
			mainArea.getChildren().add(label);
		} else {
			Platform.runLater(() -> writeText(text));
		}
	}

	public String getGameName() {
		return joinData.getName();
	}

	public int getGameId() {
		return joinData.getId();
	}

	public void acceptGame(GameJoinConfirmPacket data) {
		// Reset window
		setupWindow(data.getHoster().equals(networkManager.getName()));

		// Load
		this.joinData = data;
		setTitle("Loup Garou - " + networkManager.getName() + " @ " + networkManager.getIp() + ":" + networkManager.getPort() + " / " + joinData.getName());
		roomNameLabel.setText("Nom de la partie : " + data.getName());
		hostLabel.setText("Host : " + data.getHoster());
	}

	public void setState(GameState state) {
		stateLabel.setText("Êtat : " + state.getHumanState());
		if (!gameState.getChildren().contains(stateLabel))
			gameState.getChildren().add(stateLabel);

		if (state != GameState.STARTED) {
			message.setDisable(false);
			sendMessage.setDisable(false);
		}
	}

	public void setPhase(GamePhase phase) {
		phaseLabel.setText("Tour : " + phase.getHumanPhase());
		if (!gameState.getChildren().contains(phaseLabel))
			gameState.getChildren().add(phaseLabel);

		if (player != null) {
			player.stop();
			player = null;
		}

		URL url = this.getClass().getClassLoader().getResource("phase-" + phase.name() + ".mp3");
		if (url != null) {
			Media media = new Media(url.toExternalForm());
			player = new MediaPlayer(media);
			player.play();
		}

		switch (phase) {
			case PRE_NIGHT:
			case END_NIGHT:
				message.setDisable(true);
				sendMessage.setDisable(true);
				break;
			case NIGHT:
				if (role == Role.WOLF || role == null) {
					message.setDisable(false);
					sendMessage.setDisable(false);
				} else {
					message.setDisable(true);
					sendMessage.setDisable(true);
				}
				break;
			case PREPARATION:
			case DAY:
				message.setDisable(false);
				sendMessage.setDisable(false);
				break;
		}
	}

	public void setRole(Role role) {
		roleLabel.setText("Personnage : " + role.getName() + "\nBut : " + role.getObjective() + "\nPouvoirs : " + role.getPower());
		if (!gameState.getChildren().contains(roleLabel))
			gameState.getChildren().add(roleLabel);
		this.role = role;

		try {
			InputStream stream = getClass().getClassLoader().getResourceAsStream("card-" + role.name() + ".jpg");
			if (stream != null) {
				Image image = new Image(stream);
				roleCard.setImage(image);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void setPlayers(SetPlayersPacket packet) {
		if (!Platform.isFxApplicationThread()) {
			Platform.runLater(() -> setPlayers(packet));
			return;
		}

		players.getChildren().clear();
		List<Label> labelList = Lists.newArrayList(packet.getPlayers()).stream().map(Label::new).collect(Collectors.toList());
		Label firstLabel = new Label("Joueurs (" + labelList.size() + " / " + packet.getMaxPlayers() + ")");
		firstLabel.setFont(Font.font(firstLabel.getFont().getFamily(), FontWeight.BOLD, 15));

		players.getChildren().add(firstLabel);
		players.getChildren().addAll(labelList);
	}

	public void startVote(VoteRequestPacket packet) {
		finishVote(packet.getVoteId());

		VoteHolder pane = new VoteHolder(packet);
		voteMap.put(packet.getVoteId(), pane);
		votes.getChildren().add(pane.getVotePane());
		if (packet.getVoters().length > 1) {
			voteValues.getPanes().add(pane.getValuePane());
			if (voteValues.getExpandedPane() == null)
				voteValues.setExpandedPane(pane.getValuePane());
		}
	}

	public void finishVote(VoteEndPacket packet) {
		finishVote(packet.getVoteId());
	}

	public void finishVote(int id) {
		if (voteMap.containsKey(id)) {
			VoteHolder holder = voteMap.remove(id);
			votes.getChildren().remove(holder.getVotePane());
			voteValues.getPanes().remove(holder.getValuePane());
			holder.remove();
		}
	}

	public void voteValue(VoteValuePacket packet) {
		if (!Platform.isFxApplicationThread()) {
			Platform.runLater(() -> voteValue(packet));
			return;
		}

		if (voteMap.containsKey(packet.getId())) {
			VoteHolder holder = voteMap.get(packet.getId());
			holder.getValuePane().setVote(packet.getVotingPlayer(), packet.getVote());
		}
	}

	public void changeComposition(Role[] roles) {
		if (!Platform.isFxApplicationThread()) {
			Platform.runLater(() -> changeComposition(roles));
			return;
		}

		// On considère la modif comme validée, s'il y en avait une en cours
		EditGameWindow.closeCurrent();

		Map<Role, Integer> amt = new HashMap<>();
		for (Role role : roles) {
			if (!amt.containsKey(role))
				amt.put(role, 0);
			amt.put(role, amt.get(role) + 1);
		}

		presentRoles.getChildren().clear();
		Label first = new Label("Personnages de la partie :");
		presentRoles.getChildren().add(first);
		presentRoles.getChildren().addAll(
				amt.entrySet().stream()
						.map(entry -> new Label("- " + entry.getKey().getName() + ((entry.getValue() > 1) ? " (" + entry.getValue() + ")" : "")))
						.collect(Collectors.toList()));

		LGClient.logger.info("Received info " + amt.toString());

		this.roleMap.clear();
		this.roleMap.putAll(amt);
	}

	public NetworkManager getNetworkManager() {
		return networkManager;
	}

	private class VoteHolder {
		private final VotePane       pane;
		private final VoteValuePane  value;
		private final RepeatableTask task;

		private VoteHolder(VoteRequestPacket packet) {
			this.pane = new VotePane(packet);
			this.value = new VoteValuePane(packet);

			task = new RepeatableTask(1, 1) {
				private int time = packet.getChooseTime();

				@Override
				public void run() {
					time--;
					if (time == 0)
						cancel();
					Platform.runLater(() -> pane.voteLabel.setText("Temps restant pour voter : " + time));
				}
			};
			TaskManager.submit(task);
		}

		public VoteValuePane getValuePane() {
			return value;
		}

		public VotePane getVotePane() {
			return pane;
		}

		public void remove() {
			task.cancel();
		}
	}

	private class VotePane extends TitledPane {
		private final Label voteLabel;

		public VotePane(VoteRequestPacket packet) {
			voteLabel = new Label("Temps restant pour voter : " + packet.getChooseTime());
			ChoiceBox<String> choiceBox = new ChoiceBox<>(FXCollections.observableArrayList(packet.getAvailableChoices()));
			choiceBox.setValue(packet.getAvailableChoices()[0]);
			setText(packet.getChooseReason());
			HBox choiceline = new HBox(new Label("Vote"), choiceBox);
			choiceline.setSpacing(5);

			Button button = new Button("Envoyer le vote");
			button.setMaxWidth(Double.MAX_VALUE);
			HBox.setHgrow(button, Priority.ALWAYS);

			button.setOnMouseClicked(event -> {
				if (choiceBox.getValue() == null)
					return;

				networkManager.send(new VotePacket(packet.getVoteId(), choiceBox.getValue()));
			});

			VBox vBox = new VBox(voteLabel, choiceline, button);
			vBox.setSpacing(5);
			setContent(vBox);
		}
	}

	private class VoteValuePane extends TitledPane {
		private Map<String, Label> playersLabels = new HashMap<>();

		public VoteValuePane(VoteRequestPacket packet) {
			setText(packet.getChooseReason());

			Label firstLabel = new Label("Votes des autres joueurs :");
			firstLabel.setFont(Font.font(firstLabel.getFont().getFamily(), FontWeight.SEMI_BOLD, firstLabel.getFont().getSize()));

			VBox vBox = new VBox(firstLabel);
			vBox.setSpacing(5);

			for (String player : packet.getVoters()) {
				Label label = new Label(player + " -> (x)");
				vBox.getChildren().add(label);
				playersLabels.put(player, label);
			}

			setContent(vBox);
		}

		public void setVote(String player, String vote) {
			if (playersLabels.containsKey(player)) {
				playersLabels.get(player).setText(player + " -> " + vote);
			}
		}
	}

	public Map<Role, Integer> getRoleMap() {
		return roleMap;
	}
}
