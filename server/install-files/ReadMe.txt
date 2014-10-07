This directory holds configuration files
that may be necessary to set up yamslogd 


* monitrc - Example of monit file for supervision of yamslogd-process
* yamslogd.service - systemd service file for starting yamslogd on boot and writing PID to file (for monit)
* 99-yamsLog-Symlinks.rules - udev-rules to automatically map hardware to devicenames that are equal regardless of insertion order
