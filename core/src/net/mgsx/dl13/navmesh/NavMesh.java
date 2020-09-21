package net.mgsx.dl13.navmesh;

import java.util.Comparator;

import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.utils.Array;

public class NavMesh {

	private static final Vector3 tmp = new Vector3();
	
	private static final Vector3 delta0 = new Vector3();
	private static final Vector3 edge0 = new Vector3();
	private static final Vector3 tangent = new Vector3();
	private static final Vector3 center = new Vector3();
	private static final Vector3 v0 = new Vector3();
	private static final Vector3 v1 = new Vector3();
	private static final Vector3 v2 = new Vector3();
	
	public static class Triangle{
		public Edge edgeA, edgeB, edgeC;
		public Vertex vertexA, vertexB, vertexC;
		public Vector3 center = new Vector3();
		public Vector3 normal = new Vector3();
		public Vector3 tangent = new Vector3();
		public Vector3 binormal = new Vector3();
		public void unproject(Vector3 translation, Vector2 tangentPosition) {
//			float dst = tmp.set(translation).sub(center).dot(normal);
//			tmp.set(translation).mulAdd(normal, -dst).sub(vertexA.position);
			tmp.set(translation).sub(center);
			tangentPosition.x = tmp.dot(tangent);
			tangentPosition.y = tmp.dot(binormal);
			// System.out.println(tangentPosition);
		}
		public Vector3 project(Vector3 translation, Vector2 tangentPosition) {
			return translation.set(center)
			.mulAdd(tangent, tangentPosition.x)
			.mulAdd(binormal, tangentPosition.y);
		}
		
		// TODO : TO BE TESTED ! first tests look good
		public Vector3 getNormal(Vector3 result, Vector3 position) {
			return result.setZero()
				.mulAdd(vertexA.normal, 1f / position.dst(vertexA.position))
				.mulAdd(vertexB.normal, 1f / position.dst(vertexB.position))
				.mulAdd(vertexC.normal, 1f / position.dst(vertexC.position)).nor();
		}
	}
	public static class Edge{
		public Edge(Vertex vertexA, Vertex vertexB) {
			this.vertexA = vertexA;
			this.vertexB = vertexB;
			vector.set(vertexB.position).sub(vertexA.position).nor();
		}
		public Vertex vertexA, vertexB;
		Triangle triangleA, triangleB;
		public Vector3 vector = new Vector3();
		public boolean isOpened() {
			return triangleA == null || triangleB == null;
		}
	}
	public static class Vertex{
		public Vector3 position = new Vector3();
		public Array<Edge> edges = new Array<Edge>();
		public Array<Triangle> triangles = new Array<Triangle>();
		public int index;
		public Vector3 normal = new Vector3();
		public Vector3 computeNormal(){
			normal.setZero();
			for(Triangle t : triangles){
				normal.add(t.normal);
			}
			return normal.nor();
		}
		public void addEdges(Edge edge, boolean forward) {
			edges.add(edge);
		}
	}
	public Array<Vertex> vertices;
	public Array<Triangle> triangles;
	public Array<Edge> edges;

	public Triangle find(Vector2 tangentPosition, final Vector3 translation) 
	{
		triangles.sort(new Comparator<Triangle>() {
			@Override
			public int compare(Triangle o1, Triangle o2) {
				return Float.compare(o1.center.dst2(translation), o2.center.dst2(translation)) ;
			}
		});
		Triangle triangle = triangles.first();
		triangle.unproject(translation, tangentPosition);
		return triangle;
	}
	
	public static class RayCastResult{
		public Triangle triangle;
		public Vector3 position = new Vector3();
		public Vector3 normal = new Vector3();
	}
	
	public RayCastResult rayCast(Ray ray){
		Vector3 intersection = new Vector3();
		RayCastResult result = new RayCastResult();
		float bestDst2 = Float.MAX_VALUE;
		for(Triangle t : triangles){
			if(Intersector.intersectRayTriangle(ray, t.vertexA.position, t.vertexB.position, t.vertexC.position, intersection)){
				float dst2 = intersection.dst2(ray.origin);
				if(result.triangle == null || dst2 < bestDst2){
					bestDst2 = dst2;
					result.position.set(intersection);
					result.triangle = t;
				}
			}
		}
		if(result.triangle != null){
			result.triangle.getNormal(result.normal, result.position);
			return result;
		}
		return null;
	}
	
	public Vector3 getNormal(Vector3 normal, Triangle triangle, Vector2 tangentPosition){
		// TODO use tangent position
/*		triangle.getNormal(normal, tmp.set(triangle.vertexA.position)
				.mulAdd(triangle.vertexB.position, tangentPosition.x)
				.mulAdd(triangle.vertexB.position, tangentPosition.x));
	*/
		return normal.set(triangle.normal);
	}
	
	// TODO could be added to libgdx GeometryUtils
	private static Vector3 barycentric(Vector3 result, Vector3 p, Vector3 a, Vector3 b, Vector3 c)
	{
		v0.set(b).sub(a);
		v1.set(c).sub(a);
		v2.set(p).sub(a);
		
	    float d00 = v0.dot(v0);
	    float d01 = v0.dot(v1);
	    float d11 = v1.dot(v1);
	    float d20 = v2.dot(v0);
	    float d21 = v2.dot(v1);
	    float denom = d00 * d11 - d01 * d01;
	    if(denom == 0){// XXX
	    	denom = 1; 
	    }
	    float v = (d11 * d20 - d01 * d21) / denom;
	    float w = (d00 * d21 - d01 * d20) / denom;
	    float u = 1.0f - v - w;
	    
	    return result.set(u, v, w);
	}
	
	public Triangle clipToSurface(Triangle triangle, Vector3 position, Vector3 normal, Vector3 direction) 
	{
		Edge[] edges = new Edge[]{triangle.edgeA, triangle.edgeB, triangle.edgeC};
		for(int i=0 ; i<3 ; i++){
			Edge edge = edges[i];
			v0.set(edge.vertexA.position).sub(triangle.center);
			v1.set(edge.vertexB.position).sub(triangle.center);
			float flipFactor = v0.crs(v1).dot(triangle.normal);
			
			delta0.set(position).sub(edge.vertexA.position);
			edge0.set(edge.vertexB.position).sub(edge.vertexA.position).nor();
			
			if(v0.set(delta0).crs(edge0).dot(triangle.normal) * flipFactor > 0){
				Triangle nextTriangle = edge.triangleA == triangle ? edge.triangleB : edge.triangleA;
				if(nextTriangle == null){
					float dot = delta0.dot(edge0);
					position.set(edge.vertexA.position).mulAdd(edge0, MathUtils.clamp(dot, 0, 1));
				}else{
					triangle = nextTriangle;
				}
				break;
			}
		}
		
		boolean smoothNormal = true; // TODO set optional
		
		// update normal
		if(smoothNormal){
			barycentric(center, position, triangle.vertexA.position, triangle.vertexB.position, triangle.vertexC.position);
			
			normal.setZero()
			.mulAdd(triangle.vertexA.normal, center.x)
			.mulAdd(triangle.vertexB.normal, center.y)
			.mulAdd(triangle.vertexC.normal, center.z).nor();
			
			position.setZero()
			.mulAdd(triangle.vertexA.position, center.x)
			.mulAdd(triangle.vertexB.position, center.y)
			.mulAdd(triangle.vertexC.position, center.z);
			
		}else{
			normal.set(triangle.normal);
			
			// project position
			delta0.set(position).sub(triangle.vertexA.position);
			float dot = delta0.dot(triangle.normal);
			position.mulAdd(triangle.normal, -dot);
		}
		
		// update direction
		tangent.set(direction).crs(normal);
		direction.set(normal).crs(tangent).nor();
		
		return triangle;
	}
}
