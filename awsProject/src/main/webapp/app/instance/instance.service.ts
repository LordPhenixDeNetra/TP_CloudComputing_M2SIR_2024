import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from 'environments/environment';
import { InstanceDTO } from 'app/instance/instance.model';
import { map } from 'rxjs';
import { transformRecordToMap } from 'app/common/utils';


@Injectable({
  providedIn: 'root',
})
export class InstanceService {

  http = inject(HttpClient);
  resourcePath = environment.apiPath + '/api/instances';

  getAllInstances() {
    return this.http.get<InstanceDTO[]>(this.resourcePath);
  }

  getInstance(id: number) {
    return this.http.get<InstanceDTO>(this.resourcePath + '/' + id);
  }

  createInstance(instanceDTO: InstanceDTO) {
    return this.http.post<number>(this.resourcePath, instanceDTO);
  }

  createWithInstanceType(instanceDTO: InstanceDTO) {
    return this.http.post<number>(this.resourcePath, instanceDTO);
  }

  updateInstance(id: number, instanceDTO: InstanceDTO) {
    return this.http.put<number>(this.resourcePath + '/' + id, instanceDTO);
  }

  deleteInstance(id: number) {
    return this.http.delete(this.resourcePath + '/' + id);
  }

  getVpcValues() {
    return this.http.get<Record<string,string>>(this.resourcePath + '/vpcValues')
        .pipe(map(transformRecordToMap));
  }

}
