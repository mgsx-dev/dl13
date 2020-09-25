package net.mgsx.dl13.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ObjectMap.Entry;
import com.badlogic.gdx.utils.viewport.FitViewport;

import net.mgsx.dl13.DL13Game;
import net.mgsx.dl13.assets.GameAssets;
import net.mgsx.dl13.model.GameModel;
import net.mgsx.dl13.model.GameWorld;
import net.mgsx.dl13.store.GameStore;
import net.mgsx.dl13.ui.GameHUD;
import net.mgsx.dl13.utils.StageScreen;

public class GameScreen extends StageScreen {
	private GameWorld world;
	private ObjectMap<String, GameWorld> worldMap = new ObjectMap<String, GameWorld>();
	private GameModel game;
	private GameHUD hud;
	private long carSoundID;
	
	public GameScreen(GameStore store) {
		super(new FitViewport(DL13Game.UIWidth, DL13Game.UIHeight));
		game = new GameModel(store);
		int nbBonus = 0;
		for(String wID : GameAssets.WORLD_IDS){
			GameWorld w = new GameWorld(game, wID);
			worldMap.put(wID, w);
			nbBonus += w.getBonusCount();
		}
		System.out.println("TotalBonus: " + nbBonus);
		setInitWorld("E", "e0");
		hud = new GameHUD(game, GameAssets.i.skin);
		stage.addActor(hud);
		hud.setFillParent(true);
		
		hud.launchCountDown();
		
		carSoundID = GameAssets.i.carSound.loop(0);
	}
	
	@Override
	public void dispose() {
		for(Entry<String, GameWorld> entry : worldMap){
			entry.value.dispose();
		}
		super.dispose();
	}
	
	private void setInitWorld(String wID, String axisID) {
		world = worldMap.get("E");
		world.resetPlayer(axisID, false);
	}

	@Override
	public void render(float delta) {
		if(DL13Game.debug){
			if(Gdx.input.isKeyJustPressed(Input.Keys.G)) finish();
		}
		
		float t = Math.abs(world.player.velocity) / 0.6f;
		float pitch = MathUtils.lerp(.4f, 2f, t * t * t);
		float vol = MathUtils.lerp(.4f, .25f, t * t);
		GameAssets.i.carSound.setVolume(carSoundID, vol);
		GameAssets.i.carSound.setPitch(carSoundID, pitch);
		// GameAssets.i.carSound.setLooping(carSoundID, true);
		// GameAssets.i.carSound.play();
		// GameAssets.i.carSound.loop();
		if(game.running){
			game.time += delta;
		}
		
		world.update(delta);
		
		world.render();
		
		viewport.apply();
		
		super.render(delta);
		
		handleDoor();
		
	}

	private void handleDoor() {
		if(world.currentDoor != null && game.running){
			String nextWorldID = null;
			String nextAxisID = null;
			
			Boolean isIn = null;
			// first wrap zone : startup to torus land
			if(world.currentDoor.name.equals("e1")){
				nextWorldID = "C";
				nextAxisID = "c1";
				isIn = true;
			}
			// from torus to first land (reverse)
			else if(world.currentDoor.name.equals("c1")){
				nextWorldID = "E";
				nextAxisID = "e1";
				isIn = false;
			}
			// from first land to parallel world
			else if(world.currentDoor.name.equals("e2")){
				nextWorldID = "B";
				nextAxisID = "b1";
				isIn = true;
			}
			// from parallel world to first land, loop back (trap)
			else if(world.currentDoor.name.equals("b1")){
				nextWorldID = "E";
				nextAxisID = "e2";
				isIn = false;
			}
			// from parallel world to first land, final area
			else if(world.currentDoor.name.equals("b2")){
				nextWorldID = "E";
				nextAxisID = "e3";
				isIn = false;
			}
			// finish line
			else if(world.currentDoor.name.equals("e3")){
				finish();
			}
			world.currentDoor = null;
			
			if(isIn != null){
				if(isIn){
					GameAssets.i.inSound.play();
				}else{
					GameAssets.i.outSound.play();
				}
			}
			
			if(nextWorldID != null && nextAxisID != null){
				GameWorld oldWorld = world;
				world = worldMap.get(nextWorldID);
				// change car position and synchronize
				world.resetPlayer(nextAxisID, true);
				world.player.sync(oldWorld.player);
			}
		}
	}

	private void finish() {
		GameAssets.i.carSound.stop();
		GameAssets.i.finishSound.play();
		game.finishLine = true;
		game.running = false;
		game.newRecord();
		DL13Game.save();
		stage.addAction(Actions.delay(2, Actions.run(()->hud.spawnFinish())));
	}
	
	@Override
	public void show() {
		GameAssets.i.stopSong();
		super.show();
	}
}
