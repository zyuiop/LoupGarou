package net.zyuiop.loupgarou.network.handlers;

import android.app.Activity;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import net.zyuiop.loupgarou.MainActivity;
import net.zyuiop.loupgarou.R;
import net.zyuiop.loupgarou.network.NetworkManager;
import net.zyuiop.loupgarou.network.PacketHandler;
import net.zyuiop.loupgarou.protocol.packets.clientbound.LoginResponsePacket;

/**
 * @author zyuiop
 */
public class LoginHandler implements PacketHandler<LoginResponsePacket> {
	private final NetworkManager networkManager;

	public LoginHandler(NetworkManager networkManager) {
		this.networkManager = networkManager;
	}

	@Override
	public void handle(final LoginResponsePacket packet) {
		if (packet.isSuccess()) {
			networkManager.finishLogin();
		} else {
			final Activity act = networkManager.getCurrentStage();
			if (act instanceof MainActivity) {
				((MainActivity) act).loginFailure(packet.getErrorMessage());
			} else {
				act.runOnUiThread(new Runnable() {
									  @Override
									  public void run() {
										  AlertDialog.Builder builder = new AlertDialog.Builder(act);
										  builder.setMessage(packet.getErrorMessage())
												  .setTitle(R.string.connection_error)
												  .setNeutralButton("OK", new DialogInterface.OnClickListener() {
													  @Override
													  public void onClick(DialogInterface dialogInterface, int i) {
														  dialogInterface.dismiss();
													  }
												  });
										  AlertDialog dialog = builder.create();
										  dialog.show();
									  }
								  }
				);
			}
			/*Platform.runLater(() -> {
				LGClient.logger.info("Error : " + packet.getErrorMessage());
				Alert error = new Alert(Alert.AlertType.ERROR, "Une erreur s'est produite lors de la connexion :\n" + packet.getErrorMessage(), new ButtonType("Fermer", ButtonBar.ButtonData.CANCEL_CLOSE));
				error.show();
				LoginWindow.getInstance().setConnecting(false);
			});*/
		}
	}
}
