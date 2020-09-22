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
	
	public void updateAsPlayer(NavMesh navMesh, float delta){
		if(space != null){
			float moveSpeed = delta * 2 * 0.1f * 100;
			float rotationSpeed = delta * 360 * .5f;
			boolean changed = false;
			if(Gdx.input.isKeyPressed(Input.Keys.UP)){
				acceleration = 1;
				// space.position.mulAdd(direction, moveSpeed);
				changed = true;
			}
			else if(Gdx.input.isKeyPressed(Input.Keys.DOWN)){
				acceleration = -.5f;
				// space.position.mulAdd(direction, -moveSpeed);
				changed = true;
			}else{
				acceleration = 0;
			}
			if(Gdx.input.isKeyPressed(Input.Keys.LEFT)){
				direction.rotate(space.normal, rotationSpeed);
			}
			if(Gdx.input.isKeyPressed(Input.Keys.RIGHT)){
				direction.rotate(space.normal, -rotationSpeed);
			}
			
			// damping/friction
			velocity = MathUtils.lerp(velocity, 0, delta * .5f);
			velocity += acceleration * delta * .3f;
			velocity = MathUtils.clamp(velocity, -moveSpeed/2, moveSpeed);
			space.position.mulAdd(direction, velocity);
			changed = true;
			
			//Vector3 gravity = vec1.set(space.position).nor().sub(space.normal).nor(); //.dot(space.normal);
			//space.position.mulAdd(gravity, delta * -0.1f);
			if(changed){
				space.triangle = navMesh.clipToSurface(space.triangle, space.position, space.normal, direction);
				if(space.triangle == null) space = null;
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
