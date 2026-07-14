/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kie.processmigration.rest;

import java.util.List;
import java.util.Map;
import java.util.Set;

import jakarta.inject.Inject;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.kie.processmigration.model.ProcessInfo;
import org.kie.processmigration.model.ProcessRef;
import org.kie.processmigration.model.RunningInstance;
import org.kie.processmigration.model.exceptions.InvalidKieServerException;
import org.kie.processmigration.model.exceptions.ProcessDefinitionNotFoundException;
import org.kie.processmigration.service.KieService;

@Path("/kieservers")
@Produces(MediaType.APPLICATION_JSON)
public class KieServiceResource {

    private static final String DEFAULT_PAGE = "0";
    private static final String DEFAULT_PAGE_SIZE = "100";
    private static final String DEFAULT_SORT_COLUMN = "processInstanceId";
    private static final String DEFAULT_SORT_ORDER = "asc";

    @Inject
    KieService kieService;

    @GET
    public Response getKieServers() {
        return Response.ok(kieService.getConfigs()).build();
    }

    @GET
    @Path("/{kieServerId}/definitions")
    public Response getDefinitions(@PathParam("kieServerId") String kieServerId) throws InvalidKieServerException {
        Map<String, Set<String>> definitions = kieService.getDefinitions(kieServerId);
        return Response.ok(definitions).build();
    }

    @GET
    @Path("/{kieServerId}/definitions/{containerId}/{processId}")
    public Response getDefinition(
            @PathParam("kieServerId") String kieServerId,
            @PathParam("containerId") String containerId,
            @PathParam("processId") String processId
    ) throws InvalidKieServerException {
        try {
            ProcessInfo definition = kieService.getDefinition(kieServerId, new ProcessRef().setContainerId(containerId).setProcessId(processId));
            return Response.ok(definition).build();
        } catch (ProcessDefinitionNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        }
    }

    @GET
    @Path("/{kieServerId}/instances/{containerId}")
    public Response getRunningInstances(@PathParam("kieServerId") String kieServerId,
                                        @PathParam("containerId") String containerId,
                                        @DefaultValue(DEFAULT_PAGE) @QueryParam("page") Integer page,
                                        @DefaultValue(DEFAULT_PAGE_SIZE) @QueryParam("pageSize") Integer pageSize,
                                        @DefaultValue(DEFAULT_SORT_COLUMN) @QueryParam("sortBy") String sortBy,
                                        @DefaultValue(DEFAULT_SORT_ORDER) @QueryParam("orderBy") String orderBy) throws InvalidKieServerException {
        List<RunningInstance> result = kieService.getRunningInstances(kieServerId, containerId, page, pageSize, sortBy, orderBy);
        Long total = kieService.countRunningInstances(kieServerId, containerId);
        return Response.ok(result)
                .header("X-Total-Count", total)
                .build();
    }
}
