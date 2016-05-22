package net.zyuiop.loupgarou.protocol.network;

import com.google.common.base.Charsets;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import net.zyuiop.loupgarou.protocol.BadPacketException;
import net.zyuiop.loupgarou.protocol.Packet;
import net.zyuiop.loupgarou.protocol.PacketData;
import net.zyuiop.loupgarou.protocol.ProtocolMap;

import java.util.Arrays;
import java.util.List;

/**
 * @author zyuiop
 *
 * Don't forget frame encoder/decoder
 */
public class PacketDecoder extends ByteToMessageDecoder {
	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
		int index = in.readerIndex();
		if (in.readableBytes() < 4) {
			in.readerIndex(index);
			return;
		}

		int size = in.readInt();
		if (in.readableBytes() < size) {
			in.readerIndex(index);
			return;
		}

		int packetId = in.readUnsignedByte();
		try {
			Class<? extends Packet> packetClass = ProtocolMap.getPacketFor(packetId);
			Packet packet = packetClass.newInstance();
			packet.read(new PacketData(in));
			out.add(packet);

			if (in.readerIndex() < index + size + 4) {
				int read = in.readerIndex() - (index + 4);
				byte[] data = new byte[size - read];
				in.readBytes(data);
				throw new BadPacketException("Packet " + packetClass.getName() + " wasn't read correctly : remaining " + (size - read) + " bytes to read. Content : " + Arrays.toString(data));
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println(in.toString(Charsets.UTF_8));
		}
	}
}
