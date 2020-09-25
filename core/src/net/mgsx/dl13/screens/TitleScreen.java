package net.mgsx.dl13.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.FitViewport;

import net.mgsx.dl13.DL13Game;
import net.mgsx.dl13.DL13Game.ScreenState;
import net.mgsx.dl13.assets.GameAssets;
import net.mgsx.dl13.assets.IBL;
import net.mgsx.dl13.store.GameStore;
import net.mgsx.dl13.ui.GameHUD;
import net.mgsx.dl13.utils.StageScreen;
import net.mgsx.gltf.scene3d.attributes.PBRCubemapAttribute;
import net.mgsx.gltf.scene3d.attributes.PBRFloatAttribute;
import net.mgsx.gltf.scene3d.attributes.PBRTextureAttribute;
import net.mgsx.gltf.scene3d.lights.DirectionalLightEx;
import net.mgsx.gltf.scene3d.lights.DirectionalShadowLight;
import net.mgsx.gltf.scene3d.scene.Scene;
import net.mgsx.gltf.scene3d.scene.SceneManager;
import net.mgsx.gltf.scene3d.scene.SceneSkybox;

public class TitleScreen extends StageScreen
{
	private SceneManager sceneManager;
	private PerspectiveCamera camera;
	private SceneSkybox skybox;
	private DirectionalLightEx sunLight;
	private float time;
	private Scene scene;
	private SpriteBatch batch;
	
	public TitleScreen(GameStore store) {
		super(new FitViewport(DL13Game.UIWidth, DL13Game.UIHeight));
		
		batch = new SpriteBatch();
	
		Skin skin = GameAssets.i.skin;
		
		TextButton btPlay = new TextButton("New Game", skin);
		
		Table t = new Table(skin);
		t.defaults().pad(6);
		t.add(new Label("Multiverse\nRacer", skin, "title")).padTop(100).getActor().setAlignment(Align.center);
		t.row();
		
		t.add(btPlay).expandY().bottom().row();
		
		Table recTable = new Table(skin);
		recTable.defaults().pad(4);
		
		String[] labels = {"1st", "2nd", "3rd"};
		for(int i=0 ; i<GameStore.MAX_RECORDS && i<labels.length ; i++){
			recTable.add(labels[i]).padRight(30).expandX().right().getActor().setColor(Color.LIGHT_GRAY);
			if(store.records.size <= i){
				recTable.add("-- -- --").getActor().setColor(Color.BLACK);
			}else{
				recTable.add(GameHUD.formatTime(store.records.get(i).score)).getActor().setColor(Color.YELLOW);
			}
			recTable.row();
		}
		
		t.add(recTable).padBottom(50).row();
		
		stage.addActor(t);
		t.setFillParent(true);
		
		btPlay.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				GameAssets.i.playUI();
				DL13Game.toScreen(ScreenState.SELECT);
			}
		});
		
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
		// sceneManager.setSkyBox(skybox = new SceneSkybox(ibl.environmentCubemap));
		sceneManager.environment.set(new PBRTextureAttribute(PBRTextureAttribute.BRDFLUTTexture, GameAssets.i.brdfLUT));
		
		sceneManager.environment.set(new PBRFloatAttribute(PBRFloatAttribute.ShadowBias, 0f));
		
		sceneManager.setAmbientLight(1f);
		
		boolean shadows = true;
		sunLight = shadows ? new DirectionalShadowLight(store.getShadowMapSize(), store.getShadowMapSize()) : new DirectionalLightEx();
		sunLight.direction.set(0,-1,0);
		sceneManager.environment.add(sunLight);
		
		sceneManager.addScene(scene = new Scene(GameAssets.i.world("E").scene));
	}
	
	@Override
	public void show() {
		GameAssets.i.playSongMenu();
		super.show();
	}
	
	@Override
	public void dispose() {
		sceneManager.dispose();
		super.dispose();
	}
	
	@Override
	public void render(float delta) {
		time += delta;
		
		sceneManager.setAmbientLight(.7f);
		if(sunLight instanceof DirectionalShadowLight){
			float s = 300;
			BoundingBox bbox = new BoundingBox(new Vector3(-s,-s,-s), new Vector3(s,s,s));
			((DirectionalShadowLight) sunLight).setBounds(bbox);
			sceneManager.environment.set(new PBRFloatAttribute(PBRFloatAttribute.ShadowBias, 1 / 50f));
		}
		sunLight.intensity = 3;
		sunLight.direction.set(2,-3,2).nor();
		
		scene.modelInstance.transform.idt().rotate(Vector3.Y, time * -10).translate(100, 0, 0);
		
		camera.viewportWidth = Gdx.graphics.getWidth();
		camera.viewportHeight = Gdx.graphics.getHeight();

		float d = 275;
		camera.position.set(d,d * 0.6f,d);
		camera.up.set(Vector3.Y);
		camera.lookAt(new Vector3(0, 2, 0));
		
		camera.update();
		
		sceneManager.update(delta);
		
		Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		Gdx.gl.glClearColor(DL13Game.skyColor.r, DL13Game.skyColor.g, DL13Game.skyColor.b, DL13Game.skyColor.a);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
		sceneManager.render();
		
		viewport.apply();
		super.render(delta);
		
		
		if(sunLight instanceof DirectionalShadowLight && false){
			Texture map = (Texture)((DirectionalShadowLight) sunLight).getDepthMap().texture;
			batch.getProjectionMatrix().setToOrtho2D(0, 0, 1, 1);
			batch.begin();
			batch.draw(map, 0, 0, 1, 1, 0, 0, 1, 1);
			batch.end();
		}
	}
}
