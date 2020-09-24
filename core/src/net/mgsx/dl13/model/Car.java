package net.mgsx.dl13.model;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;

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
	
	public Car(GameModel game) {
		super();
		this.game = game;
	}

	public void updateAsPlayer(NavMesh navMesh, float delta){
		if(space != null){
			float moveSpeed = delta * 2 * 0.1f * 100 * 3;
			float rotationSpeed = delta * 360 * .5f * .6f;
			boolean changed = false;
			if(Gdx.input.isKeyPressed(Input.Keys.UP)){
				acceleration = 1;
			}
			else if(Gdx.input.isKeyPressed(Input.Keys.DOWN)){
				acceleration = -.5f;
			}else{
				acceleration = 0;
			}
			
			game.speed = Math.abs(velocity);
			
			if(game.running){
				float friction = .999f;
				if(Gdx.input.isKeyPressed(Input.Keys.LEFT)){
					direction.rotate(space.normal, rotationSpeed);
					velocity *= friction;
				}
				if(Gdx.input.isKeyPressed(Input.Keys.RIGHT)){
					direction.rotate(space.normal, -rotationSpeed);
					velocity *= friction;
				}
				
			}
			
			// damping/friction
			velocity = MathUtils.lerp(velocity, 0, delta * .5f);
			if(game.running) velocity += acceleration * delta * .3f;
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
					
				}
			}
			
			// XXX 
			if(direction.isZero()){
				direction.set(1,0,0);
			}
			
			float s = 1f;
			scene.modelInstance.transform.idt()
			.setToLookAt(space.position, space.position.cpy().mulAdd(direction, -1), space.normal)
			.inv()
			.scale(s, s, s);
		}
	}

	public void sync(Car player) {
		acceleration = player.acceleration;
		velocity = player.velocity;
	}
}
