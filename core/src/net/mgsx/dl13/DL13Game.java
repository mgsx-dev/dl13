package net.mgsx.dl13;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.FPSLogger;
import com.badlogic.gdx.utils.Json;

import net.mgsx.dl13.assets.GameAssets;
import net.mgsx.dl13.model.GameInputs;
import net.mgsx.dl13.screens.GameScreen;
import net.mgsx.dl13.screens.LogoScreen;
import net.mgsx.dl13.screens.SelectScreen;
import net.mgsx.dl13.screens.TitleScreen;
import net.mgsx.dl13.store.GameStore;

public class DL13Game extends Game {
	
	private static final String PREF_FILE = "dl13";
	private static final String PREF_KEY = "save";
	public static final int UIWidth = 1280;
	public static final int UIHeight = 960;
	public static final Color skyColor = new Color(.5f, .8f, 1f, 1f);
	public static final Color neutralColor = new Color(.8f, .8f, .8f, 1f);
	
	private GameStore store;
	private GameInputs inputManager;
	
	public static boolean debug = false;
	public static boolean perf = false;
	
	public static enum ScreenState{
		LOGO, TITLE, SELECT, GAME
	}

	private ScreenState nextState;
	private FPSLogger fpsLogger; 
	
	public static void save(){
		((DL13Game)Gdx.app.getApplicationListener()).saveStore();
	}
	
	public static GameInputs getInputs(){
		return ((DL13Game)Gdx.app.getApplicationListener()).inputManager;
	}

	@Override
	public void create () {
		fpsLogger = new FPSLogger();
		
		GameAssets.i = new GameAssets();
		loadStore();
		
		inputManager = new GameInputs(Gdx.app.getPreferences(PREF_FILE));
		
		// setScreenState(ScreenState.TITLE);
		setScreenState(ScreenState.LOGO);
		// setScreenState(ScreenState.GAME);
		// setScreenState(ScreenState.SELECT);
	}

	private void loadStore() {
		Preferences pref = Gdx.app.getPreferences(PREF_FILE);
		String json = pref.getString(PREF_KEY, null);
		if(json != null){
			store = new Json().fromJson(GameStore.class, json);
			store.compute();
		}else{
			store = new GameStore();
		}
	}
	
	private void saveStore() {
		store.compress();
		Preferences pref = Gdx.app.getPreferences(PREF_FILE);
		String json = new Json().toJson(store);
		pref.putString(PREF_KEY, json);
		pref.flush();
	}

	public static void toScreen(ScreenState state) {
		((DL13Game)Gdx.app.getApplicationListener()).nextState = state;
	}
	private void setScreenState(ScreenState state){
		Screen lastScreen = screen;
		switch(state){
		default:
		case GAME:
			setScreen(new GameScreen(store));
			break;
		case SELECT:
			setScreen(new SelectScreen(store));
			break;
		case TITLE:
			setScreen(new TitleScreen(store));
			break;
		case LOGO:
			setScreen(new LogoScreen(store));
			break;
		}
		if(lastScreen != null) lastScreen.dispose();
	}

	@Override
	public void render() {
		if(debug && Gdx.input.isKeyJustPressed(Input.Keys.L)){
			toScreen(ScreenState.LOGO);
		}
		super.render();
		if(nextState != null){
			setScreenState(nextState);
			nextState = null;
		}
		if(perf) fpsLogger.log();
	}

}
