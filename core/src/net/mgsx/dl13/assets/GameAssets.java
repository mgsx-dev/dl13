package net.mgsx.dl13.assets;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;

import net.mgsx.gltf.loaders.gltf.GLTFLoader;
import net.mgsx.gltf.scene3d.scene.SceneAsset;

public class GameAssets {
	public static GameAssets i;
	
	public SceneAsset worldA, carA;

	public Texture brdfLUT;

	public IBL moonlessGolf;
	
	public GameAssets() {
		
		brdfLUT = new Texture(Gdx.files.classpath("net/mgsx/gltf/shaders/brdfLUT.png"));
		
		moonlessGolf = new IBL("ibl/moonless_golf_2k");
		
		worldA = new GLTFLoader().load(Gdx.files.internal("models/worldA.gltf"));
		
		carA = new GLTFLoader().load(Gdx.files.internal("models/carA.gltf"));
	}
}
