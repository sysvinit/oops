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

package red.m_squa.oops;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import javax.net.ssl.SSLSocketFactory;

import org.freedesktop.dbus.connections.impl.DBusConnection;
import org.freedesktop.dbus.exceptions.DBusException;
import org.freedesktop.dbus.messages.DBusSignal;

import org.pircbotx.Configuration;
import org.pircbotx.MultiBotManager;
import org.pircbotx.PircBotX;
import org.pircbotx.UtilSSLSocketFactory;
import org.pircbotx.delay.StaticDelay;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.managers.SequentialListenerManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;

import red.m_squa.oops.DBusPath;
import red.m_squa.oops.except.BadServerName;
import red.m_squa.oops.except.DefinitionReadError;
import red.m_squa.oops.except.IncompleteServerDefinition;
import red.m_squa.oops.except.MalformedServerDefinition;
import red.m_squa.oops.except.ServerExists;
import red.m_squa.oops.except.ServerLoadError;
import red.m_squa.oops.except.ServerNotFound;
import red.m_squa.oops.except.ServerNotLoaded;
import red.m_squa.oops.iface.Manager;
import red.m_squa.oops.irc.ChannelOutputListener;
import red.m_squa.oops.irc.DBusPircBotX;
import red.m_squa.oops.irc.ServerOutputListener;


public class Oops implements Manager {
    private static final String OOPS_BUSNAME = "red.m_squa.Oops";
    private static final String OOPS_PATH = "/red/m_squa/Oops";
    private static final Logger log = LoggerFactory.getLogger(Oops.class);
    private static final long UNEXPORT_DELAY = 5000;

    private enum OopsState {
        RUNNING,
        STOPPING
    };

    private DBusPath path;
    private DBusConnection conn;
    private MultiBotManager manager;
    private BiMap<String, Integer> botnames;
    private Object statelock;
    private OopsState state;
    private File confdir;
    private CountDownLatch latch;

    public Oops(File confdir, DBusConnection conn, DBusPath path) {
        this.confdir = confdir;
        this.conn = conn;
        this.path = path;

        this.botnames = HashBiMap.create();
        this.manager = new InternalBotManager();
        this.statelock = new Object();
        this.state = OopsState.RUNNING;
        this.latch = new CountDownLatch(1);
    }

    public void go() {
        boolean again;

        log.info("Starting bot manager...");
        this.manager.start();

        do {
            again = false;

            try {
                this.latch.await();
            } catch (InterruptedException ie) {
                again = true;
                log.debug("Interrupted when waiting on latch");
            }
        } while (again);

        log.info("Bot manager terminated");
    }

    public boolean isRemote() {
        return false;
    }

    public String getObjectPath() {
        return this.path.getPath();
    }

    private String getStringKey(Properties prop, String name)
        throws IncompleteServerDefinition {
        String res;

        res = prop.getProperty(name);

        if (res == null) {
            log.warn("Missing value for key '%s'", name);
            throw new IncompleteServerDefinition("missing " + name);
        }

        log.debug("Setting configuration key '%s' to value '%s'", name, res);
        return res;
    }

    private int getIntKey(Properties prop, String name)
        throws IncompleteServerDefinition, MalformedServerDefinition {
        String str;
        int ret;
        

        str = prop.getProperty(name);

        if (str == null) {
            log.warn("Missing value for key '%s'", name);
            throw new IncompleteServerDefinition("missing " + name);
        }

        try {
            ret = Integer.parseUnsignedInt(str);
        } catch (NumberFormatException nfe) {
            log.warn("Malformed integer value for key '%s'", name);
            throw new MalformedServerDefinition("invalid integer " + str);
        }

        log.debug("Setting configuration key '%s' to value '%d'", name, ret);
        return ret;
    }

    public void LoadServer(String name) throws ServerLoadError {
        ChannelOutputListener col;
        Configuration.Builder conf;
        DBusPath newpath;
        DBusPircBotX bot;
        File cfgfile;
        FileInputStream fis;
        InetAddress source;
        Properties srv;
        SequentialListenerManager slm;
        ServerOutputListener sol;
        String prop, initmodes;
        String[] split;
        boolean defined;

        log.info("Loading server definition for server: " + name);

        if (!DBusPath.isValidPathComponent(name)) {
            log.warn("Attempting to load server with invalid name: " + name);
            throw new BadServerName(name);
        }

        defined = false;
        synchronized (this.statelock) {
            if (this.botnames.containsKey(name)) {
                defined = true;
            }
        }

        if (defined) {
            log.warn("Attempting to load already-defined server: " + name);
            throw new ServerExists(name);
        }

        cfgfile = new File(this.confdir, name);
        srv = new Properties();

        try {
            fis = new FileInputStream(cfgfile);
        } catch (FileNotFoundException fnfe) {
            log.warn("Server definition not found: " + name);
            throw new ServerNotFound(name);
        }

        try {
            srv.load(fis);
        } catch (IOException ioe) {
            log.warn("Could not read server definition: " + name);
            throw new DefinitionReadError(name);
        } catch (IllegalArgumentException iae) {
            log.warn("Server definition file malformed: " + name);
            throw new MalformedServerDefinition(name);
        }

        try {
            fis.close();
        } catch (IOException ioe) {
            log.warn("I/O error when closing server definition file for: " +
                    name);
        }

        conf = new Configuration.Builder();

        /* default values */
        conf.setAutoReconnectDelay(new StaticDelay(10000));
        conf.setAutoNickChange(true);

        conf.setName(this.getStringKey(srv, "nick"));
        conf.setLogin(this.getStringKey(srv, "ircname"));
        conf.setRealName(this.getStringKey(srv, "realname"));
        conf.addServer(this.getStringKey(srv, "server"),
                this.getIntKey(srv, "port"));

        if (srv.getProperty("insecuressl") != null) {
            log.debug("Enabling insecure TLS connection for server: " + name);
            conf.setSocketFactory(new UtilSSLSocketFactory().
                    trustAllCertificates());
        } else if (srv.getProperty("ssl") != null) {
            log.debug("Enabling TLS connection for server: " + name);
            conf.setSocketFactory(SSLSocketFactory.getDefault());
        }

        if (srv.getProperty("source-address") != null) {
            try {
                source = InetAddress.getByName(
                    srv.getProperty("source-address"));
            } catch (UnknownHostException uhe) {
                log.warn("Could not resolve source address for server: " +
                        name);
                throw new MalformedServerDefinition("source address not found");
            }

            log.debug("Setting source address to '%s' for server: '%s'",
                    source, name);
            conf.setLocalAddress(source);
        }

        if (srv.getProperty("server-password") != null) {
            log.debug("Setting server password for server: " + name);
            conf.setServerPassword(srv.getProperty("server-password"));
        }

        if (srv.getProperty("nickserv-password") != null) {
            log.debug("Setting nickserv password for server: " + name);
            conf.setNickservDelayJoin(true);
            conf.setNickservPassword(srv.getProperty("nickserv-password"));
        }

        initmodes = null;
        if (srv.getProperty("usermodes") != null) {
            log.debug("Setting inital connection modes for server: " + name);
            initmodes = srv.getProperty("usermodes");
        }

        if (srv.getProperty("autojoin") != null) {
            prop = srv.getProperty("autojoin");

            log.debug("Processing autojoin list for server: " + name);

            for (String s: prop.split("[,\\s]+")) {
                if (s.contains(":")) {
                    split = s.split(":");
                    if (split.length != 2) {
                        log.warn("Malformed autojoin channel specification " +
                                "for serer: " + name);
                        throw new MalformedServerDefinition(
                            String.format("bad channel definition: '%s'",
                                    prop));
                    } else {
                        conf.addAutoJoinChannel(split[0], split[1]);
                    }
                } else {
                    conf.addAutoJoinChannel(s);
                }
            }
        }

        newpath = this.path.appendPath(name);
        sol = new ServerOutputListener(this.conn, newpath, initmodes);
        col = new ChannelOutputListener(this.conn, newpath);

        slm = SequentialListenerManager.newDefault();
        slm.addListenerSequential(sol);
        slm.addListenerSequential(col);

        conf.setListenerManager(slm);

        log.debug("Creating bot object for server: " + name);
        bot = new DBusPircBotX(conf.buildConfiguration(), newpath);

        try {
            this.conn.exportObject(newpath.getPath(), bot);
        } catch (DBusException dbe) {
            log.warn("Could not export object to bus for server: " + name);
            throw new ServerLoadError("could not export object to DBus");
        }

        synchronized (statelock) {
            log.info("Starting bot for server: " + name);
            this.manager.addBot(bot);
            this.botnames.put(name, bot.getBotId());
        }

        try {
            this.conn.sendMessage(new
                    Manager.ServerLoaded(this.path.getPath(), name));
        } catch (DBusException dbe) {
            log.warn("Could not signal server start for server: " + name);
            throw new ServerLoadError("could not send signal to DBus");
        }
    }

    public void DisconnectServer(String name) {
        PircBotX bot;
        boolean serverfound;
        int id;

        log.info("Stopping bot for server: " + name);

        serverfound = false;
        synchronized (this.statelock) {
            if (this.botnames.containsKey(name)) {
                serverfound = true;
                id = this.botnames.get(name);
                bot = this.manager.getBotById(id);
                bot.stopBotReconnect();
                bot.sendIRC().quitServer("disconnecting");
            }
        }

        if (!serverfound) {
            log.warn("Server not loaded: " + name);
            throw new ServerNotLoaded(name);
        }
    }

    public String[] GetServerNames() {
        String[] ret;

        log.debug("Returning list of loaded servers");
        synchronized (this.statelock) {
            ret = this.botnames.keySet().toArray(new String[0]);
        }

        return ret;
    }

    public void Shutdown() {
        log.info("Shutting down all servers");

        synchronized (statelock) {
            this.manager.stop("shutting down");

            /* either we have no in-flight connections (in which case we can
             * safely fire the trigger for closing the connection in another
             * thread), or set our state flag to tell the bot exit handler to
             * pull the trigger instead */
            if (this.botnames.size() == 0) {
                this.latch.countDown();
            } else {
                this.state = OopsState.STOPPING;
            }
        }
    }

    public static void main(String[] args) {
        File dir;
        DBusConnection conn;
        DBusPath path;
        Oops oops;

        if (args.length == 0) {
            log.error("No configuration directory specified");
            System.exit(1);
        }

        dir = new File(args[0]);
        path = new DBusPath(OOPS_PATH);

        if (!dir.exists()) {
            log.error("Configuration directory does not exist");
            System.exit(1);
        } else if (!dir.isDirectory()) {
            log.error("Configuration directory is not a directory");
            System.exit(1);
        }

        log.debug("Connecting to DBus...");

        try {
            conn = DBusConnection.getConnection(
        	DBusConnection.DBusBusType.SESSION);
        } catch (DBusException dbe) {
            log.error("Could not connect to bus: " + dbe.getMessage());
            System.exit(1);
            return;
        }

        log.debug("Connected to bus successfully");
        oops = new Oops(dir, conn, path);
        log.debug("Exporting object to bus...");

        try {
            conn.exportObject(path.getPath(), oops);
        } catch (DBusException dbe) {
            log.error("Could not export object to bus:" + dbe.getMessage());
            System.exit(1);
        }

        log.debug("Object exported to bus.");
        log.debug("Requesting bus name: " + OOPS_BUSNAME);

        try {
            conn.requestBusName(OOPS_BUSNAME);
        } catch (DBusException dbe) {
            log.error("Could not request bus name \"%s\": %s",
        	    OOPS_BUSNAME, dbe.getMessage());
            System.exit(1);
            return;
        }

        log.debug("Bus name assigned");
        oops.go();

        log.debug("Unexporting object to bus");
        conn.unExportObject(path.getPath());

        log.debug("Disconnecting from bus");
        conn.disconnect();
        log.debug("Disconnected from bus");

        System.exit(0);
    }

    private class InternalBotManager extends MultiBotManager {
        public InternalBotManager() {
            super();
        }

        /* adapted from PircBotX sources */
        @Override
        protected ListenableFuture<Void> startBot(final PircBotX bot) {
            Preconditions.checkNotNull(bot, "Bot cannot be null");
            ListenableFuture<Void> future = botPool.submit(new BotRunner(bot));
            synchronized (runningBotsLock) {
                runningBots.put(bot, future);
                runningBotsNumbers.put(bot, bot.getBotId());
            }
            Futures.addCallback(future, new CallbackHack(bot), MoreExecutors.directExecutor());
            return future;
        }


        private class CallbackHack extends MultiBotManager.BotFutureCallback {
            public CallbackHack(final PircBotX bot) {
                super(bot);
            }

            @Override
            public void onSuccess(Void result) {
                log.debug("Bot #" + bot.getBotId() + " finished");
                remove(false);
            }

            @Override
            public void onFailure(Throwable t) {
                log.error("Bot exited with Exception", t);
                remove(true);
            }

            private void remove(boolean wascrash) {
                boolean done;
                String name;
                DBusPath subpath;
                super.remove();

                done = false;
                synchronized (Oops.this.statelock) {
                    name = Oops.this.botnames.inverse().remove(
                        this.bot.getBotId());

                    if (Oops.this.state == OopsState.STOPPING &&
                            Oops.this.botnames.size() == 0) {
                        done = true;
                    }
                }

                /* unexport object, send signal, tear down dbus connection */
                subpath = Oops.this.path.appendPath(name);

                log.info("Stopped server: " + name);

                try {
                    Oops.this.conn.sendMessage(
                        new Manager.ServerStopped(
                            Oops.this.path.getPath(), name, wascrash));

                    try {
                        Thread.sleep(Oops.UNEXPORT_DELAY);
                    } catch (InterruptedException ie) {
                        /* no recovery possible here */
                    }

                    Oops.this.conn.unExportObject(subpath.getPath());
                } catch (DBusException dbe) {
                    log.error("Caught exception when sending server stop " +
                            "signal to bus: " + dbe);
                }

                if (done) {
                    Oops.this.latch.countDown();
                }
            }
        }
    }
}
