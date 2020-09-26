package net.mgsx.dl13.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Array;

import net.mgsx.dl13.DL13Game;
import net.mgsx.dl13.DL13Game.ScreenState;
import net.mgsx.dl13.store.GameStore;

public class SettingsUI extends Table {

	private SelectBox<Integer> shadowSelector;

	public SettingsUI(GameStore store, Skin skin) {
		super(skin);
		
		setBackground("default-rect");
		defaults().pad(20);
		
		TextButton btOK = new TextButton("OK", skin);
		TextButton btFullscreen = new TextButton("Fullscreen", skin, "toggle");
		btFullscreen.setChecked(Gdx.graphics.isFullscreen());
		
		shadowSelector = new SelectBox<Integer>(skin);
		Array<Integer> items = new Array<Integer>();
		for(int s : GameStore.SHADOW_SIZES) items.add(s);
		shadowSelector.setItems(items);
		shadowSelector.setSelectedIndex(store.shadowQuality);
		
		add("Shadow map size");
		add(shadowSelector).row();
		
		add("Display");
		add(btFullscreen).row();
		
		add("Press F key to toggle fullscreen").colspan(2).getActor().setColor(Color.GRAY);
		row();
		
		add(btOK).colspan(2).row();
		
		shadowSelector.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				store.shadowQuality = shadowSelector.getSelectedIndex();
			}
		});
		
		btOK.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				DL13Game.save();
				DL13Game.toScreen(ScreenState.TITLE);
			}
		});
		
		btFullscreen.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				if(Gdx.graphics.isFullscreen()){
					Gdx.graphics.setWindowedMode(640, 480);
				}else{
					Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());
				}
			}
		});
	}

	
}
