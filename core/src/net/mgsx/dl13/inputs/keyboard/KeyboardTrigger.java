package net.mgsx.dl13.inputs.keyboard;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;

import net.mgsx.dl13.inputs.TriggerBase;

public class KeyboardTrigger extends TriggerBase<KeyboardController>
{
	private int key;

	public KeyboardTrigger(int key) {
		this.key = key;
	}

	@Override
	public String toString() {
		return Input.Keys.toString(key);
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof KeyboardTrigger){
			return ((KeyboardTrigger) obj).key == key;
		}
		return super.equals(obj);
	}
	
	@Override
	public int hashCode() {
		return key;
	}

	@Override
	public boolean isOn(KeyboardController controller) {
		return Gdx.input.isKeyPressed(key);
	}

	@Override
	public String format() {
		return "KEY|" + key;
	}

	@Override
	public float getAnalog(KeyboardController controller) {
		return isOn(controller) ? 1 : 0;
	}
}
