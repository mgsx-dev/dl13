package net.mgsx.dl13.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.ButtonGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.FitViewport;

import net.mgsx.dl13.DL13Game;
import net.mgsx.dl13.DL13Game.ScreenState;
import net.mgsx.dl13.assets.GameAssets;
import net.mgsx.dl13.assets.IBL;
import net.mgsx.dl13.model.GameModel;
import net.mgsx.dl13.store.GameStore;
import net.mgsx.dl13.utils.StageScreen;
import net.mgsx.gltf.scene3d.attributes.PBRCubemapAttribute;
import net.mgsx.gltf.scene3d.attributes.PBRFloatAttribute;
import net.mgsx.gltf.scene3d.attributes.PBRTextureAttribute;
import net.mgsx.gltf.scene3d.lights.DirectionalLightEx;
import net.mgsx.gltf.scene3d.lights.DirectionalShadowLight;
import net.mgsx.gltf.scene3d.scene.Scene;
import net.mgsx.gltf.scene3d.scene.SceneManager;
import net.mgsx.gltf.scene3d.scene.SceneSkybox;

public class SelectScreen extends StageScreen {
	private String[] carNames = {"Country Rider", "Badass Fury", "Dragon's Egg"};
	private SceneManager sceneManager;
	private PerspectiveCamera camera;
	private SceneSkybox skybox;
	private DirectionalLightEx sunLight;
	
	private Array<Scene> carModels = new Array<Scene>();
	private Scene carModel;
	private float time;
	private Actor actor;
	private ButtonGroup<Button> btGroup;
	
	public SelectScreen(GameStore store) {
		super(new FitViewport(DL13Game.UIWidth, DL13Game.UIHeight));
		
		Skin skin = GameAssets.i.skin;
		
		carModels.add(new Scene(GameAssets.i.carA.scene));
		carModels.add(new Scene(GameAssets.i.carB.scene));
		carModels.add(new Scene(GameAssets.i.carC.scene));
		
		Table t = new Table(skin);
		t.defaults().pad(10);
		t.add("Choose your car").colspan(3);
		t.row();
		
		btGroup = new ButtonGroup<Button>();
		for(int i=0 ; i<3 ; i++){
			TextButton btCar = new TextButton(carNames[i], skin, "toggle");
			t.add(btCar);
			btCar.setChecked(GameModel.preferredCarStyle.ordinal() == i);
			final int index = i;
			btCar.addListener(new ChangeListener() {
				@Override
				public void changed(ChangeEvent event, Actor actor) {
					if(btCar.isChecked()){
						changeCar(index);
					}
				}
			});
			btGroup.add(btCar);
		}
		t.row();
		TextButton btConfirm = new TextButton("Confirm", skin);
		t.add(btConfirm).colspan(3).row();
		btConfirm.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				DL13Game.toScreen(ScreenState.GAME);
			}
		});
		
		Table main = new Table(skin);
		main.add(t).expand().top();
		stage.addActor(main);
		main.setFillParent(true);
		
		// FAKE actor
		
		actor = new Actor();
		stage.addActor(actor);
		
		// SCENE
		
		sceneManager = new SceneManager(12);
		sceneManager.camera = camera = new PerspectiveCamera(60, 1, 1);
		camera.position.set(100, 100, 100);
		camera.up.set(Vector3.Y);
		camera.lookAt(Vector3.Zero);
		camera.near = .1f;
		camera.far = 1000f;
		
		IBL ibl = GameAssets.i.day;
		
		sceneManager.environment.set(PBRCubemapAttribute.createDiffuseEnv(ibl.diffuseCubemap));
		sceneManager.environment.set(PBRCubemapAttribute.createSpecularEnv(ibl.specularCubemap));
		// XXX sceneManager.setSkyBox(skybox = new SceneSkybox(ibl.environmentCubemap));
		sceneManager.environment.set(new PBRTextureAttribute(PBRTextureAttribute.BRDFLUTTexture, GameAssets.i.brdfLUT));
		
		sceneManager.environment.set(new PBRFloatAttribute(PBRFloatAttribute.ShadowBias, 0f));
		
		sceneManager.setAmbientLight(1f);
		
		boolean shadows = true;
		sunLight = shadows ? new DirectionalShadowLight() : new DirectionalLightEx();
		sunLight.direction.set(0,-1,0);
		sceneManager.environment.add(sunLight);
		
		carModel = carModels.get(GameModel.preferredCarStyle.ordinal());
		sceneManager.addScene(carModel);
	}
	
	@Override
	public void dispose() {
		sceneManager.dispose();
		super.dispose();
	}

	protected void changeCar(int index) {
		if(actor == null) return;
		
		GameModel.preferredCarStyle = GameModel.CarStyle.values()[index];
		// animate
		actor.addAction(Actions.sequence(
			Actions.moveTo(-12, 0, .5f, Interpolation.pow2),
			Actions.run(()->{
				actor.setX(12);
				sceneManager.removeScene(carModel);
				carModel = carModels.get(index);
				sceneManager.addScene(carModel);
			}),
			Actions.moveTo(0, 0, .5f, Interpolation.pow2)
		));
	}
	
	@Override
	public void render(float delta) {
		
		time += delta;
		
		carModel.modelInstance.transform.setToRotation(Vector3.Y, time * -45)
		.mulLeft(new Matrix4().setToTranslation(actor.getX(), .5f, 0));
		
		camera.viewportWidth = Gdx.graphics.getWidth();
		camera.viewportHeight = Gdx.graphics.getHeight();

		
		camera.position.set(0, 5, 6);
		camera.up.set(Vector3.Y);
		camera.lookAt(new Vector3(0, 2, 0));
		
		camera.update();
		
		sceneManager.update(delta);
		
		Gdx.gl.glClearColor(DL13Game.neutralColor.r, DL13Game.neutralColor.g, DL13Game.neutralColor.b, DL13Game.neutralColor.a);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
		sceneManager.render();
		
		super.render(delta);
	}
}
