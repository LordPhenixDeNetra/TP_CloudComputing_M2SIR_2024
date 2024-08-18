import { Component, inject, OnDestroy, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NavigationEnd, Router, RouterLink } from '@angular/router';
import { Subscription } from 'rxjs';
import { ErrorHandler } from 'app/common/error-handler.injectable';
import { VpcService } from 'app/vpc/vpc.service';
import { VpcDTO } from 'app/vpc/vpc.model';


@Component({
  selector: 'app-vpc-list',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './vpc-list.component.html',
  styleUrl: './vpc.component.scss'
})
export class VpcListComponent implements OnInit, OnDestroy {

  vpcService = inject(VpcService);
  errorHandler = inject(ErrorHandler);
  router = inject(Router);
  vpcs?: VpcDTO[];
  navigationSubscription?: Subscription;
  confirmeDeleteVpc?: boolean;

  getMessage(key: string, details?: any) {
    const messages: Record<string, string> = {
      confirm: $localize`:@@delete.confirm:Cette action supprimera definitivement le VPC ainsi que tous les intances qui lui sont associés. Voulez vous continuer ?`,
      deleted: $localize`:@@vpc.delete.success:Le VPC à été supprimer avec succés.`,
      'vpc.instance.vpc.referenced': $localize`:@@vpc.instance.vpc.referenced:This entity is still referenced by Instance ${details?.id} via field Vpc.`
    };
    return messages[key];
  }

  ngOnInit() {
    // this.confirmeDeleteVpc = true;
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
    this.vpcService.getAllVpcs()
        .subscribe({
          next: (data) => this.vpcs = data,
          error: (error) => this.errorHandler.handleServerError(error.error)
        });
  }

  confirmDelete(id: number) {
    if (confirm(this.getMessage('confirm'))) {
      this.confirmeDeleteVpc = true;
      this.vpcService.deleteVpc(id)
          .subscribe({
            next: () =>  {
              this.router.navigate(['/vpcs']);
              this.getMessage('deleted');

              // state: {
              //   msgSuccess: this.getMessage('created')
              // }
            },

            error: (error) => {
              if (error.error?.code === 'REFERENCED') {
                this.confirmeDeleteVpc = false;
                const messageParts = error.error.message.split(',');
                this.router.navigate(['/vpcs'], {
                  state: {
                    msgError: this.getMessage(messageParts[0], { id: messageParts[1] })
                  }
                });
                return;
              }
              this.errorHandler.handleServerError(error.error);
              this.confirmeDeleteVpc = false;
            },
            complete:() =>{
              this.confirmeDeleteVpc = false;
            }
          });
    }
  }

}
