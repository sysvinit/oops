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

package red.m_squa.oops.iface;

import org.freedesktop.dbus.annotations.DBusInterfaceName;
import org.freedesktop.dbus.exceptions.DBusException;
import org.freedesktop.dbus.interfaces.DBusInterface;
import org.freedesktop.dbus.messages.DBusSignal;
import org.freedesktop.dbus.types.UInt64;

/**
 * Synthetic events occuring on a channel.
 */
@DBusInterfaceName("red.m_squa.oops.Server.Output.Channel")
public interface SyntheticOutput extends DBusInterface {

    public class AddressedMessage extends DBusSignal {
        public final UInt64 ts;
        public final String src, dest, msg;
        public AddressedMessage(String path, UInt64 ts, String src, String dest,
                String msg) throws DBusException {
            super(path, ts, src, dest, msg);
            this.ts = ts;
            this.src = src;
            this.dest = dest;
            this.msg = msg;
        }
    }
}


