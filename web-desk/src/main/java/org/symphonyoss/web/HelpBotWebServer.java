/*
 *
 *
 * Copyright 2016 The Symphony Software Foundation
 *
 * Licensed to The Symphony Software Foundation (SSF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package org.symphonyoss.web;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.symphonyoss.HelpBotConfig;
import org.symphonyoss.client.SymphonyClient;
import org.symphonyoss.session.HelpSessionImpl;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.MultiMap;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CorsHandler;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.handler.sockjs.SockJSHandler;
import io.vertx.ext.web.handler.sockjs.SockJSHandlerOptions;

public class HelpBotWebServer {

	private static Logger logger = LoggerFactory.getLogger(HelpBotWebServer.class);

	private SymphonyClient symClient;
	private HelpBotConfig config;

	public HelpBotWebServer(HelpBotConfig config, SymphonyClient symClient, HelpSessionListener hsl) {
		Vertx vertx = Vertx.vertx();
		this.symClient = symClient;
		RestVert restVert = new RestVert(hsl);
		this.config = config;
		vertx.deployVerticle(restVert);
	}

	class RestVert extends AbstractVerticle {

		private HelpSessionListener l;

		private Map<String, MultiMap> sessionRequestData = new HashMap<>();

		public RestVert(HelpSessionListener hsl) {
			this.l = hsl;
		}

		@Override
		public void start(Future<Void> fut) {
			Router router = Router.router(vertx);
			enableCORS(router);
			// This enabled reading of the body for all paths. Limit payload to
			// 5MB
			router.route().handler(BodyHandler.create().setBodyLimit(5 * 1024 * 1024));
			enableSockJS(router);
			enableStaticHandler(router);

			router.route("/").handler(routingContext -> {
				HttpServerResponse response = routingContext.response();
				response.putHeader("location", "/web/help.html").setStatusCode(302).end();
			});

			// this is the initiation point for sessions. Posted parameters are
			// used
			// for the context of the session (e.g. username, email, issue,
			// description, etc.)
			// With the exception of "name", these parameters are simply passed
			// through.
			router.route("/help").handler(routingContext -> {
				HttpServerRequest req = routingContext.request();
				String token = UUID.randomUUID().toString().replaceAll("-", "");
				sessionRequestData.put(token, req.params());
				logger.info("New Session Posted: " + token);
				HttpServerResponse response = routingContext.response();
				// We redirect to the "index.html" page, which parses the token
				// and uses it to
				// initiate the WS connection back to helpbot.
				response.putHeader("location", "/web/index.html?token=" + token).setStatusCode(302).end();
			});

			vertx.createHttpServer().requestHandler(router::accept).listen(config.getPort(), result -> {
				if (result.succeeded()) {
					fut.complete();
				} else {
					fut.fail(result.cause());
				}
			});
		}

		private void enableCORS(Router router) {
			CorsHandler corsHandler = CorsHandler.create("*");
			corsHandler.allowedMethod(HttpMethod.GET);
			corsHandler.allowedMethod(HttpMethod.POST);
			corsHandler.allowedMethod(HttpMethod.PUT);
			corsHandler.allowedMethod(HttpMethod.DELETE);
			corsHandler.allowedHeader("Content-Type");
			router.route().handler(corsHandler);
		}

		private void enableStaticHandler(Router router) {
			// StaticHandler staticHandler = StaticHandler.create("web",
			// this.getClass().getClassLoader());
			// TODO - use classpath

			StaticHandler staticHandler = StaticHandler.create("src/main/resources/HelpBot-Client");
			staticHandler.setCacheEntryTimeout(5);
			staticHandler.setFilesReadOnly(false);
			router.route("/web/*").handler(staticHandler);
		}

		private void enableSockJS(Router router) {
			SockJSHandlerOptions options = new SockJSHandlerOptions().setHeartbeatInterval(5000);
			SockJSHandler sockJSHandler = SockJSHandler.create(vertx, options);
			sockJSHandler.socketHandler(sockJSSocket -> {
				String name = null;
				MultiMap sessionData = null;
				String uri = sockJSSocket.uri();
				logger.info("New WS connection: " + uri);
				String token = getTokenFromURI(uri);
				if (token == null) {
					logger.info("Rejecting. Unable to get token from path: " + uri);
					sockJSSocket.close();
					return;
				}
				logger.info("New WebSocket Session: " + token);
				// We remove the token, you can only init session once.
				sessionData = sessionRequestData.remove(token);
				if (sessionData == null) {
					// Unknown session (details never posted)
					sockJSSocket.close();
					return;
				} else {
					name = sessionData.get(config.getMonikerParameter());
				}
				if (name == null || name.isEmpty()) {
					name = "Unknown";
				}

				HelpSessionImpl.init(sockJSSocket, sessionData, symClient, name,
						createdSession -> l.onHelpSessionInit(createdSession), (message, throwable) -> {
					throwable.printStackTrace();
					sockJSSocket.close();
				});

			});
			router.route("/ws/*").handler(sockJSHandler);
		}

	}

	private static String getTokenFromURI(String uriString) {
		try {
			URI uri = new URI(uriString);
			String query = uri.getQuery();
			String token = query.substring(query.indexOf('=') + 1, query.length());
			return token;
		} catch (Exception e) {
			// Invalid token
			return null;
		}
	}

}
