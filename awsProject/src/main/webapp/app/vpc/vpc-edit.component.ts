import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { ReactiveFormsModule, FormControl, FormGroup, Validators } from '@angular/forms';
import { InputRowComponent } from 'app/common/input-row/input-row.component';
import { VpcService } from 'app/vpc/vpc.service';
import { VpcDTO } from 'app/vpc/vpc.model';
import { ErrorHandler } from 'app/common/error-handler.injectable';
import { updateForm } from 'app/common/utils';


@Component({
  selector: 'app-vpc-edit',
  standalone: true,
  imports: [CommonModule, RouterLink, ReactiveFormsModule, InputRowComponent],
  templateUrl: './vpc-edit.component.html'
})
export class VpcEditComponent implements OnInit {

  vpcService = inject(VpcService);
  route = inject(ActivatedRoute);
  router = inject(Router);
  errorHandler = inject(ErrorHandler);

  currentId?: number;

  editForm = new FormGroup({
    id: new FormControl({ value: null, disabled: true }),
    vpcId: new FormControl(null, [Validators.maxLength(255)]),
    nom: new FormControl(null, [Validators.required, Validators.maxLength(255)]),
    vpcCIDR: new FormControl(null, [Validators.required, Validators.maxLength(255)]),
    subnetPublicCIDR: new FormControl(null, [Validators.required, Validators.maxLength(255)]),
    subnetPrivateCIDR: new FormControl(null, [Validators.required, Validators.maxLength(255)]),
    subnetPublicName: new FormControl(null, [Validators.required, Validators.maxLength(255)]),
    subnetPrivateName: new FormControl(null, [Validators.required, Validators.maxLength(255)]),
    iGatewayeId: new FormControl(null, [Validators.required, Validators.maxLength(255)]),
    groupSecPublicName: new FormControl(null, [Validators.required, Validators.maxLength(255)]),
    groupSecPrivateName: new FormControl(null, [Validators.required, Validators.maxLength(255)]),
    routeTableId: new FormControl(null, [Validators.required, Validators.maxLength(255)])
  }, { updateOn: 'submit' });

  getMessage(key: string, details?: any) {
    const messages: Record<string, string> = {
      updated: $localize`:@@vpc.update.success:Vpc was updated successfully.`
    };
    return messages[key];
  }

  ngOnInit() {
    this.currentId = +this.route.snapshot.params['id'];
    this.vpcService.getVpc(this.currentId!)
        .subscribe({
          next: (data) => updateForm(this.editForm, data),
          error: (error) => this.errorHandler.handleServerError(error.error)
        });
  }

  handleSubmit() {
    window.scrollTo(0, 0);
    this.editForm.markAllAsTouched();
    if (!this.editForm.valid) {
      return;
    }
    const data = new VpcDTO(this.editForm.value);
    this.vpcService.updateVpc(this.currentId!, data)
        .subscribe({
          next: () => this.router.navigate(['/vpcs'], {
            state: {
              msgSuccess: this.getMessage('updated')
            }
          }),
          error: (error) => this.errorHandler.handleServerError(error.error, this.editForm, this.getMessage)
        });
  }

}
