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

#ifndef MODIFICATIONCALLBACKIMPL_H_
#define MODIFICATIONCALLBACKIMPL_H_

#include "ProjectHandler.h"
#include "CommunicationServer.h"

// Callback class for changes in project setup
class ModificationCallbackImpl : public ProjectHandler::ModificationCallbackInterface {
 public:
  explicit ModificationCallbackImpl(ProjectHandler& project_handler, const CommunicationServer& comm_server);
  virtual ~ModificationCallbackImpl();

  virtual void active_project_modified_callback() override;
  virtual void active_project_metadata_modified_callback() override;
  virtual void project_list_modified_callback() override;
  virtual void active_project_experiments_metadata_modified_callback() override;
  virtual void active_project_experiment_list_modified_callback() override;

 private:
  void broadcast_active_project();
  void broadcast_project_metadata();
  void broadcast_experiments_metadata();
  void broadcast_project_list();
  void broadcast_experiment_list();

  ProjectHandler& project_handler_;
  const CommunicationServer& comm_server_;
};

#endif  // MODIFICATIONCALLBACKIMPL_H_
