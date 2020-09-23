package net.mgsx.dl13.store;

public class GameRecord {
	public float time;
	public int bonus;
	public transient float score;
	
	public void validate(){
		score = time - bonus;
	}
}
