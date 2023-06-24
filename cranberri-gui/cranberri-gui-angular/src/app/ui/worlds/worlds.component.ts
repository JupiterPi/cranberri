import {Component} from '@angular/core';
import {World} from "../../api";

@Component({
  selector: 'app-worlds',
  templateUrl: './worlds.component.html',
  styleUrls: ['../worlds_projects.scss']
})
export class WorldsComponent {
  constructor() {
    (async () => {
      this.worlds = await api.getWorlds();
      this.recentWorldId = await api.getActiveWorldId();
      this.worldSelectedId = this.recentWorldId;
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
}
