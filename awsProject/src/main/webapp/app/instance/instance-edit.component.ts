import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { ReactiveFormsModule, FormControl, FormGroup, Validators } from '@angular/forms';
import { InputRowComponent } from 'app/common/input-row/input-row.component';
import { InstanceService } from 'app/instance/instance.service';
import { InstanceDTO } from 'app/instance/instance.model';
import { ErrorHandler } from 'app/common/error-handler.injectable';
import { updateForm } from 'app/common/utils';


@Component({
  selector: 'app-instance-edit',
  standalone: true,
  imports: [CommonModule, RouterLink, ReactiveFormsModule, InputRowComponent],
  templateUrl: './instance-edit.component.html'
})
export class InstanceEditComponent implements OnInit {

  instanceService = inject(InstanceService);
  route = inject(ActivatedRoute);
  router = inject(Router);
  errorHandler = inject(ErrorHandler);

  vpcValues?: Map<number,string>;
  currentId?: number;

  editForm = new FormGroup({
    id: new FormControl({ value: null, disabled: true }),
    instanceId: new FormControl(null, [Validators.maxLength(255)]),
    name: new FormControl(null, [Validators.required, Validators.maxLength(255)]),
    vpc: new FormControl(null)
  }, { updateOn: 'submit' });

  getMessage(key: string, details?: any) {
    const messages: Record<string, string> = {
      updated: $localize`:@@instance.update.success:Instance was updated successfully.`
    };
    return messages[key];
  }

  ngOnInit() {
    this.currentId = +this.route.snapshot.params['id'];
    this.instanceService.getVpcValues()
        .subscribe({
          next: (data) => this.vpcValues = data,
          error: (error) => this.errorHandler.handleServerError(error.error)
        });
    this.instanceService.getInstance(this.currentId!)
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
    const data = new InstanceDTO(this.editForm.value);
    this.instanceService.updateInstance(this.currentId!, data)
        .subscribe({
          next: () => this.router.navigate(['/instances'], {
            state: {
              msgSuccess: this.getMessage('updated')
            }
          }),
          error: (error) => this.errorHandler.handleServerError(error.error, this.editForm, this.getMessage)
        });
  }

}
