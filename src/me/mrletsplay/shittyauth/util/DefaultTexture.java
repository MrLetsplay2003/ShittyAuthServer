package me.mrletsplay.shittyauth.util;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public enum DefaultTexture {

	SKIN_STEVE_CLASSIC("Steve (Classic)", "1a4af718455d4aab528e7a61f86fa25e6a369d1768dcb13f7df319a713eb810b"),

	SKIN_STEVE("Steve", "31f477eb1a7beee631c2ca64d06f8f68fa93a3386d04452ab27f43acdf1b60cb"),
	SKIN_ALEX("Alex", "1abc803022d8300ab7578b189294cce39622d9a404cdc00d3feacfdf45be6981"),
	SKIN_ZURI("Zuri", "f5dddb41dcafef616e959c2817808e0be741c89ffbfed39134a13e75b811863d"),
	SKIN_SUNNY("Sunny", "a3bd16079f764cd541e072e888fe43885e711f98658323db0f9a6045da91ee7a"),
	SKIN_NOOR("Noor", "90e75cd429ba6331cd210b9bd19399527ee3bab467b5a9f61cb8a27b177f6789"),
	SKIN_MAKENA("Makena", "dc0fcfaf2aa040a83dc0de4e56058d1bbb2ea40157501f3e7d15dc245e493095"),
	SKIN_KAI("Kai", "e5cdc3243b2153ab28a159861be643a4fc1e3c17d291cdd3e57a7f370ad676f3"),
	SKIN_EFE("Efe", "daf3d88ccb38f11f74814e92053d92f7728ddb1a7955652a60e30cb27ae6659f"),
	SKIN_ARI("Ari", "4c05ab9e07b3505dc3ec11370c3bdce5570ad2fb2b562e9b9dd9cf271f81aa44"),

	SKIN_STEVE_SLIM("Steve", "d5c4ee5ce20aed9e33e866c66caa37178606234b3721084bf01d13320fb2eb3f"),
	SKIN_ALEX_SLIM("Alex", "46acd06e8483b176e8ea39fc12fe105eb3a2a4970f5100057e9d84d4b60bdfa7"),
	SKIN_ZURI_SLIM("Zuri", "eee522611005acf256dbd152e992c60c0bb7978cb0f3127807700e478ad97664"),
	SKIN_SUNNY_SLIM("Sunny", "b66bc80f002b10371e2fa23de6f230dd5e2f3affc2e15786f65bc9be4c6eb71a"),
	SKIN_NOOR_SLIM("Noor", "6c160fbd16adbc4bff2409e70180d911002aebcfa811eb6ec3d1040761aea6dd"),
	SKIN_MAKENA_SLIM("Makena", "7cb3ba52ddd5cc82c0b050c3f920f87da36add80165846f479079663805433db"),
	SKIN_KAI_SLIM("Kai", "226c617fde5b1ba569aa08bd2cb6fd84c93337532a872b3eb7bf66bdd5b395f8"),
	SKIN_EFE_SLIM("Efe", "fece7017b1bb13926d1158864b283b8b930271f80a90482f174cca6a17e88236"),
	SKIN_ARI_SLIM("Ari", "6ac6ca262d67bcfb3dbc924ba8215a18195497c780058a5749de674217721892"),

	CAPE_MOJANG_OLD("Mojang (Classic)", "8f120319222a9f4a104e2f5cb97b2cda93199a2ee9e1585cb8d09d6f687cb761"),
	CAPE_MOJANG("Mojang", "5786fe99be377dfb6858859f926c4dbc995751e91cee373468c5fbf4865e7151"),
	CAPE_MOJANG_NEW("Mojang Studios", "9e507afc56359978a3eb3e32367042b853cddd0995d17d0da995662913fb00f7"),

	CAPE_MINECON_2011("MINECON 2011", "953cac8b779fe41383e675ee2b86071a71658f2180f56fbce8aa315ea70e2ed6"),
	CAPE_MINECON_2012("MINECON 2012", "a2e8d97ec79100e90a75d369d1b3ba81273c4f82bc1b737e934eed4a854be1b6"),
	CAPE_MINECON_2013("MINECON 2013", "153b1a0dfcbae953cdeb6f2c2bf6bf79943239b1372780da44bcbb29273131da"),
	CAPE_MINECON_2015("MINECON 2015", "b0cc08840700447322d953a02b965f1d65a13a603bf64b17c803c21446fe1635"),
	CAPE_MINECON_2016("MINECON 2016", "e7dfea16dc83c97df01a12fabbd1216359c0cd0ea42f9999b6e97c584963e980"),

	CAPE_MILLIONTH_SALE("Millionth Customer", "70efffaf86fe5bc089608d3cb297d3e276b9eb7a8f9f2fe6659c23a2d8b18edf"),
	CAPE_DANNYBSTYLE("dannyBstyle", "bcfbe84c6542a4a5c213c1cacf8979b5e913dcb4ad783a8b80e3c4a7d5c8bdac"),
	CAPE_JULIANCLARK("JulianClark", "23ec737f18bfe4b547c95935fc297dd767bb84ee55bfd855144d279ac9bfd9fe"),
	CAPE_CHEAPSH0T("cheapsh0t", "ca29f5dd9e94fb1748203b92e36b66fda80750c87ebc18d6eafdb0e28cc1d05f"),
	CAPE_MRMESSIAH("MrMessiah", "2e002d5e1758e79ba51d08d92a0f3a95119f2f435ae7704916507b6c565a7da8"),
	CAPE_PRISMARINE("Prismarine", "d8f8d13a1adf9636a16c31d47f3ecc9bb8d8533108aa5ad2a01b13b1a0c55eac"),
	CAPE_BIRTHDAY("Birthday", "2056f2eebd759cce93460907186ef44e9192954ae12b227d817eb4b55627a7fc"),
	// CAPE_VALENTINE

	CAPE_TRANSLATOR("Translator", "1bf91499701404e21bd46b0191d63239a4ef76ebde88d27e4d430ac211df681e"),
	CAPE_TRANSLATOR_CHINESE("Chinese Translator", "2262fb1d24912209490586ecae98aca8500df3eff91f2a07da37ee524e7e3cb6"),
	CAPE_SCROLLS_CHAMP("Scrolls Champion", "3efadf6510961830f9fcc077f19b4daf286d502b5f5aafbd807c7bbffcaca245"),
	CAPE_COBALT("Cobalt", "ca35c56efe71ed290385f4ab5346a1826b546a54d519e6a3ff01efa01acce81"),
	CAPE_MODERATOR("Mojira Moderator", "ae677f7d98ac70a533713518416df4452fe5700365c09cf45d0d156ea9396551"),
	CAPE_MAP_MAKER("Realms MapMaker", "17912790ff164b93196f08ba71d0e62129304776d0f347334f8a6eae509f8a56"),
	CAPE_TURTLE("Turtle", "5048ea61566353397247d2b7d946034de926b997d5e66c86483dfb1e031aee95"),

	CAPE_MIGRATOR("Migrator", "2340c0e03dd24a11b15a8b33c2a7e9e32abb2051b2481d0ba7defd635ca7a933"),
	CAPE_VANILLA("Vanilla", "f9a76537647989f9a0b6d001e320dac591c359e9e61a31f4ce11c88f207f0ad4"),

	CAPE_15_YEAR_ANNIVERSARY("15 Year Anniversary", "cd9d82ab17fd92022dbd4a86cde4c382a7540e117fae7b9a2853658505a80625"),
	;

	public static final String TEXTURE_URL = "https://textures.minecraft.net/texture/%s";
	public static final Path DEFAULT_TEXTURES_PATH = Paths.get("shittyauth/textures");

	private static final List<DefaultTexture>
		SKINS,
		SLIM_SKINS,
		CAPES;

	static {
		List<DefaultTexture>
			skins = new ArrayList<>(),
			slimSkins = new ArrayList<>(),
			capes = new ArrayList<>();

		for(DefaultTexture texture : values()) {
			if(texture.name().startsWith("SKIN_")) {
				(texture.name().endsWith("_SLIM") ? slimSkins : skins).add(texture);
			}else {
				capes.add(texture);
			}
		}

		SKINS = Collections.unmodifiableList(skins);
		SLIM_SKINS = Collections.unmodifiableList(slimSkins);
		CAPES = Collections.unmodifiableList(capes);
	}

	private final String name;
	private final String textureID;

	private DefaultTexture(String name, String textureID) {
		this.name = name;
		this.textureID = textureID;
	}

	public String getName() {
		return name;
	}

	public String getTextureID() {
		return textureID;
	}

	public String getURL() {
		return String.format(TEXTURE_URL, textureID);
	}

	public Path getPath() {
		return DEFAULT_TEXTURES_PATH.resolve(name().toLowerCase() + ".png");
	}

	public static List<DefaultTexture> getSkins() {
		return SKINS;
	}

	public static List<DefaultTexture> getSlimSkins() {
		return SLIM_SKINS;
	}

	public static List<DefaultTexture> getCapes() {
		return CAPES;
	}

}
