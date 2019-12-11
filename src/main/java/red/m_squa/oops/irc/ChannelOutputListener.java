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

import red.m_squa.pircbotx.AddressedMessageEvent;

import red.m_squa.oops.DBusPath;
import red.m_squa.oops.iface.SyntheticOutput;
import red.m_squa.oops.irc.IrcDBusListener;

public class ChannelOutputListener extends IrcDBusListener
    implements SyntheticOutput {

    public ChannelOutputListener(DBusConnection conn, DBusPath path) {
        super(conn, path);
    }

    /* IRC event handlers */

    @Override
    public void onAddressed(AddressedMessageEvent ev) {
        this.signal((path, timestamp) ->
                new SyntheticOutput.AddressedMessage(path, timestamp,
                        ev.getUser().getHostmask(), ev.getChannel().getName(),
                        ev.getAddressedMessage()));
    }
}
