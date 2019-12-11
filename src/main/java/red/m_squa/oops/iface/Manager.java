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
import org.freedesktop.dbus.types.UInt32;

import red.m_squa.oops.except.ServerLoadError;
import red.m_squa.oops.except.ServerNotLoaded;

@DBusInterfaceName("red.m_squa.oops.Oops")
public interface Manager extends DBusInterface {
    public String[] GetServerNames();
    public void LoadServer(String name)
        throws ServerLoadError;
    public void DisconnectServer(String name)
        throws ServerNotLoaded;
    public void Shutdown();

    public class ServerLoaded extends DBusSignal {
	public final String server;
	public ServerLoaded(String path, String server)
	    throws DBusException {
	    super(path, server);
	    this.server = server;
	}
    }

    public class ServerStopped extends DBusSignal {
	public final String server;
        public final boolean crashed;
	public ServerStopped(String path, String server, boolean crashed)
	    throws DBusException {
	    super(path, server, crashed);
	    this.server = server;
            this.crashed = crashed;
	}
    }
}
