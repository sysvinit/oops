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
 * Raw server output events.
 */
@DBusInterfaceName("red.m_squa.oops.Server.Output")
public interface Output extends DBusInterface {

    public class Connected extends DBusSignal {
        public final UInt64 ts;
        public Connected(String path, UInt64 ts)
            throws DBusException {
            super(path, ts);
            this.ts = ts;
        }
    }

    public class Disconnected extends DBusSignal {
        public final UInt64 ts;
        public Disconnected(String path, UInt64 ts)
            throws DBusException {
            super(path, ts);
            this.ts = ts;
        }
    }

    public class PrivMsg extends DBusSignal {
        public final UInt64 ts;
        public final String src, dest, msg;
        public PrivMsg(String path, UInt64 ts, String src, String dest,
                String msg) throws DBusException {
            super(path, ts, src, dest, msg);
            this.ts = ts;
            this.src = src;
            this.dest = dest;
            this.msg = msg;
        }
    }

    public class Action extends DBusSignal {
        public final UInt64 ts;
        public final String src, dest, msg;
        public Action(String path, UInt64 ts, String src, String dest,
                String msg) throws DBusException {
            super(path, ts, src, dest, msg);
            this.ts = ts;
            this.src = src;
            this.dest = dest;
            this.msg = msg;
        }
    }

    public class Notice extends DBusSignal {
        public final UInt64 ts;
        public final String src, dest, msg;
        public Notice(String path, UInt64 ts, String src, String dest,
                String msg) throws DBusException {
            super(path, ts, src, dest, msg);
            this.ts = ts;
            this.src = src;
            this.dest = dest;
            this.msg = msg;
        }
    }

    public class Join extends DBusSignal {
        public final UInt64 ts;
        public final String user, chan;
        public Join(String path, UInt64 ts, String user, String chan)
            throws DBusException {
            super(path, ts, user, chan);
            this.ts = ts;
            this.user = user;
            this.chan = chan;
        }
    }

    public class Part extends DBusSignal {
        public final UInt64 ts;
        public final String user, chan, reason;
        public Part(String path, UInt64 ts, String user, String chan,
                String reason) throws DBusException {
            super(path, ts, chan, user, reason);
            this.ts = ts;
            this.chan = chan;
            this.user = user;
            this.reason = reason;
        }
    }

    public class Quit extends DBusSignal {
        public final UInt64 ts;
        public final String user, reason;
        public final String[] chans;
        public Quit(String path, UInt64 ts, String user, String reason,
                String[] chans) throws DBusException {
            super(path, ts, user, reason, chans);
            this.ts = ts;
            this.user = user;
            this.reason =  reason;
            this.chans = chans;
        }
    }

    public class Kick extends DBusSignal {
        public final UInt64 ts;
        public final String src, chan, tgt, reason;
        public Kick(String path, UInt64 ts, String src, String chan,
                String tgt, String reason) throws DBusException {
            super(path, ts, src, chan, tgt, reason);
            this.ts = ts;
            this.src = src;
            this.chan = chan;
            this.tgt = tgt;
            this.reason = reason;
        }
    }

    public class Nick extends DBusSignal {
        public final UInt64 ts;
        public final String oldnick, newnick;
        public final String[] chans;
        public Nick(String path, UInt64 ts, String oldnick, String newnick,
                String[] chans) throws DBusException {
            super(path, ts, oldnick, newnick, chans);
            this.ts = ts;
            this.oldnick = oldnick;
            this.newnick = newnick;
            this.chans = chans;
        }
    }

    public class Mode extends DBusSignal {
        public final UInt64 ts;
        public final String src, chan, args;
        public Mode(String path, UInt64 ts, String src, String chan, String args)
            throws DBusException {
            super(path, ts, src, chan, args);
            this.ts = ts;
            this.src = src;
            this.chan = chan;
            this.args = args;
        }
    }

    public class Topic extends DBusSignal {
        public final UInt64 ts, olddate;
        public final String src, chan, newtopic, oldtopic;
        public Topic(String path, UInt64 ts, String src, String chan,
                String newtopic, String oldtopic, UInt64 olddate)
            throws DBusException {
            super(path, ts, src, chan, newtopic, oldtopic, olddate);
            this.ts = ts;
            this.olddate = olddate;
            this.src = src;
            this.chan = chan;
            this.newtopic = newtopic;
            this.oldtopic = oldtopic;
        }
    }

    public class Invite extends DBusSignal {
        public final UInt64 ts;
        public final String src, chan;
        public Invite(String path, UInt64 ts, String src, String chan)
            throws DBusException {
            super(path, ts, src, chan);
            this.ts = ts;
            this.src = src;
            this.chan = chan;
        }
    }
}
