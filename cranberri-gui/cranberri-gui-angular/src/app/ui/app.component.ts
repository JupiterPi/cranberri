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
      this.projects = await api.getProjects();
    })()
  }

  worlds: World[] = [];
  recentWorldId?: string;
  worldSelectedId: string | null = null;

  projects: Project[] = [];
  projectSelectedName: string | null = null;

  startServer() {
    api.startServer(this.worldSelectedId);
  }

  renameWorld() {
    if (this.worldSelectedId == null) return;
    const name = prompt("Name:");
    if (name == null) return;
    api.renameWorld(this.worldSelectedId, name).then(worlds => this.worlds = worlds);
  }

  archiveWorld() {
    if (this.worldSelectedId == null) return;
    api.archiveWorld(this.worldSelectedId).then(worlds => this.worlds = worlds);
  }

  createProject() {
    const name = prompt("Name:")
    if (name == null) return;
    const language = prompt("Language:");
    if (language == null) return;
    api.createProject(name.replace(" ", "_"), language)
      .then(projects => this.projects = projects);
  }

  openProjectsFolder() {
    api.openProjectsFolder();
  }

  openProjectFolder() {
    if (this.projectSelectedName == null) return;
    api.openProjectFolder(this.projectSelectedName);
  }
}
