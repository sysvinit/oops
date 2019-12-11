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

import red.m_squa.oops.except.AlreadyOnChannel;
import red.m_squa.oops.except.NotOnChannel;
import red.m_squa.oops.except.UserNotOnChannel;

@DBusInterfaceName("red.m_squa.Oops.Server.Input")
public interface Input extends DBusInterface {
    public void JoinChannel(String chan)
        throws AlreadyOnChannel;
    public void JoinChannel(String chan, String key)
        throws AlreadyOnChannel;
    public void PartChannel(String chan)
        throws NotOnChannel;
    public void PartChannel(String chan, String msg)
	throws NotOnChannel;
    public void SendMsg(String dest, String msg)
	throws NotOnChannel;
    public void SendAction(String dest, String msg)
	throws NotOnChannel;
    public void SendNotice(String dest, String msg)
        throws NotOnChannel;
    public void SendKick(String chan, String user)
	throws NotOnChannel, UserNotOnChannel;
    public void SendKick(String chan, String user, String reason)
	throws NotOnChannel, UserNotOnChannel;
    public void ChangeMode(String target, String mode)
	throws NotOnChannel;
    public void ChangeTopic(String target, String topic)
	throws NotOnChannel;
    public void SendQuit();
    public void SendQuit(String msg);
}
