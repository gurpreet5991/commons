import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';

import { BookingModelRequest } from '../../shared/booking-request.model';
import * as appConstants from '../../app.constants';
import Utils from '../../app.util';
import { AppConfigService } from '../../app-config.service';
import { Applicant } from '../../shared/models/dashboard-model/dashboard.modal';

@Injectable({
  providedIn: 'root'
})
export class DataStorageService {
  constructor(private httpClient: HttpClient, private appConfigService: AppConfigService) {}

  // BASE_URL = environment.BASE_URL;
  BASE_URL = this.appConfigService.getConfig()['BASE_URL'];
  PRE_REG_URL = this.appConfigService.getConfig()['PRE_REG_URL'];
  SEND_FILE_URL = this.BASE_URL + this.PRE_REG_URL + 'document/documents';
  DELETE_FILE_URL = this.BASE_URL + this.PRE_REG_URL + 'document/deleteDocument';
  GET_FILE_URL = this.BASE_URL + this.PRE_REG_URL + 'document/getDocument';
  MASTER_DATA_URL = this.BASE_URL + 'masterdata/v1.0/';
  AVAILABILITY_URL = this.BASE_URL + this.PRE_REG_URL + 'booking/availability';
  BOOKING_URL = this.BASE_URL + this.PRE_REG_URL + 'booking/book';
  DELETE_REGISTRATION_URL = this.BASE_URL + this.PRE_REG_URL + 'demographic/applications';
  COPY_DOCUMENT_URL = this.BASE_URL + this.PRE_REG_URL + 'document/copyDocuments';
  QR_CODE_URL = this.BASE_URL + this.PRE_REG_URL + 'notification/generateQRCode';
  NOTIFICATION_URL = this.BASE_URL + this.PRE_REG_URL + 'notification/notify';
  DISTANCE = 2000;

  getUsers(value: string) {
    return this.httpClient.get<Applicant[]>(this.BASE_URL + this.PRE_REG_URL + appConstants.APPEND_URL.applicants, {
      observe: 'body',
      responseType: 'json',
      params: new HttpParams().append(appConstants.PARAMS_KEYS.getUsers, value)
    });
  }

  getUser(preRegId: string) {
    return this.httpClient.get(this.BASE_URL + this.PRE_REG_URL + appConstants.APPEND_URL.get_applicant, {
      observe: 'body',
      responseType: 'json',
      params: new HttpParams().append(appConstants.PARAMS_KEYS.getUser, preRegId)
    });
  }

  getGenderDetails() {
    return this.httpClient.get(this.BASE_URL + appConstants.APPEND_URL.gender);
  }

  // getResidenceDetails() {
  //   return this.httpClient.get(this.BASE_URL + appConstants.APPEND_URL.resident);
  // }

  getTransliteration(request: any) {
    const obj = {
      id: appConstants.IDS.transliteration,
      reqTime: Utils.getCurrentDate(),
      ver: appConstants.VERSION,
      request: request
    };

    console.log(obj);

    return this.httpClient.post(this.BASE_URL + this.PRE_REG_URL + appConstants.APPEND_URL.transliteration, obj);
  }

  getUserDocuments(preRegId) {
    console.log('documents fetched for : ', preRegId);

    return this.httpClient.get(this.GET_FILE_URL, {
      observe: 'body',
      responseType: 'json',
      params: new HttpParams().append('pre_registration_id', preRegId)
    });
  }

  addUser(identity: any) {
    const obj = {
      id: appConstants.IDS.newUser,
      ver: appConstants.VERSION,
      reqTime: Utils.getCurrentDate(),
      request: identity
    };
    console.log('data being sent', obj);

    return this.httpClient.post(this.BASE_URL + this.PRE_REG_URL + appConstants.APPEND_URL.applicants, obj);
  }

  sendFile(formdata: FormData) {
    return this.httpClient.post(this.SEND_FILE_URL, formdata);
    // console.log('servvice called', formdata);
  }

  deleteRegistration(preId: string) {
    return this.httpClient.delete(this.DELETE_REGISTRATION_URL, {
      observe: 'body',
      responseType: 'json',
      params: new HttpParams().append(appConstants.PARAMS_KEYS.deleteUser, preId)
    });
  }

  cancelAppointment(data: BookingModelRequest) {
    console.log('cancel appointment data', data);
    return this.httpClient.put(this.BOOKING_URL, data);
  }

  getNearbyRegistrationCenters(coords: any) {
    return this.httpClient.get(
      this.MASTER_DATA_URL +
        'getcoordinatespecificregistrationcenters/' +
        localStorage.getItem('langCode') +
        '/' +
        coords.longitude +
        '/' +
        coords.latitude +
        '/' +
        this.DISTANCE
    );
  }

  getRegistrationCentersByName(locType: string, text: string) {
    return this.httpClient.get(
      this.MASTER_DATA_URL + 'registrationcenters/' + localStorage.getItem('langCode') + '/' + locType + '/' + text
    );
  }

  getLocationTypeData() {
    return this.httpClient.get(this.MASTER_DATA_URL + 'locations/' + localStorage.getItem('langCode'));
  }

  getAvailabilityData(registrationCenterId) {
    return this.httpClient.get(this.AVAILABILITY_URL, {
      observe: 'body',
      responseType: 'json',
      params: new HttpParams().append('registration_center_id', registrationCenterId)
    });
  }

  makeBooking(request: BookingModelRequest) {
    console.log('request inside service', request);
    return this.httpClient.post(this.BOOKING_URL, request);
  }

  getLocationMetadataHirearchy(value: string) {
    return this.httpClient.get(
      this.BASE_URL + appConstants.APPEND_URL.location + appConstants.APPEND_URL.location_metadata + value,
      {
        params: new HttpParams().append(appConstants.PARAMS_KEYS.locationHierarchyName, value)
      }
    );
  }

  getLocationImmediateHierearchy(lang: string, location: string) {
    return this.httpClient.get(
      this.BASE_URL +
        appConstants.APPEND_URL.location +
        appConstants.APPEND_URL.location_immediate_children +
        location +
        '/' +
        lang
    );
  }

  deleteFile(documentId) {
    return this.httpClient.delete(this.DELETE_FILE_URL, {
      observe: 'body',
      responseType: 'json',
      params: new HttpParams().append('documentId', documentId)
    });
  }

  getPreviewData(preRegId: string) {
    return this.httpClient.get(this.BASE_URL + appConstants.PREVIEW_DATA_APPEND_URL, {
      observe: 'body',
      responseType: 'json',
      params: new HttpParams().append(appConstants.PARAMS_KEYS.getUser, preRegId)
    });
  }

  getSecondaryLanguageLabels(langCode: string) {
    return this.httpClient.get(`./assets/i18n/${langCode}.json`);
  }

  copyDocument(catCode: string, sourceId: string, destinationId: string) {
    // return this.httpClient.post(this.COPY_DOCUMENT_URL, {
    //   params: new HttpParams()
    //     .append('catCode', catCode)
    //     .append('destinationPreId', destinationId)
    //     .append('sourcePrId', sourceId)
    // });
    // const params = new URLSearchParams();
    //   params.set('catCode',catCode);
    //   params.append('destinationPreId',destinationId);
    //   params.append('sourcePrId',sourceId);

    //   const options = new RequestOptions({
    //     params: params,
    //   });
    const url = this.COPY_DOCUMENT_URL + '?catCode=POA&destinationPreId=' + destinationId + '&sourcePrId=' + sourceId;
    console.log('copy document URL', url);
    return this.httpClient.post(url, '');
  }

  generateQRCode(data: string) {
    return this.httpClient.post(this.QR_CODE_URL, data);
  }

  sendNotification(data: FormData) {
    return this.httpClient.post(this.NOTIFICATION_URL, data);
  }

  recommendedCenters(langCode: string, locationHierarchyCode: number, data: string[]) {
    let url = this.MASTER_DATA_URL + 'registrationcenters/' + langCode + '/' + locationHierarchyCode + '/names?';
    data.forEach(name => {
      url += 'name=' + name;
      if (data.indexOf(name) !== data.length - 1) {
        url += '&';
      }
    });
    console.log(url);
    return this.httpClient.get(url);
  }
}
