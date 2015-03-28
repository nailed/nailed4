/*
 * This file is part of Nailed, licensed under the MIT License (MIT).
 *
 * Copyright (c) The Nailed Team <http://nailed.jk-5.nl>
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package nl.jk_5.nailed.sponge.mappack;

import com.google.inject.Inject;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.slf4j.Logger;
import org.spongepowered.api.Game;
import org.spongepowered.api.event.state.PreInitializationEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.service.ProviderExistsException;
import org.spongepowered.api.service.config.DefaultConfig;
import org.spongepowered.api.service.event.EventManager;
import org.spongepowered.api.util.event.Subscribe;

import nl.jk_5.nailed.api.mappack.MappackRegistry;

import java.io.File;
import java.io.IOException;

@Plugin(id = "nailed", name = "Nailed", version = "4.0.0")
public class Nailed {

    @Inject
    @DefaultConfig(sharedRoot = true)
    private File defaultConfig;

    @Inject
    @DefaultConfig(sharedRoot = true)
    private ConfigurationLoader<CommentedConfigurationNode> configManager;

    @Inject
    private Logger logger;

    @Inject
    private Game game;

    @Inject
    private EventManager eventManager;

    @Subscribe
    public void initialization(PreInitializationEvent event) {
        readConfig();

        NailedMappackRegistry mappackLoader = new NailedMappackRegistry(this);
        try {
            this.game.getServiceManager().setProvider(this, MappackRegistry.class, mappackLoader);
        } catch (ProviderExistsException e) {
            this.logger.error("Was not able to set the MappackLoader service");
        }
    }

    private void readConfig() {
        CommentedConfigurationNode config;
        try {
            if (!this.defaultConfig.exists()) {
                this.defaultConfig.createNewFile();
                config = this.configManager.load();
                config.setComment("Nailed base configuration");
                CommentedConfigurationNode mapLoader = config.getNode("mapLoader");
                mapLoader.setComment("MapLoader configuration");
                CommentedConfigurationNode lobbyMappack = mapLoader.getNode("lobbyMappack");
                lobbyMappack.setComment("The mappack for the lobby map");
                lobbyMappack.setValue("nailed/lobby");
                this.configManager.save(config);
            }
            config = this.configManager.load();
        } catch (IOException e) {
            this.logger.error("The default configuration could not be loaded or created!");
            return;
        }

        this.logger.info(config.getNode("mapLoader", "lobbyMappack").getString("nailed/lobby"));
    }

    public EventManager getEventManager() {
        return this.eventManager;
    }

    public Game getGame() {
        return this.game;
    }

    public Logger getLogger() {
        return this.logger;
    }
}
