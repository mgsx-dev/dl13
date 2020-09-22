package net.mgsx.dl13.assets;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.ObjectMap;

import net.mgsx.gltf.loaders.gltf.GLTFLoader;
import net.mgsx.gltf.scene3d.scene.SceneAsset;

public class GameAssets {
	public static GameAssets i;
	
	public SceneAsset worldA, carA, bonus, worldB;

	public Texture brdfLUT;

	public IBL moonlessGolf;
	
	private ObjectMap<String, SceneAsset> worldMap = new ObjectMap<String, SceneAsset>();
	
	public GameAssets() {
		
		brdfLUT = new Texture(Gdx.files.classpath("net/mgsx/gltf/shaders/brdfLUT.png"));
		
		moonlessGolf = new IBL("ibl/moonless_golf_2k");
		
		worldA = new GLTFLoader().load(Gdx.files.internal("models/worldA.gltf"));
		worldB = new GLTFLoader().load(Gdx.files.internal("models/worldB.gltf"));
		
		worldMap.put("A", worldA);
		worldMap.put("B", worldB);
		
		carA = new GLTFLoader().load(Gdx.files.internal("models/carA.gltf"));
		
		bonus = new GLTFLoader().load(Gdx.files.internal("models/bonus.gltf"));
	}

	public SceneAsset world(String worldID) {
		return worldMap.get(worldID);
	}
}
