import {Component} from '@angular/core';
import {Project} from "../../api";

@Component({
  selector: 'app-projects',
  templateUrl: './projects.component.html',
  styleUrls: ['../worlds_projects.scss']
})
export class ProjectsComponent {
  constructor() {
    (async () => {
      this.projects = await api.getProjects();
    })();
  }

  projects: Project[] = [];
  projectSelectedName: string | null = null;

  showCreateProject = false;
  createProjectNameInput = "";

  selectProject(project: Project) {
    this.projectSelectedName = project.name;
    this.showCreateProject = false;
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
}
