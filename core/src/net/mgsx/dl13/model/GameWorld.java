package net.mgsx.dl13.model;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.graphics.g3d.shaders.DepthShader;
import com.badlogic.gdx.graphics.g3d.shaders.DepthShader.Config;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
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
import net.mgsx.gltf.scene3d.shaders.PBRDepthShaderProvider;
import net.mgsx.gltf.scene3d.shaders.PBRShaderConfig;
import net.mgsx.gltf.scene3d.shaders.PBRShaderConfig.SRGB;
import net.mgsx.gltf.scene3d.shaders.PBRShaderProvider;

public class GameWorld implements Disposable {
	
	private final int nbBonus = 0; // XXX 100;
	
	private SceneManager sceneManager;
	private PerspectiveCamera camera;
	private Scene worldScene;
	private SceneSkybox skybox;
	private DirectionalLightEx sunLight;
	private NavMesh navMesh;
	public final Car player;
	private Vector3 smoothPos = new Vector3();
	private final Array<Bonus> bonusList = new Array<Bonus>();
	private float time;
	private final Array<Bonus> bonusHitList = new Array<Bonus>();
	private final Array<WrapDoor> doorList = new Array<WrapDoor>();
	public WrapDoor currentDoor = null;
	private ObjectMap<String, Node> axisMap = new ObjectMap<String, Node>();
	private float fovTarget;

	private GameModel game;
	
	public GameWorld(GameModel game, String worldID) {
		SceneAsset worldAsset = GameAssets.i.world(worldID);
		String navMeshName = "navMesh" + worldID;
		
		this.game = game;
		player = new Car(game);
		
		PBRShaderConfig config = PBRShaderProvider.createDefaultConfig();
		config.fragmentShader = Gdx.files.classpath("gdx-pbr.fs.glsl").readString();
		config.numBones = 0;
		config.numSpotLights = 0;
		config.numPointLights = 0;
		config.numDirectionalLights = 1;
		config.manualSRGB = SRGB.FAST;
		config.useTangentSpace = false;
		
		
		Config depthConfig = new DepthShader.Config();
		depthConfig.defaultCullFace = 0;
		depthConfig.numBones = 0;
		
		sceneManager = new SceneManager(PBRShaderProvider.createDefault(config), new PBRDepthShaderProvider(depthConfig));
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
			}else if(node.id.startsWith("bonus.")){
				worldScene.modelInstance.nodes.removeIndex(i);
				Bonus bonus = new Bonus();
				bonus.model = new ModelInstanceHack(GameAssets.i.bonus.scene.model, "bonusRed");
				bonus.position.set(node.translation);
				bonus.model.transform.setToTranslation(bonus.position);
				bonusList.add(bonus);
				sceneManager.getRenderableProviders().add(bonus.model);
			}else{
				i++;
			}
		}
		
		
		sceneManager.addScene(worldScene);
		
		// ENV
		IBL ibl = worldID.equals("E") ? GameAssets.i.day : (worldID.equals("C") ? GameAssets.i.interior : GameAssets.i.night);
		
		sceneManager.environment.set(PBRCubemapAttribute.createDiffuseEnv(ibl.diffuseCubemap));
		sceneManager.environment.set(PBRCubemapAttribute.createSpecularEnv(ibl.specularCubemap));
		sceneManager.setSkyBox(skybox = new SceneSkybox(ibl.environmentCubemap));
		sceneManager.environment.set(new PBRTextureAttribute(PBRTextureAttribute.BRDFLUTTexture, GameAssets.i.brdfLUT));
		
		sceneManager.environment.set(new PBRFloatAttribute(PBRFloatAttribute.ShadowBias, 0f));
		
		sceneManager.setAmbientLight(1f);
		
		boolean shadows = true;
		sunLight = shadows ? new DirectionalShadowLight(game.store.getShadowMapSize(), game.store.getShadowMapSize()) : new DirectionalLightEx();
		sunLight.direction.set(0,-1,0);
		sceneManager.environment.add(sunLight);
		
		
		// NAVMESH
		MeshIndexer indexer = new MeshIndexer();
		Node navMeshNode = worldScene.modelInstance.getNode(navMeshName);
		FloatArray points = indexer.extractVertices(navMeshNode);
		navMesh = indexer.build(points, MathUtils.FLOAT_ROUNDING_ERROR);
		
		if(worldID.equals("E")){
			worldScene.modelInstance.nodes.removeValue(navMeshNode, true);
		}
		
		// STARTUP POINT
		// player.space = navMesh.rayCast(new Ray(new Vector3(0, 1000, 0), new Vector3(0,-1,0)));
		// player.direction.set(player.space.normal).crs(player.space.triangle.edgeA.vector).nor(); // TODO fix direction at start ?
		
		// resetCamera();
		
		player.scene = new Scene(game.getCarModel());
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
	
	@Override
	public void dispose() {
		sceneManager.dispose();
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
			if(Gdx.input.isKeyJustPressed(Input.Keys.H)){
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
			
			float cameraForce = 3;
			
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
		if(bonusHitList.size > 0){
			GameAssets.i.bonusSoundHard.play(.3f);
		}
		for(Bonus bonus : bonusHitList){
			bonusList.removeValue(bonus, true);
			sceneManager.getRenderableProviders().removeValue(bonus.model, true);
			game.bonus++;
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
			float s = 60;
			BoundingBox bbox = new BoundingBox(new Vector3(-s,-s,-s), new Vector3(s,s,s));
			// ((DirectionalShadowLight) sunLight).setViewport(30, 30, 1f, 100f);
			// ((DirectionalShadowLight) sunLight).setCenter(Vector3.Zero);
			((DirectionalShadowLight) sunLight).setBounds(bbox);
			((DirectionalShadowLight) sunLight).setCenter(camera.position.cpy().mulAdd(camera.direction, s * 1f));
		}
		sunLight.direction.set(2,-3,2).nor();
		sunLight.baseColor.set(Color.WHITE);
		sunLight.intensity = 7;
		sunLight.updateColor();
		
		// XXX 
		sceneManager.environment.set(new PBRFloatAttribute(PBRFloatAttribute.ShadowBias, 1.0f / 100f));
		sceneManager.setAmbientLight(0.8f);
		
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
		sceneManager.render();
	}

	public void resetPlayer(String axisID, boolean wrapZone) {
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
		
		fovTarget = 90;
		camera.fieldOfView = wrapZone ? 180 : fovTarget;
	}
	
	private void resetCamera() {
		updateCameraTarget();
		camera.direction.set(camDirectionTarget);
		camera.up.set(camUpTarget);
		smoothPos.set(player.space.position);
		applyCameraPosition();
		camera.update();
	}

	public int getBonusCount() {
		return bonusList.size;
	}
}
