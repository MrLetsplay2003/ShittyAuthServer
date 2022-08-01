package me.mrletsplay.shittyauth.util;

import java.nio.ByteBuffer;
import java.util.UUID;

import me.mrletsplay.mrcore.misc.ByteUtils;

public class UUIDHelper {

	public static String toShortUUID(UUID uuid) {
		return ByteUtils.bytesToHex(getBytesFromUUID(uuid));
	}

	public static UUID parseShortUUID(String shortUUID) {
		try {
			return getUUIDFromBytes(ByteUtils.hexToBytes(shortUUID));
		}catch(IllegalArgumentException e) {
			return null;
		}
	}

	private static byte[] getBytesFromUUID(UUID uuid) {
		ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
		bb.putLong(uuid.getMostSignificantBits());
		bb.putLong(uuid.getLeastSignificantBits());
		return bb.array();
	}

	private static UUID getUUIDFromBytes(byte[] bytes) {
		ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
		Long high = byteBuffer.getLong();
		Long low = byteBuffer.getLong();
		return new UUID(high, low);
	}

}
