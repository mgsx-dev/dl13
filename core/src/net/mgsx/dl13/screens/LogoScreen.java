package net.mgsx.dl13.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g3d.shaders.DepthShader;
import com.badlogic.gdx.graphics.g3d.shaders.DepthShader.Config;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.viewport.FitViewport;

import net.mgsx.dl13.DL13Game;
import net.mgsx.dl13.DL13Game.ScreenState;
import net.mgsx.dl13.assets.GameAssets;
import net.mgsx.dl13.assets.IBL;
import net.mgsx.dl13.store.GameStore;
import net.mgsx.dl13.utils.StageScreen;
import net.mgsx.gltf.scene3d.animation.AnimationsPlayer;
import net.mgsx.gltf.scene3d.attributes.PBRCubemapAttribute;
import net.mgsx.gltf.scene3d.attributes.PBRFloatAttribute;
import net.mgsx.gltf.scene3d.attributes.PBRTextureAttribute;
import net.mgsx.gltf.scene3d.lights.DirectionalLightEx;
import net.mgsx.gltf.scene3d.lights.DirectionalShadowLight;
import net.mgsx.gltf.scene3d.scene.Scene;
import net.mgsx.gltf.scene3d.scene.SceneManager;
import net.mgsx.gltf.scene3d.shaders.PBRDepthShaderProvider;
import net.mgsx.gltf.scene3d.shaders.PBRShaderConfig;
import net.mgsx.gltf.scene3d.shaders.PBRShaderConfig.SRGB;
import net.mgsx.gltf.scene3d.shaders.PBRShaderProvider;

public class LogoScreen extends StageScreen
{
	private SceneManager sceneManager;
	private PerspectiveCamera camera;
	private DirectionalLightEx sunLight;
	private float time;
	private Scene scene;
	private AnimationsPlayer player;
	private boolean animate;
	private int lightEffect = 0;
	private float lightEffectSmooth;
	
	public LogoScreen(GameStore store) {
		super(new FitViewport(DL13Game.UIWidth, DL13Game.UIHeight));
		
		Skin skin = GameAssets.i.skin;
		
		// SCENE
		
		scene = new Scene(GameAssets.i.badlog.scene);
		
		PBRShaderConfig config = PBRShaderProvider.createDefaultConfig();
		config.fragmentShader = Gdx.files.classpath("gdx-pbr.fs.glsl").readString();
		config.numBones = 24;
		config.numSpotLights = 0;
		config.numPointLights = 0;
		config.numDirectionalLights = 1;
		config.manualSRGB = SRGB.FAST;
		config.useTangentSpace = false;
		
		
		Config depthConfig = new DepthShader.Config();
		depthConfig.numBones = 24;
		
		sceneManager = new SceneManager(PBRShaderProvider.createDefault(config), new PBRDepthShaderProvider(depthConfig));
		
		sceneManager.camera = camera = (PerspectiveCamera)scene.getCamera("Camera_Orientation");
		camera.near = .01f;
		camera.far = 100f;
		
		IBL ibl = GameAssets.i.day;
		
		sceneManager.environment.set(PBRCubemapAttribute.createDiffuseEnv(ibl.diffuseCubemap));
		sceneManager.environment.set(PBRCubemapAttribute.createSpecularEnv(ibl.specularCubemap));
		// sceneManager.setSkyBox(skybox = new SceneSkybox(ibl.environmentCubemap));
		sceneManager.environment.set(new PBRTextureAttribute(PBRTextureAttribute.BRDFLUTTexture, GameAssets.i.brdfLUT));
		
		sceneManager.environment.set(new PBRFloatAttribute(PBRFloatAttribute.ShadowBias, 0f));
		
		sceneManager.setAmbientLight(1f);
		
		boolean shadows = false;
		sunLight = shadows ? new DirectionalShadowLight() : new DirectionalLightEx();
		sunLight.direction.set(0,-1,0);
		sceneManager.environment.add(sunLight);
		
		sceneManager.addScene(scene);
		
		player = new AnimationsPlayer(scene);
		player.playAll();
		player.update(0);
		
		float preTime = .1f;
		float kingFrame = 64;
		float cointTime = kingFrame / 24f;
		float lastFrame = 120;
		float lasTime = lastFrame / 24f;
		float extraTime = 1f;
		
		float lightEffectTime = .5f;
		
		stage.addAction(Actions.delay(preTime, Actions.run(()->runAnims())));
		stage.addAction(Actions.delay(cointTime + preTime, Actions.run(()->{GameAssets.i.sndCoin.play(.5f);})));
		stage.addAction(Actions.delay(cointTime + preTime + lightEffectTime, Actions.run(()->{lightEffect = 1;})));
		stage.addAction(Actions.delay(lasTime + extraTime + preTime, Actions.run(()->DL13Game.toScreen(ScreenState.TITLE))));
	}
	
	private void runAnims() {
		animate = true;
	}

	@Override
	public void show() {
		GameAssets.i.stopSong();
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
		
		if(animate) player.update(delta);
		
		lightEffectSmooth = MathUtils.lerp(lightEffectSmooth, lightEffect, delta * 5);
		
		sceneManager.setAmbientLight(MathUtils.lerp(.5f, 0, lightEffectSmooth));
		if(sunLight instanceof DirectionalShadowLight){
			float s = 300;
			BoundingBox bbox = new BoundingBox(new Vector3(-s,-s,-s), new Vector3(s,s,s));
			// ((DirectionalShadowLight) sunLight).setViewport(30, 30, 1f, 100f);
			// ((DirectionalShadowLight) sunLight).setCenter(Vector3.Zero);
			
			((DirectionalShadowLight) sunLight).setBounds(bbox);
			/*
			int shadowMapSize = 2048;
			((DirectionalShadowLight) sunLight).setShadowMapSize(shadowMapSize, shadowMapSize);
			
			((DirectionalShadowLight) sunLight).setCenter(Vector3.Zero);
			*/
			sceneManager.environment.set(new PBRFloatAttribute(PBRFloatAttribute.ShadowBias, 1 / 50f));
		}
		sunLight.intensity = MathUtils.lerp(3, 0, lightEffectSmooth);
		sunLight.direction.set(-2,-3,-1).nor();
		
		camera.viewportWidth = Gdx.graphics.getWidth();
		camera.viewportHeight = Gdx.graphics.getHeight();
		camera.update();
		
		sceneManager.update(delta);
		
		float l = MathUtils.lerp(.1f, 1f, lightEffectSmooth);
		Gdx.gl.glClearColor(l,l,l,0);
		Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
		sceneManager.render();
		
		viewport.apply();
		super.render(delta);
	}
}
