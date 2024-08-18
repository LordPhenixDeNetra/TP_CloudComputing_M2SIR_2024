import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from 'environments/environment';
import { VpcDTO } from 'app/vpc/vpc.model';


@Injectable({
  providedIn: 'root',
})
export class VpcService {

  http = inject(HttpClient);
  resourcePath = environment.apiPath + '/api/vpcs';

  getAllVpcs() {
    return this.http.get<VpcDTO[]>(this.resourcePath);
  }

  getVpc(id: number) {
    return this.http.get<VpcDTO>(this.resourcePath + '/' + id);
  }

  createVpc(vpcDTO: VpcDTO) {
    return this.http.post<number>(this.resourcePath, vpcDTO);
  }

  updateVpc(id: number, vpcDTO: VpcDTO) {
    return this.http.put<number>(this.resourcePath + '/' + id, vpcDTO);
  }

  deleteVpc(id: number) {
    return this.http.delete(this.resourcePath + '/' + id);
  }

}
