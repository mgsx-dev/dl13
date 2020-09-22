package net.mgsx.dl13.assets;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.ObjectMap;

import net.mgsx.gltf.loaders.gltf.GLTFLoader;
import net.mgsx.gltf.scene3d.scene.SceneAsset;

public class GameAssets {
	public static GameAssets i;
	
	public static final String[] WORLD_IDS = {"E"}; // XXX {"A", "B", "C", "D"};
	
	public SceneAsset carA, bonus;

	public Texture brdfLUT;

	public IBL moonlessGolf;
	
	private ObjectMap<String, SceneAsset> worldMap = new ObjectMap<String, SceneAsset>();
	
	public GameAssets() {
		
		brdfLUT = new Texture(Gdx.files.classpath("net/mgsx/gltf/shaders/brdfLUT.png"));
		
		moonlessGolf = new IBL("ibl/moonless_golf_2k");
		
		for(String wID : WORLD_IDS){
			worldMap.put(wID, new GLTFLoader().load(Gdx.files.internal("models/world" + wID + ".gltf")));
		}
		
		carA = new GLTFLoader().load(Gdx.files.internal("models/carA.gltf"));
		
		bonus = new GLTFLoader().load(Gdx.files.internal("models/bonus.gltf"));
	}

	public SceneAsset world(String worldID) {
		return worldMap.get(worldID);
	}
}