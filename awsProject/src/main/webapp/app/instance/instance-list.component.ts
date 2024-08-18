import { Component, inject, OnDestroy, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NavigationEnd, Router, RouterLink } from '@angular/router';
import { Subscription } from 'rxjs';
import { ErrorHandler } from 'app/common/error-handler.injectable';
import { InstanceService } from 'app/instance/instance.service';
import { InstanceDTO } from 'app/instance/instance.model';


@Component({
  selector: 'app-instance-list',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './instance-list.component.html',
  styleUrl: './instance.component.scss'
})
export class InstanceListComponent implements OnInit, OnDestroy {

  instanceService = inject(InstanceService);
  errorHandler = inject(ErrorHandler);
  router = inject(Router);
  instances?: InstanceDTO[];
  navigationSubscription?: Subscription;
  confirmeDeleteInstance?: boolean;

  getMessage(key: string, details?: any) {
    const messages: Record<string, string> = {
      confirm: $localize`:@@delete.confirm:Do you really want to delete this element? This cannot be undone.`,
      deleted: $localize`:@@instance.delete.success:Instance was removed successfully.`    };
    return messages[key];
  }

  ngOnInit() {
    this.loadData();
    this.navigationSubscription = this.router.events.subscribe((event) => {
      if (event instanceof NavigationEnd) {
        this.loadData();
      }
    });
  }

  ngOnDestroy() {
    this.navigationSubscription!.unsubscribe();
  }

  loadData() {
    this.instanceService.getAllInstances()
        .subscribe({
          next: (data) => this.instances = data,
          error: (error) => this.errorHandler.handleServerError(error.error)
        });
  }

  confirmDelete(id: number) {
    if (confirm(this.getMessage('confirm'))) {
      this.confirmeDeleteInstance = true;
      this.instanceService.deleteInstance(id)
          .subscribe({
            // next: () => this.router.navigate(['/instances'], {
            //   state: {
            //     msgInfo: this.getMessage('deleted')
            //   }
            // }),

            next: () =>  {
              this.router.navigate(['/instances']);
              this.getMessage('deleted');

              // state: {
              //   msgSuccess: this.getMessage('deleted')
              // }
            },
            error: (error) => {
              this.errorHandler.handleServerError(error.error)
              this.confirmeDeleteInstance = false;
            },
            complete:() =>{
              this.confirmeDeleteInstance = false;
            }
          });
    }
  }

}
