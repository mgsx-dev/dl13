package net.mgsx.dl13.screens;

import com.badlogic.gdx.ScreenAdapter;

import net.mgsx.dl13.model.GameWorld;

public class GameScreen extends ScreenAdapter {
	private GameWorld world;
	
	public GameScreen() {
		super();
		world = new GameWorld();
	}
	
	@Override
	public void render(float delta) {
		world.update(delta);
		
		world.render();
		
		super.render(delta);

	}
}
