package me.mrletsplay.shittyauth.user;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;

import me.mrletsplay.mrcore.json.converter.JSONConstructor;
import me.mrletsplay.mrcore.json.converter.JSONConvertible;
import me.mrletsplay.mrcore.json.converter.JSONValue;
import me.mrletsplay.shittyauth.textures.SkinType;
import me.mrletsplay.shittyauth.util.CryptoHelper;

public class UserData implements JSONConvertible {

	public static final String DEFAULT_SKIN = "https://textures.minecraft.net/texture/1a4af718455d4aab528e7a61f86fa25e6a369d1768dcb13f7df319a713eb810b";

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
	public UserData() {
		this.skinType = SkinType.STEVE;
		KeyPair pair = CryptoHelper.generateRSAKeyPair();
		this.publicKey = pair.getPublic();
		this.privateKey = pair.getPrivate();
	}

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

}
