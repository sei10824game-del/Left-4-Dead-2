#!/bin/bash
# Headless runClient smoke helper (Xvfb :99 + xdotool). Usage: tools/smoke_client.sh start | shot NAME | cmd "slash command (no /)"
# Window is 854x480 at offset 213,120. First run: click Continue(637,563) > Singleplayer(637,355) > Game Mode x2 (638,295) > Create(478,563).
export DISPLAY=:99
S=${SHOTS:-/tmp}
case $1 in
  start) (Xvfb :99 -screen 0 1280x720x24 &) ; sleep 2; ./gradlew --no-daemon runClient ;;
  shot)  import -window root -crop 854x480+213+120 $S/$2.png ;;
  cmd)   xdotool key slash; sleep .4; xdotool type --delay 10 "$2"; xdotool key Return ;;
esac
