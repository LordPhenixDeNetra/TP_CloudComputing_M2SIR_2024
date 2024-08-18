export class InstanceDTO {

  constructor(data?:Partial<InstanceDTO>) {
    Object.assign(this, data);
  }

  id?: number|null;
  instanceId?: string|null;
  name?: string|null;
  vpc?: number|null;
  publicInstance?: boolean|null;

}
