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

import com.google.common.util.concurrent.SettableFuture;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import nl.jk_5.nailed.api.mappack.Mappack;

import java.io.File;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.StandardOpenOption;

@NonnullByDefault
public class NmmMappack implements Mappack {

    private final String path;
    private final Logger logger;
    private File dir;
    //private MappackMetadata metadata;

    public NmmMappack(String path, NmmPlugin plugin) {
        this.path = path;
        this.logger = plugin.getLogger();
    }

    @Override
    public String getName() {
        return this.path;
    }

    @Override
    public void prepareWorld(File destination, SettableFuture<Void> future) {
        HttpClient httpClient = HttpClientBuilder.create().build();
        try {
            String mappack = this.path.split("/", 2)[1];
            HttpGet request = new HttpGet("http://nmm.jk-5.nl/" + this.path + "/versions.json");
            HttpResponse response = httpClient.execute(request);
            MappackInfo list = NmmPlugin.gson.fromJson(EntityUtils.toString(response.getEntity(), "UTF-8"), MappackInfo.class);

            HttpGet request2 = new HttpGet("http://nmm.jk-5.nl/" + this.path + "/" + mappack + "-" + list.latest + ".zip");
            HttpEntity response2 = httpClient.execute(request2).getEntity();
            if (response2 != null) {
                File mappackZip = new File(destination, "mappack.zip");
                try (ReadableByteChannel source = Channels.newChannel(response2.getContent());
                     FileChannel out = FileChannel.open(mappackZip.toPath(), StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE)) {
                    out.transferFrom(source, 0, Long.MAX_VALUE);
                }
                ZipUtils.extract(mappackZip, destination);
                mappackZip.delete();
                this.dir = destination;
                File dataDir = new File(destination, ".data");
                dataDir.mkdir();
                File metadataLocation = new File(dataDir, "game.xml");
                new File(destination, "game.xml").renameTo(metadataLocation);
                new File(destination, "scripts").renameTo(new File(dataDir, "scripts"));
                File worldsDir = new File(destination, "worlds");
                for (File f : worldsDir.listFiles()) {
                    f.renameTo(new File(destination, f.getName()));
                }
                worldsDir.delete();
                //metadata = XmlMappackMetadata.fromFile(metadataLocation);
                future.set(null);
            } else {
                future.setException(new RuntimeException("Got an empty response while downloading mappack " + this.path + " from nmm.jk-5.nl"));
            }
        } catch (Exception e) {
            future.setException(new RuntimeException("Was not able to download mappack " + this.path + " from nmm.jk-5.nl", e));
        }
    }

    /*@Nonnull
    @Override
    public MappackMetadata getMetadata() {
        return metadata;
    }

    @Nullable
    @Override
    public IMount getMappackMount() {
        return new DirectoryMount(new File(new File(dir, ".data"), "scripts"));
    }*/
}
