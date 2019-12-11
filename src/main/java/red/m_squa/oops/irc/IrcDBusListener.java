/*
 * Copyright (C) 2019 Molly Miller.
 *
 * This file is part of Oops.
 * 
 * Oops is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version.
 *
 * Oops is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Oops.  If not, see <https://www.gnu.org/licenses/>.
 */

package red.m_squa.oops.irc;

import org.freedesktop.dbus.connections.impl.DBusConnection;
import org.freedesktop.dbus.exceptions.DBusException;
import org.freedesktop.dbus.interfaces.DBusInterface;
import org.freedesktop.dbus.messages.DBusSignal;
import org.freedesktop.dbus.types.UInt64;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import red.m_squa.pircbotx.AddressableListener;

import red.m_squa.oops.DBusPath;

/**
 * IRC event listener which sends events to DBus.
 */
public abstract class IrcDBusListener extends AddressableListener
    implements DBusInterface {
    private static final Logger log =
        LoggerFactory.getLogger(IrcDBusListener.class);
    protected DBusPath path;
    protected DBusConnection conn;

    public IrcDBusListener(DBusConnection conn, DBusPath path) {
        this.conn = conn;
        this.path = path;
    }

    /* DBusInterface implementation */
    public String getObjectPath() {
        return this.path.getPath();
    }

    public boolean isRemote() {
        return false;
    }

    /* IRC signal handling */

    @FunctionalInterface
    public interface SignalGenerator {
        public DBusSignal generate(String path, UInt64 timestamp)
            throws DBusException;
    }

    protected void signal(SignalGenerator siggen) {
        DBusSignal sig;

        try {
            sig = siggen.generate(this.path.getPath(),
                    new UInt64(System.currentTimeMillis()));
        } catch (DBusException dbe) {
            log.error("Could not create DBus signal '%s':", dbe.getMessage());
            return;
        }

        this.conn.sendMessage(sig);
    }
}
