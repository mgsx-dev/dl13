package net.mgsx.dl13.inputs;

public abstract class TriggerBase<T extends ControllerBase> {

	public abstract boolean isOn(T controller);

	public abstract String format();

	public abstract float getAnalog(T controller);
}
