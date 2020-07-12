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
import org.freedesktop.dbus.interfaces.DBusInterface;
import org.freedesktop.dbus.messages.DBusSignal;

import red.m_squa.oops.except.NotOnChannel;
import red.m_squa.oops.except.UserNotOnChannel;

/**
 * Interface to server connection state.
 */
@DBusInterfaceName("red.m_squa.oops.Server")
public interface Server extends DBusInterface {
    public String[] GetChannelNames();
    public String[] GetChannelUsers(String channel)
        throws NotOnChannel;
    public String GetChannelMode(String channel)
        throws NotOnChannel;

    public boolean ChannelContainsUser(String channel, String user)
        throws NotOnChannel;

    /* check permission bits */
    public boolean UserIsRegular(String channel, String user)
        throws NotOnChannel, UserNotOnChannel;
    public boolean UserIsVoiced(String channel, String user)
        throws NotOnChannel, UserNotOnChannel;
    public boolean UserIsHalfOp(String channel, String user)
        throws NotOnChannel, UserNotOnChannel;
    public boolean UserIsOp(String channel, String user)
        throws NotOnChannel, UserNotOnChannel;
    public boolean UserIsSuperOp(String channel, String user)
        throws NotOnChannel, UserNotOnChannel;
    public boolean UserIsOwner(String channel, String user)
        throws NotOnChannel, UserNotOnChannel;

    /* combined permission check - halfops or higher */
    public boolean UserIsPrivd(String channel, String user)
        throws NotOnChannel, UserNotOnChannel;
}

