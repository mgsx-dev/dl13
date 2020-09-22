package net.mgsx.dl13.navmesh;

import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectSet;

import net.mgsx.dl13.navmesh.NavMesh.Triangle;

public class NavMeshTree {
	private NavMesh navMesh;
	private int maxPerNode;
	
	public static final Vector3 vec1 = new Vector3();
	
	private class TreeNode{
		final BoundingBox box = new BoundingBox();
		final Array<Triangle> triangles = new Array<NavMesh.Triangle>();
		final Array<TreeNode> children = new Array<NavMeshTree.TreeNode>();
		
		public void add(Triangle t) {
			if(box.contains(t.vertexA.position) || box.contains(t.vertexB.position) || box.contains(t.vertexC.position)){
				triangles.add(t);
				if(children.size > 0){
					for(TreeNode child : children){
						child.add(t);
					}
				}else{
					if(triangles.size >= maxPerNode){
						for(int x=0 ; x<2 ; x++){
							for(int y=0 ; y<2 ; y++){
								for(int z=0 ; z<2 ; z++){
									TreeNode child = new TreeNode();
									children.add(child);
									child.box.set(
											child.box.min.set(
													box.min.x + box.getWidth() / 2 * x, 
													box.min.y + box.getHeight() / 2 * y,
													box.min.z + box.getDepth() / 2 * z), 
											child.box.max.set(
													box.min.x + box.getWidth() / 2 * (x+1), 
													box.min.y + box.getHeight() / 2 * (y+1),
													box.min.z + box.getDepth() / 2 * (z+1)));
									child.add(t);
								}
							}
						}
					}
				}
			}
		}

		public void get(ObjectSet<Triangle> result, BoundingBox query) {
			if(query.intersects(box)){
				if(query.contains(box)){
					result.addAll(triangles);
				}else{
					if(children.size > 0){
						for(TreeNode child : children){
							child.get(result, query);
						}
					}
				}
			}
		}

		
		
		public RayCastResult rayCast(Ray ray, RayCastResult result) {
			if(Intersector.intersectRayBoundsFast(ray, box)){
				if(children.size > 0){
					for(TreeNode child : children){
						child.rayCast(ray, result);
					}
				}else{
					for(Triangle t : triangles){
						if(Intersector.intersectRayTriangle(ray, t.vertexA.position, t.vertexB.position, t.vertexC.position, vec1)){
							float newTime = ray.origin.dst2(vec1);
							if(result.triangle == null || result.time > newTime){
								result.time = newTime;
								result.triangle = t;
								result.position.set(vec1);
							}
						}
					}
				}
			}
			return result;
		}
	}
	
	public static class RayCastResult {
		public Triangle triangle = null;
		public Vector3 position = new Vector3();
		public Vector3 normal = new Vector3();
		public float time = Float.MAX_VALUE;
	}
	
	TreeNode root;
	
	public void get(ObjectSet<Triangle> triangles, BoundingBox box){
		root.get(triangles, box);
	}
	public RayCastResult rayCast(RayCastResult result, Ray ray){
		root.rayCast(ray, result);
		if(result.triangle != null) result.triangle.getNormal(result.normal, result.position);
		return result;
	}
	
	public void build(){
	
		root = new TreeNode();
		root.box.inf();
		for(Triangle t : navMesh.triangles){
			root.box.ext(t.vertexA.position);
		}
		root.box.set(new Vector3(), new Vector3(1,1,1)); // TODO compute from mesh
		
		for(Triangle t : navMesh.triangles){
			root.add(t);
			
		}
		
	}
}
