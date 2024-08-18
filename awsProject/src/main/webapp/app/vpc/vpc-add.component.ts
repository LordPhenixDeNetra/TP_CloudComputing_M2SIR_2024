import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { ReactiveFormsModule, FormControl, FormGroup, Validators } from '@angular/forms';
import { InputRowComponent } from 'app/common/input-row/input-row.component';
import { VpcService } from 'app/vpc/vpc.service';
import { VpcDTO } from 'app/vpc/vpc.model';
import { ErrorHandler } from 'app/common/error-handler.injectable';
import {SpinnerComponent} from "../common/spinner/spinner.component";
import {SpinnerService} from "../common/spinner/spinner.service";


@Component({
  selector: 'app-vpc-add',
  standalone: true,
  imports: [CommonModule, RouterLink, ReactiveFormsModule, InputRowComponent, SpinnerComponent],
  templateUrl: './vpc-add.component.html',
  styleUrl: './vpc.component.scss'
})
export class VpcAddComponent {

  vpcService = inject(VpcService);
  router = inject(Router);
  errorHandler = inject(ErrorHandler);
  spinnerService = inject(SpinnerService);

  // isLoading$ = this.spinnerService.isLoading$;

  isLoading = false;

  addForm = new FormGroup({
    // vpcId: new FormControl(null, [Validators.maxLength(255)]),
    nom: new FormControl(null, [Validators.required, Validators.maxLength(255)]),
    vpcCIDR: new FormControl(null, [Validators.required, Validators.maxLength(255)]),
    subnetPublicCIDR: new FormControl(null, [Validators.required, Validators.maxLength(255)]),
    subnetPrivateCIDR: new FormControl(null, [Validators.required, Validators.maxLength(255)]),
    subnetPublicName: new FormControl(null, [Validators.required, Validators.maxLength(255)]),
    subnetPrivateName: new FormControl(null, [Validators.required, Validators.maxLength(255)]),
    // iGatewayeId: new FormControl(null, [Validators.required, Validators.maxLength(255)]),
    groupSecPublicName: new FormControl(null, [Validators.required, Validators.maxLength(255)]),
    groupSecPrivateName: new FormControl(null, [Validators.required, Validators.maxLength(255)]),
    // routeTableId: new FormControl(null, [Validators.required, Validators.maxLength(255)])
  }, { updateOn: 'submit' });


  getMessage(key: string, details?: any) {
    const messages: Record<string, string> = {
      created: $localize`:@@vpc.create.success:Vpc was created successfully.`
    };
    return messages[key];
  }

  handleSubmit() {
    window.scrollTo(0, 0);
    this.addForm.markAllAsTouched();
    if (!this.addForm.valid) {
      return;
    }
    const data = new VpcDTO(this.addForm.value);

    // this.spinnerService.show();

    this.isLoading = true;

    this.vpcService.createVpc(data)
        .subscribe({

          next: () =>  {

            this.router.navigate(['/vpcs']);
            this.getMessage('created');
            // state: {
            //   msgSuccess: this.getMessage('created')
            // }
          },
          error: (error) => {
            this.errorHandler.handleServerError(error.error, this.addForm, this.getMessage)
            console.log("ERROR");
            console.log(error.error)
            this.isLoading = false;
          },
          complete: () => {
            // Cette fonction est appelée quand l'observable est complété
            // this.spinnerService.hide(); // Cache le spinner

            this.isLoading = false;
          }
        });
  }

}
