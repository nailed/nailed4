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

import com.google.common.base.Optional;
import com.google.common.collect.Maps;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import nl.jk_5.nailed.api.event.mappack.LoadMappacksEvent;
import nl.jk_5.nailed.api.mappack.Mappack;
import nl.jk_5.nailed.api.mappack.MappackRegistry;

import java.util.Collection;
import java.util.Map;

@NonnullByDefault
public class NailedMappackRegistry implements MappackRegistry {

    private final Nailed nailed;

    private Map<String, Mappack> mappacks = Maps.newHashMap();

    public NailedMappackRegistry(Nailed nailed) {
        this.nailed = nailed;
    }

    public void loadMappacks() {
        this.nailed.getLogger().info("Loading mappacks");
        this.mappacks = Maps.newHashMap();
        this.nailed.getEventManager().post(new LoadMappacksEvent(this));
    }

    @Override
    public Collection<Mappack> getMappacks() {
        return null;
    }

    @Override
    public Optional<Mappack> getByName(String name) {
        return null;
    }

    @Override
    public boolean register(Mappack mappack) {
        if (this.mappacks.containsKey(mappack.getName())) {
            return false;
        }
        this.mappacks.put(mappack.getName(), mappack);
        return true;
    }
}
