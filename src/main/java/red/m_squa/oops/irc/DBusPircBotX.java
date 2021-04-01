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

import java.util.Collection;
import java.util.Optional;

import org.pircbotx.Channel;
import org.pircbotx.Configuration;
import org.pircbotx.PircBotX;
import org.pircbotx.User;
import org.pircbotx.exception.DaoException;
import org.pircbotx.exception.NotReadyException;

import red.m_squa.oops.DBusPath;
import red.m_squa.oops.except.AlreadyOnChannel;
import red.m_squa.oops.except.NotOnChannel;
import red.m_squa.oops.except.UserNotOnChannel;
import red.m_squa.oops.iface.Input;
import red.m_squa.oops.iface.Output;
import red.m_squa.oops.iface.Server;
import red.m_squa.oops.iface.SyntheticOutput;

public class DBusPircBotX extends PircBotX
    implements Input, Server, Output, SyntheticOutput {
    /* Output and SyntheticOutput are required to make remote introspection
     * work. */

    private final DBusPath path;

    @FunctionalInterface
    private interface MemberListGetter {
        Collection<User> getUsers();
    }

    @FunctionalInterface
    private interface GetterLambda {
        MemberListGetter get(Channel c);
    }

    @FunctionalInterface
    private interface ChanSend {
        void send(String c, String m) throws NotOnChannel;
    }

    @FunctionalInterface
    private interface PrivSend {
        void send(String c, String m);
    }

    public DBusPircBotX(Configuration conf, DBusPath path) {
        super(conf);
        this.path = path;
    }

    private Channel getChannel(String name) throws NotOnChannel {
        Channel c;

        try {
            c = this.userChannelDao.getChannel(name);
        } catch (DaoException de) {
            throw new NotOnChannel(name);
        }

        return c;
    }

    private User getUser(String nick) {
        User u;

        try {
            u = this.userChannelDao.getUser(nick);
        } catch (DaoException de) {
            return null;
        }

        return u;
    }

    private User getChannelUser(String chan, String nick)
        throws NotOnChannel, UserNotOnChannel {
        Channel c;
        User u;

        c = this.getChannel(chan);
        u = this.getUser(nick);

        if (u == null || !u.getChannels().contains(c)) {
            throw new UserNotOnChannel(nick, chan);
        }

        return u;
    }

    private boolean channelPrivsCheck(MemberListGetter get, User u) {
        return get.getUsers().contains(u);
    }

    private boolean checkUserPrivs(GetterLambda lam, String channel,
            String user) throws NotOnChannel, UserNotOnChannel {
        Channel c;
        User u;
        MemberListGetter g;

        c = this.getChannel(channel);
        u = this.getChannelUser(channel, user);
        g = lam.get(c);

        return this.channelPrivsCheck(g, u);
    }

    private void maybeSendOnChannel(String dest, String msg, ChanSend ischan,
            PrivSend notchan) throws NotOnChannel {
        String info;

        info = this.getServerInfo().getISupportValue("CHANTYPES");

        if (info == null) {
            /* assume this is a channel if we can't distinguish otherwise */
            ischan.send(dest, msg);
        } else if (info.contains(dest.substring(0,1))) {
            /* on channel, so send to there */
            ischan.send(dest, msg);
        } else {
            /* not on channel, so attempt to send directly */
            notchan.send(dest, msg);
        }
    }


    /* DBusInterface implementation */
    public String getObjectPath() {
        return this.path.getPath();
    }

    public boolean isRemote() {
        return false;
    }

    /* Server interface implementation */
    public String GetNick() {
        return this.getNick();
    }

    public String[] GetChannelNames() {
        return this.userChannelDao.getAllChannels()
            .stream().map(c -> c.getName())
            .toArray(String[]::new);
    }

    public String[] GetChannelUsers(String channel) throws NotOnChannel {
        return this.getChannel(channel).getUsers().stream()
            .map(u -> u.getHostmask()).toArray(String[]::new);
    }

    public String GetChannelMode(String channel) throws NotOnChannel {
        String ret;
        boolean done;

        ret = null;
        done = true;

        do {
            try {
                ret = this.getChannel(channel).getMode();
            } catch (NotReadyException nre) {
                done = false;
            }
        } while (!done);

        return ret;
    }

    public boolean ChannelContainsUser(String channel, String user)
        throws NotOnChannel {
        return this.getChannel(channel).getUsersNicks().contains(user);
    }

    public boolean UserIsRegular(String channel, String user) throws
        NotOnChannel, UserNotOnChannel {
        return this.checkUserPrivs((c -> c::getNormalUsers), channel, user);
    }

    public boolean UserIsVoiced(String channel, String user) throws
        NotOnChannel, UserNotOnChannel {
        return this.checkUserPrivs((c -> c::getVoices), channel, user);
    }

    public boolean UserIsHalfOp(String channel, String user) throws
        NotOnChannel, UserNotOnChannel {
        return this.checkUserPrivs((c -> c::getHalfOps), channel, user);
    }

    public boolean UserIsOp(String channel, String user) throws
        NotOnChannel, UserNotOnChannel {
        return this.checkUserPrivs((c -> c::getOps), channel, user);
    }

    public boolean UserIsSuperOp(String channel, String user) throws
        NotOnChannel, UserNotOnChannel {
        return this.checkUserPrivs((c -> c::getSuperOps), channel, user);
    }

    public boolean UserIsOwner(String channel, String user) throws
        NotOnChannel, UserNotOnChannel {
        return this.checkUserPrivs((c -> c::getOwners), channel, user);
    }

    public boolean UserIsPrivd(String channel, String user) {
        return !this.UserIsRegular(channel, user) &&
            !this.UserIsVoiced(channel, user);
    }

    /* Input interface implementation */

    private void doJoin(String chan, Optional<String> key)
        throws AlreadyOnChannel {
        if (this.userChannelDao.containsChannel(chan)) {
            throw new AlreadyOnChannel(chan);
        }

        key.map(k ->
                { this.outputIRC.joinChannel(chan, k); return null; })
            .orElseGet(() ->
                    { this.outputIRC.joinChannel(chan); return null; });
    }

    public void JoinChannel(String chan) throws AlreadyOnChannel {
        this.doJoin(chan, Optional.empty());
    }

    public void JoinChannel(String chan, String key) throws AlreadyOnChannel {
        this.doJoin(chan, Optional.of(key));
    }

    public void PartChannel(String chan) throws NotOnChannel {
        this.getChannel(chan).send().part();
    }

    public void PartChannel(String chan, String msg) throws NotOnChannel {
        this.getChannel(chan).send().part(msg);
    }

    public void SendMsg(String dest, String msg) throws NotOnChannel {
        this.maybeSendOnChannel(dest, msg,
                ((d,m) -> this.getChannel(d).send().message(m)),
                ((d,m) -> this.sendIRC().message(d, m)));
    }

    public void SendAction(String dest, String msg) throws NotOnChannel {
        this.maybeSendOnChannel(dest, msg,
                ((d,m) -> this.getChannel(d).send().action(m)),
                ((d,m) -> this.sendIRC().action(d, m)));
    }

    public void SendNotice(String dest, String msg) throws NotOnChannel {
        this.maybeSendOnChannel(dest, msg,
                ((d,m) -> this.getChannel(d).send().notice(m)),
                ((d,m) -> this.sendIRC().notice(d, m)));
    }

    private void doKick(String chan, String user, Optional<String> reason)
        throws NotOnChannel, UserNotOnChannel {
        Channel c;
        User u;

        c = this.getChannel(chan);
        u = this.getChannelUser(chan, user);

        reason.map(r -> { c.send().kick(u, r); return null; })
            .orElseGet(() -> { c.send().kick(u); return null; });
    }

    public void SendKick(String chan, String user) throws NotOnChannel,
        UserNotOnChannel {
        this.doKick(chan, user, Optional.empty());
    }

    public void SendKick(String chan, String user, String reason)
        throws NotOnChannel, UserNotOnChannel {
        this.doKick(chan, user, Optional.of(reason));
    }

    public void ChangeMode(String target, String mode)
        throws NotOnChannel {
        this.getChannel(target).send().setMode(mode);
    }

    public void ChangeTopic(String target, String topic)
        throws NotOnChannel {
        this.getChannel(target).send().setTopic(topic);
    }

    private void doQuit(Optional<String> msg) {
        msg.map(r -> { this.outputIRC.quitServer(r); return null; })
            .orElseGet(() -> { this.outputIRC.quitServer(); return null; });
    }

    public void SendQuit() {
        this.doQuit(Optional.empty());
    }

    public void SendQuit(String msg) {
        this.doQuit(Optional.of(msg));
    }
}
