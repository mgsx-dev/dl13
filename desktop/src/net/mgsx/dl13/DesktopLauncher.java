package net.mgsx.dl13;

import java.awt.SplashScreen;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.foregroundFPS = 60;
		
		new LwjglApplication(new DL13Game(){
			@Override
			public void create() {
				SplashScreen splashScreen = SplashScreen.getSplashScreen();
				if(splashScreen != null){
					splashScreen.close();
				}
				super.create();
			}
		}, config);
	}
}
