/*
 * Copyright 2013 Dirk Vranckaert
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package eu.vranckaert.worktime.dao.web.model.response.sync;

import eu.vranckaert.worktime.model.Project;

/**
 * User: Dirk Vranckaert
 * Date: 11/01/13
 * Time: 10:44
 */
public class ProjectSyncResult {
    private Project project;
    private Project syncedProject;
    private EntitySyncResolution resolution;

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public Project getSyncedProject() {
        return syncedProject;
    }

    public void setSyncedProject(Project syncedProject) {
        this.syncedProject = syncedProject;
    }

    public EntitySyncResolution getResolution() {
        return resolution;
    }

    public void setResolution(EntitySyncResolution resolution) {
        this.resolution = resolution;
    }
}
