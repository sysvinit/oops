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
import org.freedesktop.dbus.types.UInt64;

import org.pircbotx.Channel;
import org.pircbotx.User;
import org.pircbotx.hooks.events.ActionEvent;
import org.pircbotx.hooks.events.ConnectEvent;
import org.pircbotx.hooks.events.DisconnectEvent;
import org.pircbotx.hooks.events.JoinEvent;
import org.pircbotx.hooks.events.KickEvent;
import org.pircbotx.hooks.events.InviteEvent;
import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.events.ModeEvent;
import org.pircbotx.hooks.events.NickChangeEvent;
import org.pircbotx.hooks.events.NoticeEvent;
import org.pircbotx.hooks.events.PartEvent;
import org.pircbotx.hooks.events.QuitEvent;
import org.pircbotx.hooks.events.PrivateMessageEvent;
import org.pircbotx.hooks.events.TopicEvent;
import org.pircbotx.snapshot.UserSnapshot;

import red.m_squa.oops.DBusPath;
import red.m_squa.oops.iface.Output;
import red.m_squa.oops.irc.IrcDBusListener;

public class ServerOutputListener extends IrcDBusListener
    implements Output {
    private final String initmodes;

    public ServerOutputListener(DBusConnection conn, DBusPath path,
            String initmodes) {
        super(conn, path);
        this.initmodes = initmodes;
    }

    /* IRC event handlers */

    @Override
    public void onConnect(ConnectEvent ev) {
        this.signal((path, timestamp) -> new Output.Connected(path, timestamp));

        /* set initial user modes upon connection */
        if (this.initmodes != null) {
            ev.getBot().sendIRC().mode(ev.getBot().getNick(), this.initmodes);
        }
    }

    @Override
    public void onDisconnect(DisconnectEvent ev) {
	this.signal((path, timestamp) -> new Output.Disconnected(path, timestamp));
    }

    @Override
    public void onMessage(MessageEvent ev) {
        this.signal((path, timestamp) -> new Output.PrivMsg(path, timestamp,
                ev.getUser().getHostmask(), ev.getChannel().getName(),
                ev.getMessage()));
    }

    @Override
    public void onPrivateMessage(PrivateMessageEvent ev) {
        this.signal((path, timestamp) -> new Output.PrivMsg(path, timestamp,
                ev.getUser().getHostmask(), "",
                ev.getMessage()));
    }

    @Override
    public void onAction(ActionEvent ev) {
        this.signal((path, timestamp) -> new Output.Action(path, timestamp,
                ev.getUser().getHostmask(), ev.getChannel().getName(),
                ev.getMessage()));
    }

    @Override
    public void onNotice(NoticeEvent ev) {
        this.signal((path, timestamp) -> new Output.Notice(path, timestamp,
                ev.getUser().getHostmask(), ev.getChannel().getName(),
                ev.getMessage()));
    }

    @Override
    public void onJoin(JoinEvent ev) {
        this.signal((path, timestamp) -> new Output.Join(path, timestamp,
                ev.getUser().getHostmask(), ev.getChannel().getName()));
    }

    @Override
    public void onPart(PartEvent ev) {
        this.signal((path, timestamp) -> new Output.Part(path, timestamp,
                ev.getUser().getHostmask(), ev.getChannel().getName(),
                ev.getReason()));
    }

    @Override
    public void onQuit(QuitEvent ev) {
        this.signal((path, timestamp) -> new Output.Quit(path, timestamp,
                ev.getUser().getHostmask(), ev.getReason(),
                ev.getUserChannelDaoSnapshot().getChannels(ev.getUser())
                .stream().map(c -> c.getName())
                .toArray(String[]::new)));
    }

    @Override
    public void onKick(KickEvent ev) throws Exception {
        this.signal((path, timestamp) -> new Output.Kick(path, timestamp,
                ev.getUser().getHostmask(), ev.getChannel().getName(),
                ev.getRecipient().getHostmask(), ev.getReason()));
    }

    @Override
    public void onInvite(InviteEvent ev) throws Exception {
        this.signal((path, timestamp) -> new Output.Invite(path, timestamp,
                ev.getUser().getHostmask(), ev.getChannel()));
    }

    @Override
    public void onNickChange(NickChangeEvent ev) {
        this.signal((path, timestamp) -> new Output.Nick(path, timestamp,
                ev.getOldNick(), ev.getNewNick(),
                ev.getBot().getUserChannelDao().getChannels(ev.getUser())
                .stream().map(c -> c.getName())
                .toArray(String[]::new)));
    }

    @Override
    public void onMode(ModeEvent ev) {
        this.signal((path, timestamp) -> new Output.Mode(path, timestamp,
                ev.getUser().getHostmask(), ev.getChannel().getName(),
                ev.getMode()));
    }

    @Override
    public void onTopic(TopicEvent ev) {
        this.signal((path, timestamp) -> new Output.Topic(path, timestamp,
                ev.getUser().getHostmask(), ev.getChannel().getName(),
                ev.getTopic(), ev.getOldTopic(), new UInt64(ev.getDate())));
    }
}
