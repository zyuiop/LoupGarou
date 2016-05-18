package net.zyuiop.loupgarou.server.network;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import net.zyuiop.loupgarou.protocol.Packet;
import net.zyuiop.loupgarou.protocol.ProtocolMap;
import net.zyuiop.loupgarou.protocol.packets.clientbound.GameListPacket;
import net.zyuiop.loupgarou.protocol.packets.serverbound.LoginPacket;
import net.zyuiop.loupgarou.protocol.packets.clientbound.LoginResponsePacket;
import net.zyuiop.loupgarou.server.LGServer;
import net.zyuiop.loupgarou.server.auth.AuthenticationService;
import net.zyuiop.loupgarou.server.game.Game;
import net.zyuiop.loupgarou.server.game.GamesManager;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * @author zyuiop
 */
public class ProtocolHandler extends ChannelInboundHandlerAdapter {
	private static Pattern namePattern = Pattern.compile("^[a-zA-Z0-9_-]{3,20}$");
	private static Map<Class<? extends Packet>, PacketHandler<?>> handlerMap = new HashMap<>();

	private ConnectedClient client = null;

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {

	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		if (client != null) {
			LGServer.getLogger().info("Client disconnected : " + client.getName());
			client.unregister();
			client = null;
		}
	}

	public void closeConnexion(ChannelHandlerContext ctx, Packet lastPacket) {
		ctx.writeAndFlush(lastPacket)
				.addListener(new ChannelFutureListener() {
					@Override
					public void operationComplete(ChannelFuture future) throws Exception {
						future.channel().close();
					}
				});
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		if (!(msg instanceof LoginPacket)) {
			if (client == null) {
				ctx.disconnect();
			} else {
				Packet packet = (Packet) msg;
				handle(packet);
			}
		} else {
			if (client == null) {
				LoginPacket loginPacket = (LoginPacket) msg;
				if (loginPacket.getProtocolVersion() > ProtocolMap.protocolVersion) {
					closeConnexion(ctx, new LoginResponsePacket(false, "Serveur obsolète ! \nUtilisez le protocole " + ProtocolMap.protocolVersion));
					return;
				} else if (loginPacket.getProtocolVersion() < ProtocolMap.protocolVersion) {
					closeConnexion(ctx, new LoginResponsePacket(false, "Client obsolète ! \nUtilisez le protocole " + ProtocolMap.protocolVersion));
					return;
				}

				String username = loginPacket.getUsername();
				if (!namePattern.matcher(username).find()) {
					closeConnexion(ctx, new LoginResponsePacket(false, "Nom d'utilisateur invalide : \nde 3 à 20 caractères alphanumériques.."));
					return;
				}

				if (ConnectedClient.getClient(loginPacket.getUsername()) != null) {
					closeConnexion(ctx, new LoginResponsePacket(false, "Ce nom d'utilisateur est déjà utilisé."));
					return;
				}

				AuthenticationService service = LGServer.getInstance().getAuthenticationService();
				String storedPubKey = service.getStoredPublicKey(username);
				if (storedPubKey != null && !loginPacket.isEnforceAuth()) {
					closeConnexion(ctx, new LoginResponsePacket(false, "Compte protégé par vérification RSA."));
					return;
				} else if (loginPacket.isEnforceAuth()) {
					if (storedPubKey != null) {
						// On a une clé
						if (loginPacket.getPublicKey().equals(storedPubKey)) {
							boolean valid = service.isSignatureValid(username, storedPubKey, loginPacket.getTimestamp(), loginPacket.getSignature());
							if (!valid) {
								closeConnexion(ctx, new LoginResponsePacket(false, "Signature invalide."));
								return;
							}
						} else {
							closeConnexion(ctx, new LoginResponsePacket(false, "Clé publique invalide."));
							return;
						}
					} else {
						// On a pas encore de clé, on la crée
						boolean valid = service.isSignatureValid(username, loginPacket.getPublicKey(), loginPacket.getTimestamp(), loginPacket.getSignature());
						if (!valid) {
							closeConnexion(ctx, new LoginResponsePacket(false, "Signature invalide."));
							return;
						} else {
							LGServer.getLogger().info("Saving public key for user " + username + " o/");
							service.saveKey(username, loginPacket.getPublicKey());
						}
					}
					ctx.writeAndFlush(new LoginResponsePacket(true, "Welcome, " + ((LoginPacket) msg).getUsername() + " !"));
				} else {
					ctx.writeAndFlush(new LoginResponsePacket(true, "Welcome, " + ((LoginPacket) msg).getUsername() + " ! It's advised to use RSA authentication to avoid someone else to steal your username."));
				}


				client = new ConnectedClient(ctx, loginPacket.getUsername());
				LGServer.getLogger().info(client.getName() + " has logged in from " + ctx.channel().remoteAddress());
				GamesManager.login(client);
			}
		}
	}

	private <T extends Packet> void handle(T packet) {
		PacketHandler<T> handler = (PacketHandler<T>) handlerMap.get(packet.getClass());
		if (handler != null)
			handler.handle(packet, client);
	}

	public static <T extends Packet> void handle(Class<T> clazz, PacketHandler<T> handler) {
		handlerMap.put(clazz, handler);
		LGServer.getLogger().info("[Protocol] " + clazz.getName() + " packets will be sent to " + handler + ".");
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		LGServer.getLogger().warn("Exception caught on client " + (client == null ? ctx.name() : client.getName()), cause);
		if (client != null)
			client.unregister();
	}
}
