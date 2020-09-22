package net.mgsx.dl13.screens;

import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.utils.ObjectMap;

import net.mgsx.dl13.model.GameWorld;

public class GameScreen extends ScreenAdapter {
	private GameWorld world;
	private ObjectMap<String, GameWorld> worldMap = new ObjectMap<String, GameWorld>();
	
	public GameScreen() {
		super();
		worldMap.put("A", new GameWorld("A"));
		worldMap.put("B", new GameWorld("B"));
		world = worldMap.get("B");
	}
	
	@Override
	public void render(float delta) {
		world.update(delta);
		
		world.render();
		
		super.render(delta);
		
		handleDoor();
		
	}

	private void handleDoor() {
		if(world.currentDoor != null){
			String nextWorldID = null;
			String nextAxisID = null;
			
			
			if(world.currentDoor.name.equals("b1")){
				nextWorldID = "A";
				nextAxisID = "a1";
			}else if(world.currentDoor.name.equals("a1")){
				nextWorldID = "B";
				nextAxisID = "b1";
			}
			
			if(nextWorldID != null && nextAxisID != null){
				
				GameWorld oldWorld = world;
				oldWorld.currentDoor = null;
				world = worldMap.get(nextWorldID);
				// change car position and synchronize
				world.resetPlayer(nextAxisID);
				world.player.sync(oldWorld.player);
			}
			
		}
	}
}
