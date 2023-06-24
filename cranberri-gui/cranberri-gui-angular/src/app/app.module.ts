import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';

import { AppComponent } from './ui/app.component';
import {FormsModule} from "@angular/forms";
import { WorldsComponent } from './ui/worlds/worlds.component';
import { ProjectsComponent } from './ui/projects/projects.component';

@NgModule({
  declarations: [
    AppComponent,
    WorldsComponent,
    ProjectsComponent
  ],
    imports: [
        BrowserModule,
        FormsModule
    ],
  providers: [],
  bootstrap: [AppComponent]
})
export class AppModule { }
