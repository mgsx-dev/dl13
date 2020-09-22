package net.mgsx.dl13.model;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.FloatArray;
import com.badlogic.gdx.utils.ObjectMap;

import net.mgsx.dl13.DL13Game;
import net.mgsx.dl13.assets.GameAssets;
import net.mgsx.dl13.assets.IBL;
import net.mgsx.dl13.navmesh.MeshIndexer;
import net.mgsx.dl13.navmesh.NavMesh;
import net.mgsx.dl13.navmesh.NavMesh.Triangle;
import net.mgsx.gltf.scene3d.attributes.PBRCubemapAttribute;
import net.mgsx.gltf.scene3d.attributes.PBRFloatAttribute;
import net.mgsx.gltf.scene3d.attributes.PBRTextureAttribute;
import net.mgsx.gltf.scene3d.lights.DirectionalLightEx;
import net.mgsx.gltf.scene3d.lights.DirectionalShadowLight;
import net.mgsx.gltf.scene3d.model.ModelInstanceHack;
import net.mgsx.gltf.scene3d.scene.Scene;
import net.mgsx.gltf.scene3d.scene.SceneAsset;
import net.mgsx.gltf.scene3d.scene.SceneManager;
import net.mgsx.gltf.scene3d.scene.SceneSkybox;

public class GameWorld {
	
	private final int nbBonus = 0; // XXX 100;
	
	private SceneManager sceneManager;
	private PerspectiveCamera camera;
	private Scene worldScene;
	private SceneSkybox skybox;
	private DirectionalLightEx sunLight;
	private NavMesh navMesh;
	public final Car player = new Car();
	private Vector3 smoothPos = new Vector3();
	private final Array<Bonus> bonusList = new Array<Bonus>();
	private float time;
	private final Array<Bonus> bonusHitList = new Array<Bonus>();
	private final Array<WrapDoor> doorList = new Array<WrapDoor>();
	public WrapDoor currentDoor = null;
	private ObjectMap<String, Node> axisMap = new ObjectMap<String, Node>();
	private float fovTarget = 90;
	
	public GameWorld(String worldID) {
		SceneAsset worldAsset = GameAssets.i.world(worldID);
		String navMeshName = "navMesh" + worldID;
		
		
		sceneManager = new SceneManager(12);
		sceneManager.camera = camera = new PerspectiveCamera(60, 1, 1);
		camera.position.set(100, 100, 100);
		camera.up.set(Vector3.Y);
		camera.lookAt(Vector3.Zero);
		camera.near = .1f;
		camera.far = 1000f;
		worldScene = new Scene(worldAsset.scene);
		
		// extract empties
		for(int i=0 ; i<worldScene.modelInstance.nodes.size ; ){
			Node node = worldScene.modelInstance.nodes.get(i);
			if(node.id.startsWith("sphere.")){
				String name = node.id.split("\\.")[1];
				worldScene.modelInstance.nodes.removeIndex(i);
				WrapDoor door = new WrapDoor();
				door.name = name;
				door.position.set(node.translation);
				door.radius = node.scale.x;
				doorList.add(door);
			}else if(node.id.startsWith("axis.")){
				String name = node.id.split("\\.")[1];
				worldScene.modelInstance.nodes.removeIndex(i);
				axisMap.put(name, node);
			}else{
				i++;
			}
		}
		
		
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
		FloatArray points = indexer.extractVertices(worldScene.modelInstance.getNode(navMeshName));
		navMesh = indexer.build(points, MathUtils.FLOAT_ROUNDING_ERROR);
		
		// STARTUP POINT
		player.space = navMesh.rayCast(new Ray(new Vector3(0, 1000, 0), new Vector3(0,-1,0)));
		player.direction.set(player.space.normal).crs(player.space.triangle.edgeA.vector).nor(); // TODO fix direction at start ?
		
		resetCamera();
		
		player.scene = new Scene(GameAssets.i.carA.scene);
		sceneManager.addScene(player.scene);
		
		Array<Triangle> rndTriangle = new Array<Triangle>(navMesh.triangles);
		for(int i=0 ; i<nbBonus && rndTriangle.size > 0 ; i++){
			Bonus bonus = new Bonus();
			bonus.model = new ModelInstanceHack(GameAssets.i.bonus.scene.model, "bonusRed");
			int index = MathUtils.random(rndTriangle.size-1);
			rndTriangle.swap(index, rndTriangle.size-1);
			Triangle triangle = rndTriangle.pop();
			bonus.position.set(triangle.center).mulAdd(triangle.normal, 1);
			bonus.model.transform.setToTranslation(bonus.position);
			bonusList.add(bonus);
			sceneManager.getRenderableProviders().add(bonus.model);
		}
	}
	
	private final Vector3 camDirectionTarget = new Vector3();
	private final Vector3 camUpTarget = new Vector3();
	
	private void updateCameraTarget(){
		camDirectionTarget.set(player.direction);
		camUpTarget.set(player.space.normal);
	}
	
	private void applyCameraPosition(){
		float distance = 100.5f * 1;
		camera.position.set(smoothPos).mulAdd(camera.up, distance * .055f).mulAdd(camera.direction, distance * -0.1f);
	}
	
	public void update(float delta){
		
		if(DL13Game.debug){
			if(Gdx.input.isKeyJustPressed(Input.Keys.F)){
				camera.fieldOfView = 170;
			}
		}
		
		for(WrapDoor door : doorList){
			if(player.space != null){
				if(door.position.dst2(player.space.position) < door.radius*door.radius){
					currentDoor = door;
				}
			}
		}
		
		time += delta;
		
		if(player.space != null){
			updateCameraTarget();
			
			camera.direction.slerp(camDirectionTarget, MathUtils.clamp(delta * 10, 0, 1));
			camera.up.slerp(camUpTarget, MathUtils.clamp(delta * 10, 0, 1));
			
			float cameraForce = 1;
			
			smoothPos.lerp(player.space.position, delta * 3f * cameraForce);
			applyCameraPosition();
		}
		
		for(Bonus bonus : bonusList){
			float t = MathUtils.sin(time * 10) * .5f + .5f;
			bonus.model.transform.setToTranslation(bonus.position).scl(MathUtils.lerp(1f, 4f, t));
			
			if(player.space != null){
				if(bonus.position.dst2(player.space.position) < 10){
					bonusHitList.add(bonus);
				}
			}
		}
		for(Bonus bonus : bonusHitList){
			bonusList.removeValue(bonus, true);
			sceneManager.getRenderableProviders().removeValue(bonus.model, true);
		}
		bonusHitList.clear();
		
		camera.viewportWidth = Gdx.graphics.getWidth();
		camera.viewportHeight = Gdx.graphics.getHeight();
		
		camera.fieldOfView = MathUtils.lerp(camera.fieldOfView, fovTarget, delta * .5f);
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
		sunLight.baseColor.set(Color.WHITE);
		sunLight.intensity = 1;
		sunLight.updateColor();
		
		// XXX 
		sceneManager.environment.set(new PBRFloatAttribute(PBRFloatAttribute.ShadowBias, 1.0f / 255f));
		sceneManager.setAmbientLight(.9f);
		
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
		sceneManager.render();
	}

	public void resetPlayer(String axisID) {
		Node axis = axisMap.get(axisID);
		axis.calculateWorldTransform();
		
		Ray ray = new Ray();
		axis.globalTransform.getTranslation(ray.origin);
		ray.direction.set(Vector3.Y).rot(axis.globalTransform);
		
		player.space = navMesh.rayCast(ray);
		
		Vector3 tan = new Vector3(Vector3.Z);
		tan.rot(axis.globalTransform);
		player.direction.set(player.space.normal).crs(tan).scl(-1).nor();

		resetCamera();
		
		camera.fieldOfView = 180;
		fovTarget = 90;
	}
	
	private void resetCamera() {
		updateCameraTarget();
		camera.direction.set(camDirectionTarget);
		camera.up.set(camUpTarget);
		smoothPos.set(player.space.position);
		applyCameraPosition();
		camera.update();
	}
}
