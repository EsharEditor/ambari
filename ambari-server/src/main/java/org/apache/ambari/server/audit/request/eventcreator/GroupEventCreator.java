/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.ambari.server.audit.request.eventcreator;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.ambari.server.api.services.Request;
import org.apache.ambari.server.api.services.Result;
import org.apache.ambari.server.api.services.ResultStatus;
import org.apache.ambari.server.audit.AuditEvent;
import org.apache.ambari.server.audit.request.event.CreateGroupRequestAuditEvent;
import org.apache.ambari.server.audit.request.event.DeleteGroupRequestAuditEvent;
import org.apache.ambari.server.audit.request.RequestAuditEventCreator;
import org.apache.ambari.server.controller.spi.Resource;
import org.apache.ambari.server.controller.utilities.PropertyHelper;
import org.joda.time.DateTime;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;

/**
 * This creator handles group requests
 * For resource type {@link Resource.Type#Group}
 * and request types {@link Request.Type#POST} and {@link Request.Type#DELETE}
 */
public class GroupEventCreator implements RequestAuditEventCreator {

  /**
   * Set of {@link Request.Type}s that are handled by this plugin
   */
  private Set<Request.Type> requestTypes = new HashSet<Request.Type>();

  {
    requestTypes.add(Request.Type.POST);
    requestTypes.add(Request.Type.DELETE);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Set<Request.Type> getRequestTypes() {
    return requestTypes;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Set<Resource.Type> getResourceTypes() {
    return Collections.singleton(Resource.Type.Group);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Set<ResultStatus.STATUS> getResultStatuses() {
    return null;
  }

  @Override
  public AuditEvent createAuditEvent(Request request, Result result) {
    String username = ((User) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUsername();

    switch(request.getRequestType()) {
      case POST:
        return CreateGroupRequestAuditEvent.builder()
          .withTimestamp(DateTime.now())
          .withRequestType(request.getRequestType())
          .withResultStatus(result.getStatus())
          .withUrl(request.getURI())
          .withRemoteIp(request.getRemoteAddress())
          .withUserName(username)
          .withGroupName(getGroupName(request))
          .build();
      case DELETE:
        return DeleteGroupRequestAuditEvent.builder()
          .withTimestamp(DateTime.now())
          .withRequestType(request.getRequestType())
          .withResultStatus(result.getStatus())
          .withUrl(request.getURI())
          .withRemoteIp(request.getRemoteAddress())
          .withUserName(username)
          .withGroupName(request.getResource().getKeyValueMap().get(Resource.Type.Group))
          .build();
      default:
        break;
    }
    return null;
  }

  private String getGroupName(Request request) {
    if(!request.getBody().getPropertySets().isEmpty()) {
      return String.valueOf(request.getBody().getPropertySets().iterator().next().get(PropertyHelper.getPropertyId("Groups", "group_name")));
    }
    return null;
  }

}