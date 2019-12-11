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

import java.util.regex.Pattern;

public class DBusPath extends org.freedesktop.dbus.DBusPath {
    private static Pattern PATH_COMPONENT_PATTERN =
        Pattern.compile("[a-zA-Z0-9_]+");
    private boolean startupDone = false;

    public static boolean isValidPathComponent(String comp) {
        return PATH_COMPONENT_PATTERN.matcher(comp).matches();
    }

    public DBusPath(String... comps) {
        super("");
        this.startupDone = true;
        for (int i = 0; i < comps.length; i++) {
            if (comps[i] == null || !isValidPathComponent(comps[i])) {
                throw new IllegalArgumentException(
                    "Malformed DBus object path component");
            }
        }

        super.setPath("/" + String.join("/", comps));
    }

    public DBusPath(String path) {
        super("");
        this.startupDone = true;
        this.setPath(path);
    }

    public DBusPath appendPath(String app) {
        return new DBusPath(this.getPath() + '/' + app);
    }

    private boolean isValidPath(String path) {
        String[] comps;

        if (path == null) {
            return false;
        } else if (path.length() == 0) {
            return false;
        } else if (path.charAt(0) != '/') {
            return false;
        } else if (path.length() > 1 && path.charAt(path.length() - 1) == '/') {
            return false;
        }

        comps = path.split("/");

        /* string starting with "/" will always split into at least
         * one component, the first of which will be empty. each
         * subsequent component must be non-empty */
        for (int i = 1; i < comps.length; i++) {
            if (comps[i].length() == 0) {
                return false;
            } else if (!isValidPathComponent(comps[i])) {
                return false;
            }
        }

        return true;
    }

    @Override
    public void setPath(String path) {
        if (this.startupDone && !this.isValidPath(path)) {
            throw new IllegalArgumentException("Malformed DBus object path");
        }

        super.setPath(path);
    }
}

