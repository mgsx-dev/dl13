package net.mgsx.dl13.model;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Vector3;

import net.mgsx.dl13.navmesh.NavMesh;
import net.mgsx.dl13.navmesh.NavMesh.RayCastResult;
import net.mgsx.gltf.scene3d.scene.Scene;

public class Car {
	public RayCastResult space;
	public Vector3 direction = new Vector3();
	public Scene scene;
	
	public void updateAsPlayer(NavMesh navMesh, float delta){
		if(space != null){
			float moveSpeed = delta * 2 * 0.1f * 100;
			float rotationSpeed = delta * 360 * .5f;
			boolean changed = false;
			if(Gdx.input.isKeyPressed(Input.Keys.UP)){
				space.position.mulAdd(direction, moveSpeed);
				changed = true;
			}
			if(Gdx.input.isKeyPressed(Input.Keys.DOWN)){
				space.position.mulAdd(direction, -moveSpeed);
				changed = true;
			}
			if(Gdx.input.isKeyPressed(Input.Keys.LEFT)){
				direction.rotate(space.normal, rotationSpeed);
			}
			if(Gdx.input.isKeyPressed(Input.Keys.RIGHT)){
				direction.rotate(space.normal, -rotationSpeed);
			}
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
}
