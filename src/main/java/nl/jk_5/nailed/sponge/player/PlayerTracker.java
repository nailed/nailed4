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
package nl.jk_5.nailed.sponge.player;

import com.google.common.base.Charsets;
import org.slf4j.Logger;
import org.spongepowered.api.entity.player.Player;
import org.spongepowered.api.event.entity.player.PlayerJoinEvent;
import org.spongepowered.api.event.entity.player.PlayerQuitEvent;
import org.spongepowered.api.util.event.Subscribe;

import nl.jk_5.nailed.sponge.Nailed;
import nl.jk_5.nailed.sponge.utils.SQLUtils;

import java.sql.*;
import java.util.UUID;

public class PlayerTracker {

    private final Nailed nailed;
    private final Logger logger;

    public PlayerTracker(Nailed nailed) {
        this.nailed = nailed;
        this.logger = this.nailed.getLogger();
        this.nailed.getGame().getEventManager().register(this.nailed, this);
    }

    @Subscribe
    public void joinServer(PlayerJoinEvent event) {
        final Player player = event.getPlayer();
        UUID offlineUuid = UUID.nameUUIDFromBytes(("OfflinePlayer:" + player.getName()).getBytes(Charsets.UTF_8));
        this.logger.info("Real uuid: " + player.getUniqueId());
        this.logger.info("Offline uuid: " + offlineUuid);
        final boolean online;
        if (player.getUniqueId().equals(offlineUuid)) {
            this.logger.info("Player is offline mode");
            online = false;
        } else {
            this.logger.info("Player is online mode");
            online = true;
        }

        //this.nailed.getGame().getAsyncScheduler().runTask(this.nailed, new Runnable() {
        new Runnable() {
            @Override
            public void run() {
                Connection conn = null;
                try {
                    conn = PlayerTracker.this.nailed.getDataSource().getConnection();
                    PreparedStatement stmt1 = conn.prepareStatement("SELECT COUNT(*) FROM users WHERE id=?");
                    stmt1.setString(1, player.getUniqueId().toString());
                    ResultSet rs1 = stmt1.executeQuery();
                    rs1.next();
                    boolean exists = rs1.getInt(1) == 1;
                    stmt1.close();
                    if (exists) {
                        PreparedStatement stmt2 = conn.prepareStatement("UPDATE users SET username = ?, \"last\" = ?, online = TRUE, \"times-online\" = \"times-online\" + 1 WHERE id = ?");
                        stmt2.setString(1, player.getName());
                        stmt2.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
                        stmt2.setString(3, player.getUniqueId().toString());
                        stmt2.execute();
                        stmt2.close();
                    } else {
                        PreparedStatement stmt2 = conn.prepareStatement("INSERT INTO users(id, \"online-mode\", username, online, \"times-online\") VALUES (?, ?, ?, TRUE, 1)");
                        stmt2.setString(1, player.getUniqueId().toString());
                        stmt2.setBoolean(2, online);
                        stmt2.setString(3, player.getName());
                        stmt2.execute();
                        stmt2.close();
                    }
                } catch (SQLException e) {
                    PlayerTracker.this.logger.warn("Was not able to execute the sql update", e);
                } finally {
                    SQLUtils.close(conn);
                }
            }
        //});
        }.run();
    }

    @Subscribe
    public void leaveServer(PlayerQuitEvent event) {
        final Player player = event.getPlayer();
        //this.nailed.getGame().getAsyncScheduler().runTask(this.nailed, new Runnable() {
        new Runnable() {
            @Override
            public void run() {
                Connection conn = null;
                try {
                    conn = PlayerTracker.this.nailed.getDataSource().getConnection();
                    PreparedStatement stmt = conn.prepareStatement("UPDATE users SET online = FALSE WHERE id = ?");
                    stmt.setString(1, player.getUniqueId().toString());
                    stmt.execute();
                    stmt.close();
                } catch (SQLException e) {
                    PlayerTracker.this.logger.warn("Was not able to execute the sql update", e);
                } finally {
                    SQLUtils.close(conn);
                }
            }
        //});
        }.run();
    }
}
