package net.mgsx.dl13.store;

import com.badlogic.gdx.utils.Array;

public class GameStore {
	public static final int MAX_RECORDS = 10;
	
	public Array<GameRecord> records = new Array<GameRecord>();
	
	public int shadowQuality = 2;
	public static final int [] SHADOW_SIZES = {0, 1024, 2048, 4096};
	
	public int getShadowMapSize(){
		return SHADOW_SIZES[shadowQuality];
	}
	
	public void compress() {
		records.truncate(MAX_RECORDS);
		
	}

	public void compute() {
		for(GameRecord r : records) r.validate();
	}
}
