import {Component} from '@angular/core';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent {
  installOrUpdate() {
    alert("install or update")
  }

  close() {
    api.close()
  }
}
