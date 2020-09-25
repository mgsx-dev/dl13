package net.mgsx.dl13.assets;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
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
	
	public Sound carSound, collisionSound, inSound, outSound, bonusSoundSoft, bonusSoundHard, uiSound, finishSound;
	public Music mainSong;

	private Music currentSong, introSong;
	
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
		
		carSound = Gdx.audio.newSound(Gdx.files.internal("sfx/cars3.wav"));
		inSound = Gdx.audio.newSound(Gdx.files.internal("sfx/in.wav"));
		outSound = Gdx.audio.newSound(Gdx.files.internal("sfx/out.wav"));
		collisionSound = Gdx.audio.newSound(Gdx.files.internal("sfx/collision.wav"));
		bonusSoundSoft = Gdx.audio.newSound(Gdx.files.internal("sfx/bonus4.wav"));
		bonusSoundHard = Gdx.audio.newSound(Gdx.files.internal("sfx/bonus3.wav"));
		uiSound = Gdx.audio.newSound(Gdx.files.internal("sfx/ui-short.wav"));
		finishSound = Gdx.audio.newSound(Gdx.files.internal("sfx/good.mp3"));

		
		mainSong = Gdx.audio.newMusic(Gdx.files.internal("music/MRsong.mp3"));
		introSong = Gdx.audio.newMusic(Gdx.files.internal("music/MRmenu.mp3"));
		
	}

	public SceneAsset world(String worldID) {
		return worldMap.get(worldID);
	}

	public void playUI() {
		uiSound.play(0.3f);
	}
	public void playUIHard() {
		bonusSoundHard.play(0.5f);
	}

	public void playSongGame() {
		playSong(mainSong);
		
	}
	public void playSongMenu() {
		playSong(introSong);
	}
	private void playSong(Music song){
		if(currentSong == song) return;
		stopSong();
		currentSong = song;
		song.setLooping(true);
		song.play();
	}

	public void stopSong() {
		if(currentSong!= null){
			currentSong.stop();
			currentSong = null;
		}
		
	}
}
