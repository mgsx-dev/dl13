package net.mgsx.dl13.model;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;

import net.mgsx.dl13.DL13Game;
import net.mgsx.dl13.assets.GameAssets;
import net.mgsx.dl13.navmesh.NavMesh;
import net.mgsx.dl13.navmesh.NavMesh.RayCastResult;
import net.mgsx.gltf.scene3d.scene.Scene;

public class Car {
	public RayCastResult space;
	public Vector3 direction = new Vector3();
	public Scene scene;
	
	public float acceleration = 0;
	public float velocity = 0;
	
	private GameModel game;
	private float collisionTimeout;
	
	public Car(GameModel game) {
		super();
		this.game = game;
	}

	public void updateAsPlayer(NavMesh navMesh, float delta){
		collisionTimeout -= delta;
		
		GameInputs inputs = DL13Game.getInputs();
		
		if(space != null){
			float moveSpeed = delta * 2 * 0.1f * 100 * 3;
			float rotationSpeed = delta * 360 * .5f * .6f;
			boolean changed = false;
			if(game.running){
				if(inputs.controller.isOn(GameInputs.PlayerCommand.BRAKE)){
					acceleration = -.5f;
				}
				else if(inputs.controller.isOn(GameInputs.PlayerCommand.ACCEL)){
					acceleration = 1;
				}else{
					acceleration = 0;
				}
			}else{
				acceleration = 0;
			}
			
			game.speed = Math.abs(velocity);
			
			if(game.running){
				float friction = .999f;
				float rLeft = inputs.controller.getAnalog(GameInputs.PlayerCommand.LEFT);
				float rRight = inputs.controller.getAnalog(GameInputs.PlayerCommand.RIGHT);
				
//				System.out.println(rLeft + " " + rRight);
//				System.out.println(inputs.controller.isOn(GameInputs.PlayerCommand.LEFT));
				
				float epsilon = 0.01f;
				if(rLeft > epsilon){
					float fric = MathUtils.lerp(1, friction, rLeft);
					direction.rotate(space.normal, rotationSpeed * rLeft);
					velocity *= fric;
				}
				if(rRight > epsilon){
					float fric = MathUtils.lerp(1, friction, rRight);
					direction.rotate(space.normal, -rotationSpeed * rRight);
					velocity *= fric;
				}
				
			}
			
			// damping/friction
			velocity = MathUtils.lerp(velocity, 0, delta * .5f);
			if(game.running || game.finishLine) 
				velocity += acceleration * delta * .3f;
			velocity = MathUtils.clamp(velocity, -moveSpeed/2, moveSpeed);
			
			space.position.mulAdd(direction, velocity);
			changed = true;
			
			
			//Vector3 gravity = vec1.set(space.position).nor().sub(space.normal).nor(); //.dot(space.normal);
			//space.position.mulAdd(gravity, delta * -0.1f);
			if(changed){
				space.triangle = navMesh.clipToSurface(space.triangle, space.position, space.normal, direction);
				if(space.triangle == null) space = null;
				if(navMesh.clipToSurfaceOnEdge){
					velocity *= .9f;
					// make it bounce a bit
					// TODO use tmp vector
					Vector3 mirror = direction.cpy().mulAdd(navMesh.clipToSurfaceEdge, -2 * direction.dot(navMesh.clipToSurfaceEdge)).nor();
					space.position.mulAdd(mirror, 20 * velocity * delta);
					// direction.lerp(mirror, 1f);
					if(!mirror.isZero() && game.running) direction.lerp(mirror, 0.5f);
					
					if(collisionTimeout <= 0 && game.running){
						GameAssets.i.collisionSound.play(0.7f);
						collisionTimeout = .3f;
					}
				}
			}
			
			// XXX 
			if(direction.isZero()){
				direction.set(1,1,1).nor();
			}
			
			// XXX non invertible matrix workaround
			try{
				scene.modelInstance.transform.idt()
				.setToLookAt(space.position, space.position.cpy().mulAdd(direction, -1), space.normal)
				.inv();
			}catch(RuntimeException e){
				scene.modelInstance.transform.setToTranslation(space.position);
			}
		}
	}

	public void sync(Car player) {
		acceleration = player.acceleration;
		velocity = player.velocity;
	}
}
