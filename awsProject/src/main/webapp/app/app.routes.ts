import { Routes } from '@angular/router';
import { HomeComponent } from './home/home.component';
import { VpcListComponent } from './vpc/vpc-list.component';
import { VpcAddComponent } from './vpc/vpc-add.component';
import { VpcEditComponent } from './vpc/vpc-edit.component';
import { InstanceListComponent } from './instance/instance-list.component';
import { InstanceAddComponent } from './instance/instance-add.component';
import { InstanceEditComponent } from './instance/instance-edit.component';
import { ErrorComponent } from './error/error.component';


export const routes: Routes = [
  {
    path: '',
    component: HomeComponent,
    title: $localize`:@@home.index.headline:Welcome to your new app!`
  },
  {
    path: 'vpcs',
    component: VpcListComponent,
    title: $localize`:@@vpc.list.headline:Vpcs`
  },
  {
    path: 'vpcs/add',
    component: VpcAddComponent,
    title: $localize`:@@vpc.add.headline:Add Vpc`
  },
  {
    path: 'vpcs/edit/:id',
    component: VpcEditComponent,
    title: $localize`:@@vpc.edit.headline:Edit Vpc`
  },
  {
    path: 'instances',
    component: InstanceListComponent,
    title: $localize`:@@instance.list.headline:Instances`
  },
  {
    path: 'instances/add',
    component: InstanceAddComponent,
    title: $localize`:@@instance.add.headline:Add Instance`
  },
  {
    path: 'instances/edit/:id',
    component: InstanceEditComponent,
    title: $localize`:@@instance.edit.headline:Edit Instance`
  },
  {
    path: 'error',
    component: ErrorComponent,
    title: $localize`:@@error.headline:Error`
  },
  {
    path: '**',
    component: ErrorComponent,
    title: $localize`:@@notFound.headline:Page not found`
  }
];
