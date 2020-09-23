package net.mgsx.dl13.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.viewport.FitViewport;

import net.mgsx.dl13.DL13Game;
import net.mgsx.dl13.DL13Game.ScreenState;
import net.mgsx.dl13.assets.GameAssets;
import net.mgsx.dl13.store.GameStore;
import net.mgsx.dl13.ui.GameHUD;
import net.mgsx.dl13.utils.StageScreen;

public class TitleScreen extends StageScreen
{
	public TitleScreen(GameStore store) {
		super(new FitViewport(640, 480));
	
		Skin skin = GameAssets.i.skin;
		
		TextButton btPlay = new TextButton("New Game", skin);
		
		Table t = new Table(skin);
		t.defaults().pad(6);
		t.add(new Label("Multiverse\nRacer", skin, "title")).row();
		
		t.add(btPlay).row();
		
		Table recTable = new Table(skin);
		recTable.defaults().pad(4);
		
		String[] labels = {"1st", "2nd", "3rd"};
		for(int i=0 ; i<GameStore.MAX_RECORDS && i<labels.length ; i++){
			recTable.add(labels[i]);
			if(store.records.size <= i){
				recTable.add("--:--.--");
			}else{
				recTable.add(GameHUD.formatTime(store.records.get(i).score));
			}
			recTable.row();
		}
		
		t.add(recTable).row();
		
		stage.addActor(t);
		t.setFillParent(true);
		
		btPlay.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				DL13Game.toScreen(ScreenState.SELECT);
			}
		});
	}
	
	@Override
	public void render(float delta) {
		Gdx.gl.glClearColor(0, 0, 0, 0);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
		
		super.render(delta);
	}
}
