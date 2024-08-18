export class VpcDTO {

  constructor(data:Partial<VpcDTO>) {
    Object.assign(this, data);
  }

  id?: number|null;
  vpcId?: string|null;
  nom?: string|null;
  vpcCIDR?: string|null;
  subnetPublicCIDR?: string|null;
  subnetPrivateCIDR?: string|null;
  subnetPublicName?: string|null;
  subnetPrivateName?: string|null;
  subnetPublicId?: string|null;
  subnetPrivateId?: string|null;
  iGatewayeId?: string|null;
  groupSecPublicName?: string|null;
  groupSecPrivateName?: string|null;
  routeTableId?: string|null;

}
