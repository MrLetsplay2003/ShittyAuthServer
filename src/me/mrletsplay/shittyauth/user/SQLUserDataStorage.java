package me.mrletsplay.shittyauth.user;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import me.mrletsplay.shittyauth.textures.SkinType;
import me.mrletsplay.shittyauth.util.CryptoHelper;
import me.mrletsplay.webinterfaceapi.sql.SQLHelper;

public class SQLUserDataStorage implements UserDataStorage {

	@Override
	public void initialize() {
		SQLHelper.run(c -> {
			try(PreparedStatement st = c.prepareStatement("CREATE TABLE IF NOT EXISTS " + SQLHelper.tableName("shittyauth_user_data") + "(UserId VARCHAR(255) PRIMARY KEY, HasCape BOOLEAN, SkinType VARCHAR(255), SkinLastChanged BIGINT, CapeLastChanged BIGINT, PublicKey BLOB, PrivateKey BLOB)")) {
				st.execute();
			}
		});
	}

	@Override
	public void updateUserData(String accID, UserData userData) {
		SQLHelper.run(c -> {
			try(PreparedStatement st = c.prepareStatement("INSERT INTO " + SQLHelper.tableName("shittyauth_user_data") + "(UserId, HasCape, SkinType, SkinLastChanged, CapeLastChanged, PublicKey, PrivateKey) VALUES(?, ?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE HasCape = VALUES(HasCape), SkinType = VALUES(SkinType), SkinLastChanged = VALUES(SkinLastChanged), CapeLastChanged = VALUES(CapeLastChanged), PublicKey = VALUES(PublicKey), PrivateKey = VALUES(PrivateKey)")) {
				st.setString(1, accID);
				st.setBoolean(2, userData.hasCape());
				st.setString(3, userData.getSkinType().name());
				st.setLong(4, userData.getSkinLastChanged());
				st.setLong(5, userData.getCapeLastChanged());
				st.setBytes(6, userData.getPublicKey().getEncoded());
				st.setBytes(7, userData.getPrivateKey().getEncoded());
				st.execute();
			}
		});
	}

	@Override
	public UserData getUserData(String accID) {
		return SQLHelper.run(c -> {
			try(PreparedStatement st = c.prepareStatement("SELECT * FROM " + SQLHelper.tableName("shittyauth_user_data") + " WHERE UserId = ?")) {
				st.setString(1, accID);
				try(ResultSet r = st.executeQuery()) {
					if(!r.next()) return null;
					return new UserData(
						r.getBoolean("HasCape"),
						SkinType.valueOf(r.getString("SkinType")),
						r.getLong("SkinLastChanged"),
						r.getLong("CapeLastChanged"),
						CryptoHelper.parseRSAPublicKey(r.getBytes("PublicKey")),
						CryptoHelper.parseRSAPrivateKey(r.getBytes("PrivateKey"))
					);
				}
			}
		});
	}

}
