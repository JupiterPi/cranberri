import { Component } from '@angular/core';
import {Project, World} from "../api";

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent {
  constructor() {
    (async () => {
      this.worlds = await api.getWorlds();
      this.recentWorldId = await api.getActiveWorldId();
      this.worldSelectedId = this.recentWorldId;
      this.projects = await api.getProjects();
    })();
  }

  worlds: World[] = [];
  recentWorldId?: string;
  worldSelectedId: string | null = null;

  worldRename: string | null = null;
  worldRenameInput = "";

  selectWorld(world: World | null) {
    const worldId = world?.id ?? null;
    if (this.worldSelectedId !== worldId) {
      this.worldSelectedId = worldId;
      this.worldRename = null;
    }
  }

  projects: Project[] = [];
  projectSelectedName: string | null = null;

  showCreateProject = false;
  createProjectNameInput = "";

  selectProject(project: Project) {
    this.projectSelectedName = project.name;
    this.showCreateProject = false;
  }

  startServer() {
    api.startServer(this.worldSelectedId).then(world => {
      if (this.worldSelectedId == null) this.worlds.unshift(world);
      this.worldSelectedId = world.id;
    });
  }

  renameWorld() {
    if (this.worldSelectedId == null) return;
    this.worldRename = this.worldSelectedId;
    this.worldRenameInput = this.worlds.filter(world => world.id == this.worldSelectedId)[0].name;
  }
  confirmRenameWorld() {
    if (this.worldRenameInput === "") this.worldRenameInput = "Unnamed World";
    api.renameWorld(this.worldRename!!, this.worldRenameInput).then(worlds => this.worlds = worlds);
    this.worldRename = null;
  }

  archiveWorld() {
    if (this.worldSelectedId == null) return;
    api.archiveWorld(this.worldSelectedId).then(worlds => this.worlds = worlds);
  }

  createProject() {
    this.showCreateProject = true;
    this.createProjectNameInput = "";
  }
  confirmCreateProject() {
    this.showCreateProject = false;
    if (this.createProjectNameInput == "") return;
    api.createProject(this.createProjectNameInput.replace(" ", "_"), "kotlin")
      .then(project => {
        this.projectSelectedName = project.name;
        return this.projects.unshift(project);
      });
  }

  openProjectsFolder() {
    api.openProjectsFolder();
  }

  openProjectFolder() {
    if (this.projectSelectedName == null) return;
    console.log(this.projectSelectedName);
    api.openProjectFolder(this.projectSelectedName);
  }

  installOrUpdate() {
    alert("install or update")
  }

  close() {
    api.close()
  }
}
