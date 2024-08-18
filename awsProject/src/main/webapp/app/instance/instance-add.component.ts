import {Component, inject, OnInit, Type} from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import {ReactiveFormsModule, FormControl, FormGroup, Validators, FormsModule} from '@angular/forms';
import { InputRowComponent } from 'app/common/input-row/input-row.component';
import { InstanceService } from 'app/instance/instance.service';
import { InstanceDTO } from 'app/instance/instance.model';
import { ErrorHandler } from 'app/common/error-handler.injectable';


@Component({
  selector: 'app-instance-add',
  standalone: true,
  imports: [CommonModule, RouterLink, ReactiveFormsModule, InputRowComponent, FormsModule],
  templateUrl: './instance-add.component.html',
  styleUrl: './instance.component.scss'
})
export class InstanceAddComponent implements OnInit {

  instanceService = inject(InstanceService);
  router = inject(Router);
  errorHandler = inject(ErrorHandler);
  instance = new InstanceDTO();

  vpcValues?: Map<number,string>;

  addForm = new FormGroup({
    // instanceId: new FormControl(null, [Validators.maxLength(255)]),
    publicInstance: new FormControl(true),
    name: new FormControl(null, [Validators.required, Validators.maxLength(255)]),
    vpc: new FormControl(null)
  }, { updateOn: 'submit' });

  getMessage(key: string, details?: any) {
    const messages: Record<string, string> = {
      created: $localize`:@@instance.create.success:Instance was created successfully.`
    };
    return messages[key];
  }

  ngOnInit() {
    this.instanceService.getVpcValues()
        .subscribe({
          next: (data) => this.vpcValues = data,
          error: (error) => this.errorHandler.handleServerError(error.error)
        });
  }

  handleSubmit() {
    window.scrollTo(0, 0);
    this.addForm.markAllAsTouched();
    if (!this.addForm.valid) {
      return;
    }

    console.log(this.addForm.value.publicInstance!)

    // const data = new InstanceDTO(this.addForm.value);
    // data.isPublic = this.addForm.value.isPublic!;

    this.instance = this.addForm.value;
    // this.instance.isPublic = this.addForm.value.isPublic!;
    // this.instance.publicInstance = true;
    // console.log(data)
    // this.instanceService.createInstance(data)
    this.instanceService.createWithInstanceType(this.instance)
        .subscribe({
          // next: () =>
          //   this.router.navigate(['/instances'], {
          //   state: {
          //     msgSuccess: this.getMessage('created')
          //   }
          // }),
          next: () =>  {
            this.router.navigate(['/instances']);
            this.getMessage('created');
            console.log(this.instance);
            console.log(typeof this.instance.publicInstance);
            // state: {
            //   msgSuccess: this.getMessage('created')
            // }
          },

          error: (error) => this.errorHandler.handleServerError(error.error, this.addForm, this.getMessage)
        });
  }

  isPublicChecked(): boolean {
    return this.addForm.value.publicInstance! === true;
  }

}
