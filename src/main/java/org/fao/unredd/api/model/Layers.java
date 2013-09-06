package org.fao.unredd.api.model;

import org.fao.unredd.api.json.AddLayerRequest;
import org.fao.unredd.api.json.LayerRepresentation;

public interface Layers {

	/**
	 * Return a list of the layers in the group
	 * 
	 * @return
	 */
	Iterable<LayerRepresentation> getJSON();

	/**
	 * Adds a layer to the backend accessed by this implementation and returns
	 * the created layer
	 * 
	 * @param addLayerRequest
	 * @return
	 */
	long addLayer(AddLayerRequest addLayerRequest);

	/**
	 * Get the layer with the specified ID
	 * 
	 * @param id
	 *            ID of the layer to be retrieved
	 * @return
	 * @throws IllegalArgumentException
	 *             If the specified id does not correspond to any layer
	 */
	public Layer getLayer(String id) throws IllegalArgumentException;

	/**
	 * Updates the layer with the specified ID setting all the fields to the
	 * ones contained by the {@link AddLayerRequest} instance
	 * 
	 * @param id
	 * @param layer
	 * @throws IllegalArgumentException
	 *             If there is no layer with the selected ID.
	 */
	void updateLayer(String id, AddLayerRequest layer)
			throws IllegalArgumentException;

	/**
	 * Deletes the layer with the specified ID
	 * 
	 * @param id
	 * 
	 * @throws IllegalArgumentException
	 *             If there is no layer with the selected ID.
	 */
	void deleteLayer(String id);

}
