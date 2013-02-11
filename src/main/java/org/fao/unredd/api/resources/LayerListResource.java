package org.fao.unredd.api.resources;

import java.net.URI;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.fao.unredd.api.json.AddLayerRequest;
import org.fao.unredd.api.json.LayersResponseRoot;
import org.fao.unredd.api.model.Layers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Path("/layers")
public class LayerListResource {

	@Autowired
	private Layers layers;

	@Context
	private UriInfo uriInfo;

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public LayersResponseRoot asJSON() {
		return new LayersResponseRoot(layers.getJSON());
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public Response addLayer(AddLayerRequest layerRequest) {
		List<String> errors = layerRequest.validate();
		if (errors.size() > 0) {
			throw new BadRequestException(errors);
		}

		long id = layers.addLayer(layerRequest);
		UriBuilder uriBuilder = UriBuilder.fromUri(uriInfo.getRequestUri());
		uriBuilder.path("{id}");
		URI location = uriBuilder.build(Long.toString(id));
		return Response.created(location).type(MediaType.APPLICATION_JSON)
				.build();
	}

}
