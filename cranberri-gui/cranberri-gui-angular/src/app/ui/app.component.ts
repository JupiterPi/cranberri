import {Component} from '@angular/core';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent {
  constructor() {
    (async () => {
      this.isInstalled = await api.isInstalled();
      this.updateAvailable = await api.updateAvailable();
    })();
  }

  isInstalled = false;
  installOrUpdateLoading = false;
  updateAvailable = false;

  installOrUpdate() {
    this.installOrUpdateLoading = true;
    api.installOrUpdate().then(() => {
      this.installOrUpdateLoading = false;
      api.isInstalled().then(isInstalled => this.isInstalled = isInstalled);
    });
  }

  close() {
    api.close();
  }
}
