package net.mgsx.dl13;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.utils.Json;

import net.mgsx.dl13.assets.GameAssets;
import net.mgsx.dl13.screens.GameScreen;
import net.mgsx.dl13.store.GameStore;

public class DL13Game extends Game {
	
	private static final String PREF_FILE = "dl13";
	private static final String PREF_KEY = "save";
	private GameStore store;
	
	public static boolean debug = true;

	public static void save(){
		((DL13Game)Gdx.app.getApplicationListener()).saveStore();
	}

	@Override
	public void create () {
		GameAssets.i = new GameAssets();
		loadStore();
		newGame();
	}

	private void loadStore() {
		Preferences pref = Gdx.app.getPreferences(PREF_FILE);
		String json = pref.getString(PREF_KEY, null);
		if(json != null){
			store = new Json().fromJson(GameStore.class, json);
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

	public static void toTitleScreen() {
		((DL13Game)Gdx.app.getApplicationListener()).newGame(); // TODO title screen
	}

	private void newGame() {
		setScreen(new GameScreen(store));
	}

}
