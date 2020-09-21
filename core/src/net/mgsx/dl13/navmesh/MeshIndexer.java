package net.mgsx.dl13.navmesh;

import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.graphics.g3d.model.NodePart;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.FloatArray;
import com.badlogic.gdx.utils.IntArray;

import net.mgsx.dl13.navmesh.NavMesh.Edge;
import net.mgsx.dl13.navmesh.NavMesh.Triangle;
import net.mgsx.dl13.navmesh.NavMesh.Vertex;

// TODO rename, NavMeshBuilder
public class MeshIndexer {
	/* TODO sort during merge to optimize performances
	private static final Comparator<Vector3> ascX = new Comparator<Vector3>() {
		@Override
		public int compare(Vector3 o1, Vector3 o2) {
			return Float.compare(o1.x, o2.x);
		}
	};
	private static final Comparator<Vector3> ascY = new Comparator<Vector3>() {
		@Override
		public int compare(Vector3 o1, Vector3 o2) {
			return Float.compare(o1.x, o2.x);
		}
	};
	private static final Comparator<Vector3> ascZ = new Comparator<Vector3>() {
		@Override
		public int compare(Vector3 o1, Vector3 o2) {
			return Float.compare(o1.x, o2.x);
		}
	};
	*/
	
	public MeshIndexer begin(){
		return this; // TODO
	}
	public NavMesh end(){
		return null; // TODO
	}
	
	public static boolean SKIP_SAME_EDGES;
	
	public int mergeByEpsilon(Array<Vector3> points, IntArray indices){
		return mergeByEpsilon(points, indices, MathUtils.FLOAT_ROUNDING_ERROR);
	}
	public int mergeByEpsilon(Array<Vector3> points, IntArray indices, float epsilon){
		int vc=0;
		IntArray mapping = new IntArray(indices.size);
		for(int i=0 ; i<points.size ; i++){
			Vector3 a = points.get(i);
			// boolean found = false;
			for(int j=0 ; j<mapping.size ; j++){
				int index = mapping.items[j];
				Vector3 b = points.get(index);
				if(b.epsilonEquals(a, epsilon)){
					mapping.add(index);
					indices.add(indices.get(j));
					break;
				}
			}
			if(i == mapping.size){
				mapping.add(i);
				indices.add(vc++);
			}
		}
		
		return vc;
	}
	
	public FloatArray extractVertices(Node node){
		FloatArray result = new FloatArray();
		Vector3 pos = new Vector3();
		Matrix4 transform = new Matrix4(node.globalTransform);
		for(NodePart nodePart : node.parts){
			Mesh mesh = nodePart.meshPart.mesh;
			VertexAttribute positionAttribute = mesh.getVertexAttribute(VertexAttributes.Usage.Position);
			float [] buffer = new float[3];
			if(mesh.getNumIndices() > 0){
				result.ensureCapacity(result.size + mesh.getNumIndices());
				short[] indices = new short[mesh.getNumIndices()];
				mesh.getIndices(indices);
				for(int i=0 ; i<indices.length ; i++){
					int index = (int)indices[i] & 0xFFFF;
					mesh.getVertices(index * mesh.getVertexSize() / 4 + positionAttribute.offset/4, buffer);
					pos.set(buffer).mul(transform);
					result.add(pos.x, pos.y, pos.z);
				}
			}else{
				result.ensureCapacity(result.size + mesh.getNumVertices()*3);
				for(int v = 0 ; v<mesh.getNumVertices() ; v++){
					mesh.getVertices(v * mesh.getVertexSize() / 4 + positionAttribute.offset/4, buffer);
					pos.set(buffer).mul(transform);
					result.add(pos.x, pos.y, pos.z);
				}
			}
		}
		return result;
	}
	
	public Array<Vector3> buildVertices(FloatArray vertices){
		Array<Vector3> points = new Array<Vector3>();
		for(int i=0 ; i<vertices.size ; i+=3){
			points.add(new Vector3(vertices.items[i], vertices.items[i+1], vertices.items[i+2]));
		}
		return points;
	}
	
	public NavMesh build(FloatArray inputVertices, float epsilon){
		Array<Vector3> points = buildVertices(inputVertices);
		IntArray indices = new IntArray(points.size);
		int numVertices = mergeByEpsilon(points, indices, epsilon);
		
		NavMesh navMesh = new NavMesh();
		navMesh.vertices = new Array<Vertex>(numVertices);
		navMesh.vertices.setSize(numVertices);
		for(int i=0 ; i<points.size ; i++){
			int index = indices.get(i);
			if(navMesh.vertices.get(index) == null){
				Vertex vertex = new Vertex();
				vertex.index = index;
				vertex.position.set(points.get(i));
				navMesh.vertices.set(index, vertex);
			}
		}
		
		
		int numTriangles = indices.size / 3;
		navMesh.triangles = new Array<Triangle>(numTriangles);
		navMesh.edges = new Array<NavMesh.Edge>(numTriangles * 3);
		for(int i=0 ; i<indices.size ; i+=3){
			Triangle t = new Triangle();
			t.vertexA = navMesh.vertices.get(indices.items[i]);
			t.vertexB = navMesh.vertices.get(indices.items[i+1]);
			t.vertexC = navMesh.vertices.get(indices.items[i+2]);
			
			// filter empty triangles.
			if(t.vertexA == t.vertexB || t.vertexB == t.vertexC || t.vertexC == t.vertexA){
				continue;
			}
			
			t.edgeA = findEdge(navMesh, t, t.vertexA, t.vertexB);
			t.edgeB = findEdge(navMesh, t, t.vertexB, t.vertexC);
			t.edgeC = findEdge(navMesh, t, t.vertexC, t.vertexA);
			
			t.vertexA.triangles.add(t);
			t.vertexB.triangles.add(t);
			t.vertexC.triangles.add(t);
			t.vertexA.addEdges(t.edgeA, true);
			t.vertexA.addEdges(t.edgeC, false);
			t.vertexB.addEdges(t.edgeB, true);
			t.vertexB.addEdges(t.edgeA, false);
			t.vertexC.addEdges(t.edgeC, true);
			t.vertexC.addEdges(t.edgeB, false);
			t.center.set(t.vertexA.position).add(t.vertexB.position).add(t.vertexC.position).scl(1f / 3f);
			
			t.normal.set(t.vertexB.position).sub(t.vertexA.position);
			t.tangent.set(t.vertexC.position).sub(t.vertexA.position);
			t.normal.crs(t.tangent).nor();
			
			t.tangent.set(t.edgeA.vector).crs(t.normal).nor();
			t.binormal.set(t.tangent).crs(t.normal);
			navMesh.triangles.add(t);
		}
		for(Vertex v : navMesh.vertices){
			v.computeNormal();
		}
		
		return navMesh;
	}
	private Edge findEdge(NavMesh navMesh, Triangle t, Vertex vertexA, Vertex vertexB) {
		for(Edge edge : navMesh.edges){
			if(edge.vertexA == vertexB && edge.vertexB == vertexA){
				if(edge.triangleB != null){
					// few edges cases require to build another edge (2 triangles inverted)
					if(SKIP_SAME_EDGES) return edge;
					continue;
				}
				edge.triangleB = t;
				return edge;
			}
		}
		Edge edge = new Edge(vertexA, vertexB);
		edge.triangleA = t;
		navMesh.edges.add(edge);
		return edge;
	}
	@Deprecated
	public NavMesh build(ModelInstance model, float epsilon) {
		return build(model.model.nodes.first()); // TODO
	}
	@Deprecated
	public NavMesh build(Node node){
		NavMesh navMesh = new NavMesh();
		node.calculateTransforms(true);
		Matrix4 transform = node.globalTransform;
		
		navMesh.vertices = new Array<Vertex>();
		
		Mesh mesh = node.parts.first().meshPart.mesh;
		VertexAttribute positionAttribute = mesh.getVertexAttribute(VertexAttributes.Usage.Position);
		float [] buffer = new float[3];
		for(int v = 0 ; v<mesh.getNumVertices() ; v++){
			mesh.getVertices(v * mesh.getVertexSize() / 4 + positionAttribute.offset/4, buffer);
			Vertex vertex = new Vertex();
			vertex.position.set(buffer).mul(transform);
			navMesh.vertices.add(vertex);
			vertex.index = v;
		}
		navMesh.triangles = new Array<Triangle>();
		short[] indices = new short[mesh.getNumIndices()];
		mesh.getIndices(indices);
		navMesh.edges = new Array<NavMesh.Edge>();
		for(int i=0 ; i<indices.length ; i+=3){
			Triangle t = new Triangle();
			t.vertexA = navMesh.vertices.get(indices[i]);
			t.vertexB = navMesh.vertices.get(indices[i+1]);
			t.vertexC = navMesh.vertices.get(indices[i+2]);
			
			t.edgeA = findEdge(navMesh, t, t.vertexA, t.vertexB);
			t.edgeB = findEdge(navMesh, t, t.vertexB, t.vertexC);
			t.edgeC = findEdge(navMesh, t, t.vertexC, t.vertexA);

			t.vertexA.triangles.add(t);
			t.vertexB.triangles.add(t);
			t.vertexC.triangles.add(t);
			
			t.vertexA.addEdges(t.edgeA, true);
			t.vertexA.addEdges(t.edgeC, false);
			t.vertexB.addEdges(t.edgeB, true);
			t.vertexB.addEdges(t.edgeA, false);
			t.vertexC.addEdges(t.edgeC, true);
			t.vertexC.addEdges(t.edgeB, false);
			
			t.edgeA.vector.set(t.vertexB.position).sub(t.vertexA.position);
			t.edgeB.vector.set(t.vertexC.position).sub(t.vertexB.position);

			t.center.set(t.vertexA.position).add(t.vertexB.position).add(t.vertexC.position).scl(1f / 3f);
			t.normal.set(t.edgeA.vector).nor().crs(t.edgeB.vector).nor();
			t.tangent.set(t.edgeA.vector).nor().crs(t.normal).nor();
			t.binormal.set(t.tangent).crs(t.normal);
			navMesh.triangles.add(t);
		}
		for(Vertex v : navMesh.vertices){
			v.computeNormal();
		}
		return navMesh;
	}
	
}
