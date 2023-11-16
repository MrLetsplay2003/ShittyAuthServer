package me.mrletsplay.shittyauth.user;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;

import me.mrletsplay.mrcore.json.JSONObject;
import me.mrletsplay.mrcore.json.converter.JSONConstructor;
import me.mrletsplay.mrcore.json.converter.JSONConvertible;
import me.mrletsplay.mrcore.json.converter.JSONValue;
import me.mrletsplay.shittyauth.textures.SkinType;
import me.mrletsplay.shittyauth.util.CryptoHelper;

public class UserData implements JSONConvertible {

	@JSONValue
	private boolean hasCape;

	@JSONValue
	private SkinType skinType;

	@JSONValue
	private long skinLastChanged; // To update skin cache in client after change

	@JSONValue
	private long capeLastChanged; // To update cape cache in client after change

	private PublicKey publicKey;

	private PrivateKey privateKey;

	@JSONConstructor
	private UserData() {}

	public UserData(boolean hasCape, SkinType skinType, long skinLastChanged, long capeLastChanged, PublicKey publicKey, PrivateKey privateKey) {
		this.hasCape = hasCape;
		this.skinType = skinType;
		this.skinLastChanged = skinLastChanged;
		this.capeLastChanged = capeLastChanged;
		this.publicKey = publicKey;
		this.privateKey = privateKey;
	}

	public void setHasCape(boolean hasCape) {
		this.hasCape = hasCape;
	}

	public boolean hasCape() {
		return hasCape;
	}

	public void setSkinType(SkinType skinType) {
		this.skinType = skinType;
	}

	public SkinType getSkinType() {
		return skinType;
	}

	public void setSkinLastChanged(long skinLastChanged) {
		this.skinLastChanged = skinLastChanged;
	}

	public long getSkinLastChanged() {
		return skinLastChanged;
	}

	public void setCapeLastChanged(long capeLastChanged) {
		this.capeLastChanged = capeLastChanged;
	}

	public long getCapeLastChanged() {
		return capeLastChanged;
	}

	public PublicKey getPublicKey() {
		return publicKey;
	}

	public PrivateKey getPrivateKey() {
		return privateKey;
	}

	public boolean hasKeyPair() {
		return publicKey != null && privateKey != null;
	}

	public void generateNewKeyPair() {
		KeyPair pair = CryptoHelper.generateRSAKeyPair();
		this.publicKey = pair.getPublic();
		this.privateKey = pair.getPrivate();
	}

	@Override
	public void preSerialize(JSONObject object) {
		object.put("publicKey" ,Base64.getEncoder().encodeToString(publicKey.getEncoded()));
		object.put("privateKey" ,Base64.getEncoder().encodeToString(privateKey.getEncoded()));
	}

	@Override
	public void preDeserialize(JSONObject object) {
		publicKey = CryptoHelper.parseRSAPublicKey(Base64.getDecoder().decode(object.getString("publicKey")));
		privateKey = CryptoHelper.parseRSAPrivateKey(Base64.getDecoder().decode(object.getString("privateKey")));
	}

	public static UserData createNew() {
		UserData ud = new UserData();
		ud.setSkinType(SkinType.STEVE);
		ud.generateNewKeyPair();
		return ud;
	}

}
