package net.mgsx.dl13.screens;

import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.utils.ObjectMap;

import net.mgsx.dl13.assets.GameAssets;
import net.mgsx.dl13.model.GameWorld;

public class GameScreen extends ScreenAdapter {
	private GameWorld world;
	private ObjectMap<String, GameWorld> worldMap = new ObjectMap<String, GameWorld>();
	private boolean finishLine;
	
	public GameScreen() {
		super();
		for(String wID : GameAssets.WORLD_IDS){
			GameWorld w = new GameWorld(wID);
			worldMap.put(wID, w);
		}
		setInitWorld("E", "e0");
	}
	
	private void setInitWorld(String wID, String axisID) {
		world = worldMap.get("E");
		world.resetPlayer(axisID);
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
			
			// first wrap zone : startup to torus land
			if(world.currentDoor.name.equals("e1")){
				nextWorldID = "C";
				nextAxisID = "c1";
			}
			// from torus to first land (reverse)
			else if(world.currentDoor.name.equals("c1")){
				nextWorldID = "E";
				nextAxisID = "e1";
			}
			// from first land to parallel world
			else if(world.currentDoor.name.equals("e2")){
				nextWorldID = "B";
				nextAxisID = "b1";
			}
			// from parallel world to first land, loop back (trap)
			else if(world.currentDoor.name.equals("b1")){
				nextWorldID = "E";
				nextAxisID = "e2";
			}
			// from parallel world to first land, final area
			else if(world.currentDoor.name.equals("b2")){
				nextWorldID = "E";
				nextAxisID = "e3";
			}
			// finish line
			else if(world.currentDoor.name.equals("e3")){
				finishLine = true;
				// TODO no more controls, fade to title screen...
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
