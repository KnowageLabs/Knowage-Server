/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.
 *
 * Knowage is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Knowage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package it.eng.spagobi.tools.dataset.solr;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLEngineResult.HandshakeStatus;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.util.JavaBinCodec;
import org.apache.solr.common.util.NamedList;
import org.junit.Test;

import com.sun.net.httpserver.HttpServer;

import io.netty.buffer.ByteBufAllocator;
import io.netty.handler.ssl.JdkSslContext;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;

public class SolrClientRuntimeTest {

	@Test(timeout = 10000)
	public void httpClientReadsBinaryResponseWithoutHttp2Transport() throws Exception {
		SolrDocument document = new SolrDocument();
		document.setField("id", "item-1");
		SolrDocumentList documents = new SolrDocumentList();
		documents.setNumFound(1);
		documents.add(document);
		NamedList<Object> header = new NamedList<>();
		header.add("status", 0);
		NamedList<Object> response = new NamedList<>();
		response.add("responseHeader", header);
		response.add("response", documents);
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		try (JavaBinCodec codec = new JavaBinCodec()) {
			codec.marshal(response, output);
		}
		byte[] body = output.toByteArray();
		HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
		server.createContext("/solr/collection/select", exchange -> {
			exchange.getResponseHeaders().set("Content-Type", "application/octet-stream");
			exchange.sendResponseHeaders(200, body.length);
			try (OutputStream responseBody = exchange.getResponseBody()) {
				responseBody.write(body);
			}
		});
		server.start();
		try (HttpSolrClient client = new HttpSolrClient.Builder(
				"http://127.0.0.1:" + server.getAddress().getPort() + "/solr/collection").build()) {
			QueryResponse result = client.query(new SolrQuery("*:*"));

			assertEquals(0, result.getStatus());
			assertEquals(1, result.getResults().getNumFound());
			assertEquals("item-1", result.getResults().get(0).getFieldValue("id"));
		} finally {
			server.stop(0);
		}
	}

	@Test
	public void nettyStartsClientTlsWithoutNativeAccelerator() throws Exception {
		SslContext context = SslContextBuilder.forClient().build();

		assertTrue(context instanceof JdkSslContext);
		SSLEngine engine = context.newEngine(ByteBufAllocator.DEFAULT, "localhost", 443);
		try {
			engine.beginHandshake();
			assertEquals(HandshakeStatus.NEED_WRAP, engine.getHandshakeStatus());
		} finally {
			engine.closeOutbound();
		}
	}
}
