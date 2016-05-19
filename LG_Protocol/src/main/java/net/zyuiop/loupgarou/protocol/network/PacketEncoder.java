package net.zyuiop.loupgarou.protocol.network;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.MessageToByteEncoder;
import net.zyuiop.loupgarou.protocol.Packet;
import net.zyuiop.loupgarou.protocol.PacketData;
import net.zyuiop.loupgarou.protocol.ProtocolMap;

import java.util.List;

/**
 * @author zyuiop
 */
public class PacketEncoder extends MessageToByteEncoder<Packet> {
	@Override
	protected void encode(ChannelHandlerContext ctx, Packet msg, ByteBuf out) throws Exception {
		ByteBuf buf = ctx.alloc().buffer();

		buf.writeByte(ProtocolMap.getPacketIdFor(msg));
		msg.write(new PacketData(buf));

		out.writeInt(buf.writerIndex());
		out.writeBytes(buf);
		buf.release();
	}
}
