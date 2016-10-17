package net.zyuiop.loupgarou.network.handlers;

import net.zyuiop.loupgarou.network.NetworkManager;
import net.zyuiop.loupgarou.network.PacketHandler;
import net.zyuiop.loupgarou.protocol.packets.clientbound.GameLeavePacket;

/**
 * @author zyuiop
 */
public class GameLeaveHandler implements PacketHandler<GameLeavePacket> {
	private final NetworkManager manager;

	public GameLeaveHandler(NetworkManager manager) {
		this.manager = manager;
	}

	@Override
	public void handle(GameLeavePacket packet) {
		/*Platform.runLater(() -> {
			manager.leaveGame();
			new Alert(Alert.AlertType.INFORMATION, "Vous avez quitté la partie :\n" + packet.getReason(), new ButtonType("Ok", ButtonBar.ButtonData.OK_DONE)).show();
		});*/
	}
}
