# Oops

Oops is a bidirectional DBus-to-IRC proxy. It translates IRC events into DBus
signals and performs IRC actions upon DBus method calls. Concurrent connections
to multiple distinct servers are supported.

## Building

Oops is implemented using Java 8, with IRC connectivity provided by
[PircBotX](https://github.com/pircbotx/pircbotx) and DBus functionality
provided by [dbus-java](https://github.com/hypfvieh/dbus-java). Maven is used
for building; issue `mvn package` at the project root to create a combined JAR
file containing all dependency code under `target/oops-unified.jar`.

Oops is still under development, so the DBus API is not documented at this time.

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
