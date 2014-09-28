/**
 * yamsLog is a program for real time multi sensor logging and 
 * supervision
 * Copyright (C) 2011, Per Öberg, 2012, Philipp Koschorrek, 
 *               2014  Emil Berg
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

#include "DataLogger.h"

#include <iostream>

#ifdef __GNUC__
#include <signal.h>
#include <unistd.h>
#include <cstdlib>
#include <execinfo.h>
#endif

#ifdef __GNUC__
void print_stack_trace() {
  void* array[10];
  size_t size;

  size = ::backtrace(array, 10);
  std::cerr << "Stack trace:" << std::endl;
  ::backtrace_symbols_fd(array, size, STDERR_FILENO);
  std::exit(1);
}

void signal_handler(int sig) {
  std::cerr << "Segmentation fault: signal " << sig << std::endl;
  print_stack_trace();
  std::exit(1);
}
#endif

int main(int argc, const char** argv) {
#ifdef __GNUC__
  // Install segfault handler. This will print the stack trace at seg fault.
  std::signal(SIGSEGV, signal_handler);
#endif
  DataLogger mylogger;
  try {
    return mylogger.run(argc, argv);
  } catch (std::exception& exc) {
    std::cerr << "Exception: " << exc.what() << std::endl;
#ifdef __GNUC__
    print_stack_trace();
#endif
  }
}
