# Oops

Oops is a bidirectional DBus-to-IRC proxy. It translates IRC events into DBus
signals and performs IRC actions upon DBus method calls. Concurrent connections
to multiple distinct servers are supported.

## Building

Oops is implemented in Java 8, with IRC connectivity provided by
[PircBotX](https://github.com/pircbotx/pircbotx) and the
[`pircbotx-extras`](https://github.com/sysvinit/pircbotx-extras) library, and
DBus functionality is provided by
[dbus-java](https://github.com/hypfvieh/dbus-java). Note that `pircbotx-extras`
must be installed manually from the git repository at time of writing. Maven is
used for building otherwise; issue `mvn package` at the project root to create
a combined JAR file containing all dependency classes under
`target/oops-unified.jar`.

Oops is still under development, so the DBus API is not documented
at this time, however it is presently in production use by [Adélie
Linux](https://www.adelielinux.org) as part of their server monitoring
infrastructure.

## Licence

Oops is Free Software under the GPL version 3. See the [COPYING](COPYING) file
for the full licence text.

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or (at
    your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.
