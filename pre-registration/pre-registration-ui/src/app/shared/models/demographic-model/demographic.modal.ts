import { RequestModel } from './request.modal';

export class DemographicModel {
  constructor(public id: string, public ver: string, public reqTime: string, public request: RequestModel) {}
}
