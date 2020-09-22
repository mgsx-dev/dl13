package net.mgsx.dl13;

import com.badlogic.gdx.Game;

import net.mgsx.dl13.assets.GameAssets;
import net.mgsx.dl13.screens.GameScreen;

public class DL13Game extends Game {
	
	
	public static boolean debug = true;

	@Override
	public void create () {
		GameAssets.i = new GameAssets();
		setScreen(new GameScreen());
	}

}
