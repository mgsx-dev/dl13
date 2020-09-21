package net.mgsx.dl13.model;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.utils.FloatArray;

import net.mgsx.dl13.assets.GameAssets;
import net.mgsx.dl13.assets.IBL;
import net.mgsx.dl13.navmesh.MeshIndexer;
import net.mgsx.dl13.navmesh.NavMesh;
import net.mgsx.gltf.scene3d.attributes.PBRCubemapAttribute;
import net.mgsx.gltf.scene3d.attributes.PBRFloatAttribute;
import net.mgsx.gltf.scene3d.attributes.PBRTextureAttribute;
import net.mgsx.gltf.scene3d.lights.DirectionalLightEx;
import net.mgsx.gltf.scene3d.lights.DirectionalShadowLight;
import net.mgsx.gltf.scene3d.scene.Scene;
import net.mgsx.gltf.scene3d.scene.SceneManager;
import net.mgsx.gltf.scene3d.scene.SceneSkybox;

public class GameWorld {
	
	private SceneManager sceneManager;
	private PerspectiveCamera camera;
	private Scene worldScene;
	private SceneSkybox skybox;
	private DirectionalLightEx sunLight;
	private NavMesh navMesh;
	private Car player = new Car();
	private Vector3 smoothPos = new Vector3();
	private static final Vector3 vec1 = new Vector3();
	
	public GameWorld() {
		sceneManager = new SceneManager(12);
		sceneManager.camera = camera = new PerspectiveCamera(60, 1, 1);
		camera.position.set(100, 100, 100);
		camera.up.set(Vector3.Y);
		camera.lookAt(Vector3.Zero);
		camera.near = .1f;
		camera.far = 1000f;
		worldScene = new Scene(GameAssets.i.worldA.scene);
		sceneManager.addScene(worldScene);
		
		// ENV
		IBL ibl = GameAssets.i.moonlessGolf;
		
		sceneManager.environment.set(PBRCubemapAttribute.createDiffuseEnv(ibl.diffuseCubemap));
		sceneManager.environment.set(PBRCubemapAttribute.createSpecularEnv(ibl.specularCubemap));
		sceneManager.setSkyBox(skybox = new SceneSkybox(ibl.environmentCubemap));
		sceneManager.environment.set(new PBRTextureAttribute(PBRTextureAttribute.BRDFLUTTexture, GameAssets.i.brdfLUT));
		
		sceneManager.environment.set(new PBRFloatAttribute(PBRFloatAttribute.ShadowBias, 0f));
		
		sceneManager.setAmbientLight(1f);
		
		boolean shadows = true;
		sunLight = shadows ? new DirectionalShadowLight() : new DirectionalLightEx();
		sunLight.direction.set(0,-1,0);
		sceneManager.environment.add(sunLight);
		
		sceneManager.setSkyBox(skybox);
		
		// NAVMESH
		MeshIndexer indexer = new MeshIndexer();
		FloatArray points = indexer.extractVertices(worldScene.modelInstance.getNode("landA"));
		navMesh = indexer.build(points, MathUtils.FLOAT_ROUNDING_ERROR);
		
		// STARTUP POINT
		player.space = navMesh.rayCast(new Ray(new Vector3(0, 1000, 0), new Vector3(0,-1,0)));
		player.direction.set(player.space.normal).crs(player.space.triangle.edgeA.vector).nor(); // TODO fix direction at start ?
		
		smoothPos.set(camera.position);
		
		player.scene = new Scene(GameAssets.i.carA.scene);
		sceneManager.addScene(player.scene);
	}
	
	public void update(float delta){
		if(player.space != null){
			
			camera.direction.slerp(player.direction, MathUtils.clamp(delta * 10, 0, 1));
			camera.up.slerp(player.space.normal, MathUtils.clamp(delta * 10, 0, 1));
			
			float cameraForce = 1;
			
			vec1.set(player.space.position);
			smoothPos.lerp(vec1, delta * 3f * cameraForce);
			float distance = 100.5f * 1;
			camera.position.set(smoothPos).mulAdd(camera.up, distance * .055f).mulAdd(camera.direction, distance * -0.1f);
		}
		camera.fieldOfView = 90;
		camera.near = .01f;
		camera.update();
		player.updateAsPlayer(navMesh, delta);
		sceneManager.update(delta);
	}
	
	public void render(){
		if(sunLight instanceof DirectionalShadowLight){
			float s = 30;
			BoundingBox bbox = new BoundingBox(new Vector3(-s,-s,-s), new Vector3(s,s,s));
			// ((DirectionalShadowLight) sunLight).setViewport(30, 30, 1f, 100f);
			// ((DirectionalShadowLight) sunLight).setCenter(Vector3.Zero);
			((DirectionalShadowLight) sunLight).setBounds(bbox);
			int shadowMapSize = 2048;
			((DirectionalShadowLight) sunLight).setShadowMapSize(shadowMapSize, shadowMapSize);
			
			((DirectionalShadowLight) sunLight).setCenter(camera.position);
		}
		sunLight.direction.set(0,-1,0);
		sunLight.baseColor.set(Color.RED);
		sunLight.intensity = 10;
		sunLight.updateColor();
		
		// XXX 
		sceneManager.environment.set(new PBRFloatAttribute(PBRFloatAttribute.ShadowBias, 1.0f / 255f));
		sceneManager.setAmbientLight(.1f);
		
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
		sceneManager.render();
	}
}
