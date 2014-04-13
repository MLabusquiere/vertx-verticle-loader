/*
 * Copyright 2014 Zenika
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zenika.vertx.app.starter;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.AsyncResultHandler;
import org.vertx.java.core.Future;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.logging.Logger;
import org.vertx.java.platform.Verticle;

/**
 * @author M. Labusquière
 */
public class AppStarter extends Verticle {

	private Logger logger;
	private JsonObject configuration;

	@Override
	public void start() {

		logger = container.logger();
		configuration = container.config();

		logger.info("Starting to innitialise the application application");

		JsonObject deployConfig = configuration.getObject("app_starter");

		JsonArray verticles = deployConfig.getArray("verticles");

		if(null != verticles)
			for(Object object :  verticles)	{
				JsonObject verticleConf = (JsonObject) object;
				if(isVerticleConf(verticleConf))
					deployVerticle(verticleConf);
			}

		JsonArray modules = deployConfig.getArray("mods");
		if(null != modules)
			for(Object object :  modules)	{
				JsonObject moduleConf = (JsonObject) object;
				if(isModuleConf(moduleConf))
					deployModule(moduleConf);
			}
		//TODO How using the future???????
	}

	private void deployVerticle(final JsonObject verticleInitConf) {

		final int instance = (verticleInitConf.containsField("instance")) ? verticleInitConf.getInteger("instance") : 1;

		container.deployVerticle(
				verticleInitConf.getString("main"),
				configuration.getObject(verticleInitConf.getString("conf")),
				instance,
				new AsyncResultHandler<String>() {

					@Override
					public void handle(final AsyncResult<String> asyncResult) {
						if (asyncResult.succeeded()) {
							logger.info("The verticle with the id " + asyncResult.result() + " has been deployed " + instance + " times with " + verticleInitConf.getString("conf") + " as configuration ");
						} else {
							container.logger().error("Error during the deployment of the module " + asyncResult.cause());
						}
					}

				}
		);
	}

	private void deployModule(final JsonObject moduleInitConf) {

		final int instance = (moduleInitConf.containsField("instance")) ? moduleInitConf.getInteger("instance") : 1;

		container.deployModule(
				moduleInitConf.getString("module_name"),
				configuration.getObject(moduleInitConf.getString("conf")),
				instance,
				new AsyncResultHandler<String>() {

					@Override
					public void handle(final AsyncResult<String> asyncResult) {
						if (asyncResult.succeeded()) {
							logger.info("The module with the id " + asyncResult.result() + " has been deployed " + instance + " times with " + moduleInitConf.getString("conf") + " as configuration ");
						} else {
							container.logger().error("Error during the deployment of the module " + asyncResult.cause());
						}
					}

				}
		);
	}

	private boolean isModuleConf(JsonObject moduleConf) {
		if( moduleConf.containsField("module_name"))
			return true;

		logger.error("You need to specify at least a module_name to load a module");
		return false;
	}

	private boolean isVerticleConf(JsonObject verticleInitConf) {
		if( verticleInitConf.containsField("main"))
			return true;

		logger.error("You need to specify at least a main to load a verticle");
		return false;
	}

}
