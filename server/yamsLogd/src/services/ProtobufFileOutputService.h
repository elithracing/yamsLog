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

#ifndef SERVICES_PROTOBUFFILEOUTPUTSERVICE_H_
#define SERVICES_PROTOBUFFILEOUTPUTSERVICE_H_

#include <fstream>
#include <mutex>

#include <boost/shared_ptr.hpp>

#include "AbstractService.h"
#include "sensors/AbstractSensor.h"
#include "ProjectHandler.h"
#include "StatusObserver.h"

class ProtobufFileOutputService : public AbstractService {
 public:
  ProtobufFileOutputService(
      const std::vector<boost::shared_ptr<AbstractSensor>>& sensors,
      const ProjectHandler& project_handler,
      StatusObserver& status_observer);

  virtual void finalize() override;

 protected:
  virtual bool initialize() override;
  virtual void execute() override;

 private:
  void write_to_file(const boost::shared_ptr<AbstractSensor>& sensor) const;

  const std::vector<boost::shared_ptr<AbstractSensor>>& sensors_;
  const ProjectHandler& project_handler_;
  StatusObserver& status_observer_;
};

#endif  // SERVICES_PROTOBUFFILEOUTPUTSERVICE_H_
