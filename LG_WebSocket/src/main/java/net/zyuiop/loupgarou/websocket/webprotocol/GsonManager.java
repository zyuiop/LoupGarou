package net.zyuiop.loupgarou.websocket.webprotocol;

import java.lang.reflect.Type;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import net.zyuiop.loupgarou.protocol.BadPacketException;
import net.zyuiop.loupgarou.protocol.Packet;
import net.zyuiop.loupgarou.protocol.ProtocolMap;

/**
 * Created by zyuiop on 30/07/2016.
 * Part of the lg-parent project.
 */
public class GsonManager implements JsonSerializer<Packet>, JsonDeserializer<Packet> {
	private static Gson gson = new GsonBuilder().registerTypeAdapter(Packet.class, new GsonManager()).create();

	private GsonManager() {}

	public static Gson getGson() {
		return gson;
	}

	@Override
	public Packet deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
		int id = jsonElement.getAsJsonObject().get("packetId").getAsInt();
		try {
			Class<? extends Packet> packetClass = ProtocolMap.getPacketFor(id);
			return jsonDeserializationContext.deserialize(jsonElement, packetClass);
		} catch (BadPacketException e) {
			throw new JsonParseException("Incorrect packet id");
		}
	}

	@Override
	public JsonElement serialize(Packet packet, Type type, JsonSerializationContext jsonSerializationContext) {
		JsonObject elt = jsonSerializationContext.serialize(packet).getAsJsonObject();
		try {
			elt.add("packetId", new JsonPrimitive(ProtocolMap.getPacketIdFor(packet)));
		} catch (BadPacketException e) {
			return null;
		}
		return elt;
	}
}
