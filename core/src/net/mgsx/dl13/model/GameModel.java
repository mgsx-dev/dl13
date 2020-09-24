package net.mgsx.dl13.model;

import java.util.Comparator;

import net.mgsx.dl13.assets.GameAssets;
import net.mgsx.dl13.store.GameRecord;
import net.mgsx.dl13.store.GameStore;
import net.mgsx.gltf.scene3d.scene.SceneModel;

public class GameModel {
	
	public static enum CarStyle{
		A,B,C
	}
	
	public static CarStyle preferredCarStyle = CarStyle.A;
	
	public final GameStore store;
	public float time;
	public int bonus;
	public boolean running;
	public float speed;
	public boolean finishLine;
	public boolean isNewRecord;
	public GameRecord record;
	
	public GameModel(GameStore store) {
		super();
		this.store = store;
	}
	
	public void newRecord() {
		record = new GameRecord();
		record.time = time;
		record.bonus = bonus;
		record.validate();
		store.records.add(record);
		store.records.sort(new Comparator<GameRecord>() {
			@Override
			public int compare(GameRecord o1, GameRecord o2) {
				return Float.compare(o1.score, o2.score);
			}
		});
		isNewRecord = record == store.records.first();
	}

	public SceneModel getCarModel() {
		switch(preferredCarStyle){
		default:
		case A:
			return GameAssets.i.carA.scene;
		case B:
			return GameAssets.i.carB.scene;
		case C:
			return GameAssets.i.carC.scene;
		}
	}

}
