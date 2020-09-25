package net.mgsx.dl13.ui;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;

import net.mgsx.dl13.DL13Game;
import net.mgsx.dl13.DL13Game.ScreenState;
import net.mgsx.dl13.assets.GameAssets;
import net.mgsx.dl13.model.GameModel;
import net.mgsx.dl13.store.GameRecord;

public class GameHUD extends Table
{
	private GameModel game;
	private Label timeCounter;
	private Label speedCounter;
	private Label bonusCounter;

	public GameHUD(GameModel game, Skin skin) {
		super(skin);
		this.game = game;
		
		timeCounter = new Label("00:00.000", skin);
		speedCounter = new Label("88.00 km/h", skin);
		bonusCounter = new Label("-13s", skin);
		
		Table t = new Table();
		t.add(speedCounter).width(130);
		t.add(timeCounter).expandX().width(130);  // TODO use monospace font instead
		t.add(bonusCounter).width(250);
		
		add(t).expandY().top().growX();
	}
	
	public void launchCountDown(){
		addAction(Actions.sequence(
			launchWord("GET READY!", 1f, false),
			Actions.delay(2),
			launchWord("3", 0, true),
			Actions.delay(1),
			launchWord("2", 0, true),
			Actions.delay(1),
			launchWord("1", 0, true),
			Actions.delay(1),
			launchWord("GO!", 1, false),
			Actions.run(()->{
				game.running = true;
				GameAssets.i.playSongGame();
			})));
	}
	
	private Action launchWord(String word, float holdTime, boolean sfx) {
		return Actions.run(()->spawnWord(word, holdTime, sfx));
	}

	private void spawnWord(String word, float holdTime, boolean sfx) {
		final Label label = new Label(word, getSkin());
		label.pack();
		getStage().addActor(label);
		label.setPosition(getStage().getWidth()/2, getStage().getHeight()/2, Align.center);
		label.setOrigin(Align.center);
		
		if(sfx) GameAssets.i.bonusSoundSoft.play(.5f);
		
		label.addAction(Actions.sequence(
			Actions.delay(holdTime),
			Actions.alpha(0, 1f),
			Actions.removeActor()));
	}

	@Override
	public void act(float delta) {
		speedCounter.setText(formatSpeed(game.speed));
		timeCounter.setText(formatTime(game.time));
		bonusCounter.setText(formatBonus(game.bonus));
		super.act(delta);
	}
	
	private String formatBonus(int value){
		if(value == 0){
			return "No bonus";
		}else{
			return value + "s";
		}
	}
	
	private String formatSpeed(float speed){
		// Rescale to fake value : max is 0.6
		float fakeSpeed = speed * 88.0f / 0.6f;
		
		int unit = MathUtils.floor(fakeSpeed);
		int dec = MathUtils.floor((fakeSpeed % 1f) * 100);
		
		String sUnit = unit < 10 ? "0" + unit : String.valueOf(unit); 
		String sDec = dec < 10 ? "0" + dec : String.valueOf(dec); 
		
		return sUnit + "." + sDec + " mph";
	}

	public void spawnFinish() 
	{
		GameRecord currentRecord = game.record;
		Table t = new Table(getSkin());
		
		t.setBackground("default-rect");
		t.defaults().pad(10);
		
		Array<Actor> actors = new Array<Actor>();
		
		actors.add(t.add("FINISHED!").colspan(2).expandX().getActor());
		t.row();
		
		actors.add(t.add("Raw time:").getActor());
		actors.add(t.add(formatTime(currentRecord.time)).getActor());
		t.row();
		
		actors.add(t.add("Bonus:").getActor());
		actors.add(t.add(formatBonus(currentRecord.bonus)).getActor());
		t.row();
		
		actors.add(t.add("Final time:").getActor());
		actors.add(t.add(formatTime(currentRecord.score)).getActor());
		t.row();
		
		if(game.isNewRecord){
			actors.add(t.add("NEW RECORD!").colspan(2).expandX().getActor());
			t.row();
		}else{
			actors.add(t.add("Best:").getActor());
			actors.add(t.add(formatTime(game.store.records.first().score)).getActor());
			t.row();
		}
		{
			TextButton btOK = new TextButton("Try again", getSkin());
			actors.add(t.add(btOK).colspan(2).expandX().getActor());
			t.row();
			btOK.addListener(new ChangeListener() {
				@Override
				public void changed(ChangeEvent event, Actor actor) {
					DL13Game.toScreen(ScreenState.GAME);
				}
			});
		}
		{
			TextButton btOK = new TextButton("Change car", getSkin());
			actors.add(t.add(btOK).colspan(2).expandX().getActor());
			t.row();
			btOK.addListener(new ChangeListener() {
				@Override
				public void changed(ChangeEvent event, Actor actor) {
					DL13Game.toScreen(ScreenState.SELECT);
				}
			});
		}
		{
			TextButton btOK = new TextButton("Title screen", getSkin());
			actors.add(t.add(btOK).colspan(2).expandX().getActor());
			t.row();
			btOK.addListener(new ChangeListener() {
				@Override
				public void changed(ChangeEvent event, Actor actor) {
					DL13Game.toScreen(ScreenState.TITLE);
				}
			});
		}
		
		
		t.pack();
		t.setPosition(getStage().getWidth()/2, getStage().getHeight()/2, Align.center);
		
		getStage().addActor(t);
		
		for(int i=0 ; i<actors.size ; i++){
			Actor actor = actors.get(i);
			actor.getColor().a = 0;
			actor.addAction(Actions.delay(i * .5f, Actions.alpha(1, .2f)));
		}
	}

	public static String formatTime(float time) {
		int min = MathUtils.floor(time / 60);
		time -= min * 60;
		int sec = MathUtils.floor(time);
		time -= sec;
		int ms = MathUtils.floor(time * 100);
		
		String text;
		if(min >= 60){
			text = "59:59.999";
		}
		else{
			String sMin = min < 10 ? "0" + min : String.valueOf(min);
			String sSec = sec < 10 ? "0" + sec : String.valueOf(sec);
			String sMs = ms < 10 ? "0" + ms : String.valueOf(ms);
			text = sMin + ":" + sSec + "." + sMs;
		}
		return text;
	}
	
}
