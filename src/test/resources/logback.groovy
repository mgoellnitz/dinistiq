/**
 *
 * Copyright 2015 Martin Goellnitz
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 */

// Log configuration for test setup
scan '3600 seconds'

String encoderPattern = '%-5level %logger{35}.%msg%n'

def appenders = []
appender('CONSOLE', ConsoleAppender) {
  encoder(PatternLayoutEncoder) {
    pattern = encoderPattern
  }
}
appenders.add('CONSOLE')

appender('FILE', RollingFileAppender) {
  file = "build/dinistiq-test.log"
  append = true
  rollingPolicy(TimeBasedRollingPolicy) {
    fileNamePattern = "${logDir}/${logFileName}-%d{yyyy-MM-dd_HH}.log"
    maxHistory = 7
  }
  encoder(PatternLayoutEncoder) {
    pattern = encoderPattern
  }
}
appenders.add('FILE')

root DEBUG, appenders
logger "dinistiq", DEBUG, appenders, false
