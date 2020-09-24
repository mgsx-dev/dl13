package net.mgsx.dl13.assets;

import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.graphics.Cubemap;

import net.mgsx.gltf.scene3d.utils.EnvironmentUtil;

public class IBL {

	public Cubemap diffuseCubemap;
	public Cubemap environmentCubemap;
	public Cubemap specularCubemap;

	public IBL(String basePath, String ext, int nbCascade) {
		diffuseCubemap = EnvironmentUtil.createCubemap(new InternalFileHandleResolver(), 
				basePath + "/diffuse/diffuse_", ext, EnvironmentUtil.FACE_NAMES_NEG_POS);
		
		environmentCubemap = EnvironmentUtil.createCubemap(new InternalFileHandleResolver(), 
				basePath + "/environment/environment_", ext, EnvironmentUtil.FACE_NAMES_NEG_POS);
		
		specularCubemap = EnvironmentUtil.createCubemap(new InternalFileHandleResolver(), 
				basePath + "/specular/specular_", "_", ext, nbCascade, EnvironmentUtil.FACE_NAMES_NEG_POS);
	}

}
