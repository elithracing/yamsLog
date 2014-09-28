/**
 * yamsLog is a program for real time multi sensor logging and 
 * supervision
 * Copyright (C) 2012 Philipp Koschorrek, 2014  Emil Berg
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

/*
 * This class is made to listen to quit commands from the keyboard.
 * If a quit command is found from the keyboard it signals and exit callback function.
 */

#include "KeyboardInputService.h"

#include <iostream>
#include <algorithm>

const std::vector<std::string> quit_commands = {
  "q",
  "quit",
  "end",
  "exit",
  "stop"
};

KeyboardInputService::KeyboardInputService(boost::function<void()> exit_callback_func) : exit_callback_func_(exit_callback_func) {
}

bool KeyboardInputService::initialize() {
  return true;
}

void KeyboardInputService::execute() {
  std::string input_str;
  std::cin >> input_str;
  std::transform(input_str.begin(), input_str.end(), input_str.begin(), ::tolower);

  if (std::find(quit_commands.begin(), quit_commands.end(), input_str) != quit_commands.end()) {
    exit_callback_func_();
    set_running(false);
  }
}

void KeyboardInputService::finalize() {
}
