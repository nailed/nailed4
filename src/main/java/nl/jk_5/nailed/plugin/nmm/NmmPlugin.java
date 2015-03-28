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
package nl.jk_5.nailed.plugin.nmm;

import com.google.gson.Gson;
import com.google.inject.Inject;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.util.event.Subscribe;

import nl.jk_5.nailed.api.event.mappack.LoadMappacksEvent;

@Plugin(id = "Nailed|NMM", name = "Nailed Mappack Manager", version = "1.0.0")
public class NmmPlugin {

    public static final Gson gson = new Gson();

    @Inject
    private Logger logger;

    @Subscribe
    public void registerMappacks(LoadMappacksEvent event) {
        HttpClient httpClient = HttpClientBuilder.create().build();
        try {
            HttpGet request = new HttpGet("http://nmm.jk-5.nl/mappacks.json");
            HttpResponse response = httpClient.execute(request);
            MappacksList list = gson.fromJson(EntityUtils.toString(response.getEntity(), "UTF-8"), MappacksList.class);
            for (String mappack : list.mappacks) {
                event.register(new NmmMappack(mappack, this));
            }
            this.logger.info("Registered " + list.mappacks.length + " nmm mappacks");
        } catch (Exception e) {
            this.logger.warn("Was not able to fetch mappacks from nmm.jk-5.nl", e);
        }
    }

    public Logger getLogger() {
        return this.logger;
    }
}
