package net.zyuiop.loupgarou.protocol.network;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import net.zyuiop.loupgarou.protocol.Packet;
import net.zyuiop.loupgarou.protocol.PacketData;
import net.zyuiop.loupgarou.protocol.ProtocolMap;

import java.util.List;

/**
 * @author zyuiop
 */
public class PacketDecoder extends ByteToMessageDecoder {
	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
		int packetId = in.readInt();
		Class<? extends Packet> packetClass = ProtocolMap.getPacketFor(packetId);
		Packet packet = packetClass.newInstance();
		packet.read(new PacketData(in));
		out.add(packet);
	}
}
