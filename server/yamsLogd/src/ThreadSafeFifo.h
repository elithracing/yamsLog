/**
 * yamsLog is a program for real time multi sensor logging and 
 * supervision
 * Copyright (C) 2014  Emil Berg
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

#ifndef THREADSAFEFIFO_H_
#define THREADSAFEFIFO_H_

#include <stddef.h>
#include <cstdbool>
#include <mutex>
#include <queue>

/**
 * A FIFO implementation with Thread-safe operations.
**/
template<class T> class ThreadSafeFifo {
 public:
  ThreadSafeFifo() = default;
  ~ThreadSafeFifo() = default;

  // Returns the first element, without modifying the queue
  T front() {
    queue_mutex_.lock();
    T ret = queue_.front();
    queue_mutex_.unlock();
    return ret;
  }

  // Adds an element in the back of the queue
  void push(const T& new_element) {
    queue_mutex_.lock();
    queue_.push(new_element);
    queue_mutex_.unlock();
  }

  // Removes the fist element of the queue and returns it
  T pop() {
    if (!queue_.empty()) {
      queue_mutex_.lock();
      T ret = queue_.front();
      queue_.pop();
      queue_mutex_.unlock();
      return ret;
    }
    throw std::range_error("Tried to pop nonexistant FIFO element");
  }

  // Returns true if the queue is empty
  bool is_empty() {
    queue_mutex_.lock();
    bool empty = queue_.empty();
    queue_mutex_.unlock();
    return empty;
  }

  // Returns the number of elements in the queue
  size_t get_size() {
    queue_mutex_.lock();
    size_t ret = queue_.size();

//    size_t ret = queue_.max_size();
    queue_mutex_.unlock();
    return ret;
  }


  // Removes all elements in the queue
  void clear() {
    queue_mutex_.lock();
    std::queue<T> empty_queue;
    std::swap(queue_, empty_queue);
    queue_mutex_.unlock();
  }

 private:
  std::mutex queue_mutex_;
  std::queue<T> queue_;
};

#endif  // THREADSAFEFIFO_H_
