package org.fao.unredd.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import it.geosolutions.geostore.core.model.Attribute;
import it.geosolutions.geostore.core.model.Resource;
import it.geosolutions.geostore.services.dto.ShortAttribute;
import it.geosolutions.geostore.services.rest.model.RESTResource;
import it.geosolutions.geostore.services.rest.model.ResourceList;
import it.geosolutions.unredd.geostore.model.UNREDDLayer;
import it.geosolutions.unredd.geostore.model.UNREDDLayer.Attributes;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.ws.rs.core.MediaType;

import org.fao.unredd.api.json.AddLayerRequest;
import org.fao.unredd.api.json.LayerRepresentation;
import org.fao.unredd.api.json.LayersResponseRoot;
import org.fao.unredd.api.model.LayerType;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;

public class LayersTest extends AbstractRestTest {

	@Test
	public void testCreateLayerNullFail() throws Exception {
		testNull(new AddLayerRequest(null, LayerType.RASTER, "/", "/",
				"/foobar", 1, 1, 1d, 2d, 1d, 2d));
		testNull(new AddLayerRequest("newlayer", null, "/", "/", "/foobar", 1,
				1, 1d, 2d, 1d, 2d));
		testNull(new AddLayerRequest("newlayer", LayerType.RASTER, null, "/",
				"/foobar", 1, 1, 1d, 2d, 1d, 2d));
		testNull(new AddLayerRequest("newlayer", LayerType.RASTER, "/", null,
				"/foobar", 1, 1, 1d, 2d, 1d, 2d));
		testNull(new AddLayerRequest("newlayer", LayerType.RASTER, "/", "/",
				null, 1, 1, 1d, 2d, 1d, 2d));
		testNull(new AddLayerRequest("newlayer", LayerType.RASTER, "/", "/",
				"/foobar", null, 1, 1d, 2d, 1d, 2d));
		testNull(new AddLayerRequest("newlayer", LayerType.RASTER, "/", "/",
				"/foobar", 1, null, 1d, 2d, 1d, 2d));
		testNull(new AddLayerRequest("newlayer", LayerType.RASTER, "/", "/",
				"/foobar", 1, 1, null, 2d, 1d, 2d));
		testNull(new AddLayerRequest("newlayer", LayerType.RASTER, "/", "/",
				"/foobar", 1, 1, 1d, null, 1d, 2d));
		testNull(new AddLayerRequest("newlayer", LayerType.RASTER, "/", "/",
				"/foobar", 1, 1, 1d, 2d, null, 2d));
		testNull(new AddLayerRequest("newlayer", LayerType.RASTER, "/", "/",
				"/foobar", 1, 1, 1d, 2d, 1d, null));
	}

	private void testNull(AddLayerRequest request) {
		ClientResponse response = createLayer(request);
		assertEquals(ClientResponse.Status.BAD_REQUEST,
				response.getClientResponseStatus());
		String[] errorList = response.getEntity(String[].class);
		assertEquals(1, errorList.length);
	}

	@Test
	public void testGetEmptyLayers() throws Exception {
		ResourceList resourceList = mockResourceList();
		mockGeostoreSearchAnswer(resourceList);

		ClientResponse response = getLayersOk();

		LayersResponseRoot root = response.getEntity(LayersResponseRoot.class);
		assertFalse(root.getLayers().iterator().hasNext());
	}

	@Test
	public void testGetOneLayer() throws Exception {
		ResourceList resourceList = mockResourceList(mockResource(1L,
				"newLayer", LayerType.RASTER));
		mockGeostoreSearchAnswer(resourceList);

		ClientResponse response = getLayersOk();

		LayersResponseRoot root = response.getEntity(LayersResponseRoot.class);
		Iterator<LayerRepresentation> layerIterator = root.getLayers()
				.iterator();
		assertTrue(layerIterator.hasNext());
		layerIterator.next();
		assertFalse(layerIterator.hasNext());
	}

	@Test
	public void testGetLayers() throws Exception {
		ResourceList resourceList = mockResourceList(
				mockResource(0L, "newLayer0", LayerType.RASTER),
				mockResource(1L, "newLayer1", LayerType.VECTOR));
		mockGeostoreSearchAnswer(resourceList);

		ClientResponse response = getLayersOk();

		LayersResponseRoot root = response.getEntity(LayersResponseRoot.class);
		Iterator<LayerRepresentation> layerIterator = root.getLayers()
				.iterator();
		assertTrue(layerIterator.hasNext());
		LayerRepresentation layer = layerIterator.next();
		assertEquals("0", layer.getId());
		assertEquals("newLayer0", layer.getName());

		assertTrue(layerIterator.hasNext());
		layer = layerIterator.next();
		assertEquals("1", layer.getId());
		assertEquals("newLayer1", layer.getName());

		assertFalse(layerIterator.hasNext());
	}

	@Test
	public void testCreateLayer() throws Exception {
		when(geostoreClient.insert(any(RESTResource.class))).thenReturn(12L);

		ClientResponse response = createLayerOk(new AddLayerRequest("newlayer",
				LayerType.RASTER, "/foo", "/bar", "/foobar", 2, 1, 1d, 3d, 10d,
				11d));
		String location = response.getHeaders().getFirst("location");
		assertTrue(location.endsWith("/layers/12"));

		ArgumentCaptor<RESTResource> resourceCaptor = ArgumentCaptor
				.forClass(RESTResource.class);
		verify(geostoreClient).insert(resourceCaptor.capture());
		RESTResource resource = resourceCaptor.getValue();
		assertEquals(resource.getName(), "newlayer");
		assertEquals(getAttribute(resource, UNREDDLayer.Attributes.LAYERTYPE),
				LayerType.RASTER.toString());
		assertEquals(getAttribute(resource, UNREDDLayer.Attributes.MOSAICPATH),
				"/foo");
		assertEquals(
				getAttribute(resource, UNREDDLayer.Attributes.DISSMOSAICPATH),
				"/bar");
		assertEquals(
				getAttribute(resource, UNREDDLayer.Attributes.ORIGDATADESTPATH),
				"/foobar");
		assertEquals(
				getAttribute(resource, UNREDDLayer.Attributes.RASTERPIXELHEIGHT),
				"1");
		assertEquals(
				getAttribute(resource, UNREDDLayer.Attributes.RASTERPIXELWIDTH),
				"2");
		assertEquals(getAttribute(resource, UNREDDLayer.Attributes.RASTERX0),
				"1.0");
		assertEquals(getAttribute(resource, UNREDDLayer.Attributes.RASTERX1),
				"3.0");
		assertEquals(getAttribute(resource, UNREDDLayer.Attributes.RASTERY0),
				"10.0");
		assertEquals(getAttribute(resource, UNREDDLayer.Attributes.RASTERY1),
				"11.0");
	}

	@Test
	public void testModifyLayer() {
		ClientResponse response = modifyLayer("243", new AddLayerRequest(
				"newlayer", LayerType.RASTER, "/foo", "/bar", "/foobar", 2, 1,
				1d, 3d, 10d, 11d));
		assertEquals(ClientResponse.Status.NO_CONTENT,
				response.getClientResponseStatus());

		ArgumentCaptor<RESTResource> resourceCaptor = ArgumentCaptor
				.forClass(RESTResource.class);
		ArgumentCaptor<Long> idCaptor = ArgumentCaptor.forClass(Long.class);
		verify(geostoreClient).updateResource(idCaptor.capture(),
				resourceCaptor.capture());
		assertEquals(243L, idCaptor.getValue().longValue());
		RESTResource resource = resourceCaptor.getValue();
		assertEquals(resource.getName(), "newlayer");
		assertEquals(getAttribute(resource, UNREDDLayer.Attributes.LAYERTYPE),
				LayerType.RASTER.toString());
		assertEquals(getAttribute(resource, UNREDDLayer.Attributes.MOSAICPATH),
				"/foo");
		assertEquals(
				getAttribute(resource, UNREDDLayer.Attributes.DISSMOSAICPATH),
				"/bar");
		assertEquals(
				getAttribute(resource, UNREDDLayer.Attributes.ORIGDATADESTPATH),
				"/foobar");
		assertEquals(
				getAttribute(resource, UNREDDLayer.Attributes.RASTERPIXELHEIGHT),
				"1");
		assertEquals(
				getAttribute(resource, UNREDDLayer.Attributes.RASTERPIXELWIDTH),
				"2");
		assertEquals(getAttribute(resource, UNREDDLayer.Attributes.RASTERX0),
				"1.0");
		assertEquals(getAttribute(resource, UNREDDLayer.Attributes.RASTERX1),
				"3.0");
		assertEquals(getAttribute(resource, UNREDDLayer.Attributes.RASTERY0),
				"10.0");
		assertEquals(getAttribute(resource, UNREDDLayer.Attributes.RASTERY1),
				"11.0");
	}

	@Test
	public void testModifyGeostoreClientError() throws Exception {
		testModifyGeostoreClientError(404);
		testModifyGeostoreClientError(500);
	}

	private void testModifyGeostoreClientError(int geostoreErrorCode) {
		ClientResponse clientResponse = mock(ClientResponse.class);
		when(clientResponse.getStatus()).thenReturn(geostoreErrorCode);
		doThrow(new UniformInterfaceException(clientResponse)).when(
				geostoreClient).updateResource(anyLong(),
				any(RESTResource.class));
		ClientResponse response = modifyLayer("243", new AddLayerRequest(
				"newlayer", LayerType.RASTER, "/foo", "/bar", "/foobar", 2, 1,
				1d, 3d, 10d, 11d));
		assertEquals(geostoreErrorCode, response.getStatus());
	}

	@Test
	public void testModifySettingToNullFail() throws Exception {
		testModifySettingToNullFail(new AddLayerRequest(null, LayerType.RASTER,
				"/foo", "/bar", "/foobar", 2, 1, 1d, 3d, 10d, 11d));
		testModifySettingToNullFail(new AddLayerRequest("newlayer", null,
				"/foo", "/bar", "/foobar", 2, 1, 1d, 3d, 10d, 11d));
		testModifySettingToNullFail(new AddLayerRequest("newlayer",
				LayerType.RASTER, null, "/bar", "/foobar", 2, 1, 1d, 3d, 10d,
				11d));
		testModifySettingToNullFail(new AddLayerRequest("newlayer",
				LayerType.RASTER, "/foo", null, "/foobar", 2, 1, 1d, 3d, 10d,
				11d));
		testModifySettingToNullFail(new AddLayerRequest("newlayer",
				LayerType.RASTER, "/foo", "/bar", null, 2, 1, 1d, 3d, 10d, 11d));
		testModifySettingToNullFail(new AddLayerRequest("newlayer",
				LayerType.RASTER, "/foo", "/bar", "/foobar", null, 1, 1d, 3d,
				10d, 11d));
		testModifySettingToNullFail(new AddLayerRequest("newlayer",
				LayerType.RASTER, "/foo", "/bar", "/foobar", 2, null, 1d, 3d,
				10d, 11d));
		testModifySettingToNullFail(new AddLayerRequest("newlayer",
				LayerType.RASTER, "/foo", "/bar", "/foobar", 2, 1, null, 3d,
				10d, 11d));
		testModifySettingToNullFail(new AddLayerRequest("newlayer",
				LayerType.RASTER, "/foo", "/bar", "/foobar", 2, 1, 1d, null,
				10d, 11d));
		testModifySettingToNullFail(new AddLayerRequest("newlayer",
				LayerType.RASTER, "/foo", "/bar", "/foobar", 2, 1, 1d, 3d,
				null, 11d));
		testModifySettingToNullFail(new AddLayerRequest("newlayer",
				LayerType.RASTER, "/foo", "/bar", "/foobar", 2, 1, 1d, 3d, 10d,
				null));
	}

	@Test
	public void testDeleteLayer() {
		String id = "13";
		ClientResponse response = deleteLayer(id);
		assertEquals(ClientResponse.Status.NO_CONTENT,
				response.getClientResponseStatus());
		try {
			response.getEntity(String.class);
			fail();
		} catch (UniformInterfaceException e) {
		}

		verify(geostoreClient).deleteResource(13L);
	}

	@Test
	public void testDeleteGeostoreClientError() {
		testDeleteGeostoreClientError(404);
		testDeleteGeostoreClientError(500);
	}

	private void testDeleteGeostoreClientError(int value) {
		ClientResponse clientResponse = mock(ClientResponse.class);
		when(clientResponse.getStatus()).thenReturn(value);
		doThrow(new UniformInterfaceException(clientResponse)).when(
				geostoreClient).deleteResource(anyLong());
		ClientResponse response = deleteLayer("243");
		assertEquals(value, response.getStatus());
	}

	private void testModifySettingToNullFail(AddLayerRequest layerModification) {
		ClientResponse response = modifyLayer("0", layerModification);
		assertEquals(ClientResponse.Status.BAD_REQUEST,
				response.getClientResponseStatus());
	}

	private String getAttribute(RESTResource resource,
			final Attributes attribute) {
		List<ShortAttribute> attributes = resource.getAttribute();
		return Iterables.find(attributes, new Predicate<ShortAttribute>() {

			@Override
			public boolean apply(ShortAttribute input) {
				return input.getName().equals(attribute.getName());
			}
		}).getValue();
	}

	@Test
	public void testGetLayer() throws Exception {
		mockGeostoreSearchAnswer(mockResourceList(mockResource(12L,
				"new_layer", LayerType.VECTOR)));

		// Check actual contents by expected path
		ClientResponse response = getLayerOk("12");

		LayerRepresentation layer = response
				.getEntity(LayerRepresentation.class);
		assertEquals("new_layer", layer.getName());
		assertEquals(LayerType.VECTOR, layer.getType());
	}

	@Test
	public void testGetUnexistingLayerGives404() throws Exception {
		mockGeostoreSearchAnswer(mockResourceList());

		ClientResponse response = getLayer("an-id-that-does-not-exist");
		assertEquals(ClientResponse.Status.NOT_FOUND,
				response.getClientResponseStatus());
	}

	private Resource mockResource(long id, String name, LayerType layerType) {
		Resource resource = mock(Resource.class);
		when(resource.getId()).thenReturn(id);
		when(resource.getName()).thenReturn(name);
		List<Attribute> attributes = createAttributeList(layerType, 1, 2, 1, 2,
				1, 1, "OrigDataDestPath", "DissMosaicPath", "MosaicPath");
		when(resource.getAttribute()).thenReturn(attributes);
		return resource;
	}

	private List<Attribute> createAttributeList(LayerType layerType, double x0,
			double x1, double y0, double y1, double pixelHeight,
			double pixelWidth, String origDataDestPath, String dissMosaicPath,
			String mosaicPath) {
		List<Attribute> ret = new ArrayList<Attribute>();
		ret.add(newAttribute(UNREDDLayer.Attributes.LAYERTYPE.getName(),
				layerType.toString()));
		ret.add(newAttribute(UNREDDLayer.Attributes.RASTERY1.getName(), y1));
		ret.add(newAttribute(UNREDDLayer.Attributes.RASTERY0.getName(), y0));
		ret.add(newAttribute(UNREDDLayer.Attributes.RASTERX1.getName(), x1));
		ret.add(newAttribute(UNREDDLayer.Attributes.RASTERX0.getName(), x0));
		ret.add(newAttribute(
				UNREDDLayer.Attributes.RASTERPIXELHEIGHT.getName(), pixelHeight));
		ret.add(newAttribute(UNREDDLayer.Attributes.RASTERPIXELWIDTH.getName(),
				pixelWidth));
		ret.add(newAttribute(UNREDDLayer.Attributes.ORIGDATADESTPATH.getName(),
				origDataDestPath));
		ret.add(newAttribute(UNREDDLayer.Attributes.DISSMOSAICPATH.getName(),
				dissMosaicPath));
		ret.add(newAttribute(UNREDDLayer.Attributes.MOSAICPATH.getName(),
				mosaicPath));

		return ret;
	}

	private ClientResponse getLayerOk(String id) {
		ClientResponse response = getLayer(id);
		assertEquals(ClientResponse.Status.OK,
				response.getClientResponseStatus());
		return response;
	}

	private ClientResponse getLayer(String id) {
		WebResource webResource = resource();
		ClientResponse response = webResource.path("layers/" + id)
				.type(MediaType.APPLICATION_JSON).get(ClientResponse.class);
		return response;
	}

	private ClientResponse createLayerOk(AddLayerRequest layer) {
		ClientResponse response = createLayer(layer);
		assertEquals(ClientResponse.Status.CREATED,
				response.getClientResponseStatus());
		return response;
	}

	private ClientResponse createLayer(AddLayerRequest layer) {
		WebResource webResource = resource();
		ClientResponse response = webResource.path("layers")
				.type(MediaType.APPLICATION_JSON).entity(layer)
				.post(ClientResponse.class);
		return response;
	}

	private ClientResponse modifyLayer(String id,
			AddLayerRequest layerModification) {
		WebResource webResource = resource();
		ClientResponse response = webResource.path("layers/" + id)
				.type(MediaType.APPLICATION_JSON).entity(layerModification)
				.put(ClientResponse.class);
		return response;
	}

	private ClientResponse getLayersOk() {
		WebResource webResource = resource();
		ClientResponse response = webResource.path("layers").get(
				ClientResponse.class);
		assertEquals(ClientResponse.Status.OK,
				response.getClientResponseStatus());
		return response;
	}

	private ClientResponse deleteLayer(String id) {
		WebResource webResource = resource();
		ClientResponse response = webResource.path("layers/" + id)
				.type(MediaType.APPLICATION_JSON).delete(ClientResponse.class);
		return response;
	}
}
