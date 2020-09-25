package net.mgsx.dl13.model;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.Preferences;

import net.mgsx.dl13.inputs.InputManager;

public class GameInputs extends InputManager
{
	public static enum PlayerCommand {
		LEFT, RIGHT, ACCEL, BRAKE
	}
	
	public GameInputs(Preferences prefs) {
		super(prefs);
	}
	
	@Override
	protected void setDefault() {
		super.setDefault();
		
		addCommand(PlayerCommand.LEFT, "left", "Turn left");
		addCommand(PlayerCommand.RIGHT, "right", "Turn right");
		addCommand(PlayerCommand.ACCEL, "accel", "Accelerate");
		addCommand(PlayerCommand.BRAKE, "brake", "Brake");
		
		
		// default keys
		addKeys(PlayerCommand.LEFT, Input.Keys.LEFT, Input.Keys.Q, Input.Keys.A);
		addKeys(PlayerCommand.RIGHT, Input.Keys.RIGHT, Input.Keys.D);
		addKeys(PlayerCommand.ACCEL, Input.Keys.UP, Input.Keys.Z, Input.Keys.W);
		addKeys(PlayerCommand.BRAKE, Input.Keys.DOWN, Input.Keys.S);
	}

	/*
	public void update(Player player) 
	{
		if(controller.isOn(PlayerCommand.LEFT)){
			player.velocityTarget.x = -1;
		}else if(controller.isOn(PlayerCommand.RIGHT)){
			player.velocityTarget.x = 1;
		}else{
			player.velocityTarget.x = 0;
		}
		
		if(controller.isOn(PlayerCommand.JUMP)){
			player.jumpOn();
		}else{
			player.jumpOff();
		}
		if(controller.isOn(PlayerCommand.DOWN)){
			player.down = true;
		}else{
			player.down = false;
		}
	}
	*/

}
