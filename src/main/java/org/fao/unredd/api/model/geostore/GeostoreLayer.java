package org.fao.unredd.api.model.geostore;

import it.geosolutions.geostore.core.model.Resource;
import it.geosolutions.geostore.services.rest.GeoStoreClient;
import it.geosolutions.unredd.geostore.model.UNREDDLayer;

import org.fao.unredd.api.json.LayerRepresentation;
import org.fao.unredd.api.model.Layer;
import org.fao.unredd.api.model.LayerType;
import org.fao.unredd.api.model.LayerUpdates;

public class GeostoreLayer extends AbstractGeostoreElement implements Layer {

	private GeoStoreClient geostoreClient;

	public GeostoreLayer(Resource resource, GeoStoreClient geostoreClient) {
		super(resource);
		this.geostoreClient = geostoreClient;
	}

	@Override
	public LayerRepresentation getJSON() {
		return new LayerRepresentation(
				Long.toString(resource.getId()),
				resource.getName(),
				LayerType.valueOf(getAttribute(
						UNREDDLayer.Attributes.LAYERTYPE.getName()).getValue()),
				getAttribute(UNREDDLayer.Attributes.MOSAICPATH.getName())
						.getValue(), getAttribute(
						UNREDDLayer.Attributes.DISSMOSAICPATH.getName())
						.getValue(), getAttribute(
						UNREDDLayer.Attributes.ORIGDATADESTPATH.getName())
						.getValue(), getAttribute(
						UNREDDLayer.Attributes.RASTERPIXELWIDTH.getName())
						.getNumberValue().intValue(), getAttribute(
						UNREDDLayer.Attributes.RASTERPIXELHEIGHT.getName())
						.getNumberValue().intValue(), getAttribute(
						UNREDDLayer.Attributes.RASTERX0.getName())
						.getNumberValue(), getAttribute(
						UNREDDLayer.Attributes.RASTERX1.getName())
						.getNumberValue(), getAttribute(
						UNREDDLayer.Attributes.RASTERY0.getName())
						.getNumberValue(), getAttribute(
						UNREDDLayer.Attributes.RASTERY1.getName())
						.getNumberValue());
	}

	@Override
	public LayerUpdates getLayerUpdates() {
		return new GeostoreLayerLayerUpdates(resource.getId(), geostoreClient);

	}
}