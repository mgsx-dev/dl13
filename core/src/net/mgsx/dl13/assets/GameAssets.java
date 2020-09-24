package net.mgsx.dl13.assets;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.ObjectMap;

import net.mgsx.gltf.loaders.gltf.GLTFLoader;
import net.mgsx.gltf.scene3d.scene.SceneAsset;

public class GameAssets {
	public static GameAssets i;
	
	public static final String[] WORLD_IDS = {"B", "C", "E"};
	
	public SceneAsset carA, carB, carC, bonus;

	public Texture brdfLUT;

	public IBL night, day, interior;
	
	private ObjectMap<String, SceneAsset> worldMap = new ObjectMap<String, SceneAsset>();

	public Skin skin;
	
	public GameAssets() {
		
		skin = new Skin(Gdx.files.internal("skins/game-skin.json"));
		
		brdfLUT = new Texture(Gdx.files.classpath("net/mgsx/gltf/shaders/brdfLUT.png"));
		
		interior = new IBL("ibl/interior", ".png", 7);
		night = new IBL("ibl/kloppen", ".png", 7);
		day = new IBL("ibl/kloof", ".png", 7);
		
		for(String wID : WORLD_IDS){
			worldMap.put(wID, new GLTFLoader().load(Gdx.files.internal("models/world" + wID + ".gltf")));
		}
		
		carA = new GLTFLoader().load(Gdx.files.internal("models/carA.gltf"));
		carB = new GLTFLoader().load(Gdx.files.internal("models/carB.gltf"));
		carC = new GLTFLoader().load(Gdx.files.internal("models/carC.gltf"));
		
		bonus = new GLTFLoader().load(Gdx.files.internal("models/bonus.gltf"));
	}

	public SceneAsset world(String worldID) {
		return worldMap.get(worldID);
	}
}
