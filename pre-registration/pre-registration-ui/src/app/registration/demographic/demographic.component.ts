import { Component, OnInit, ViewChild } from '@angular/core';
import { ActivatedRoute, Params, Router } from '@angular/router';
import { FormGroup, FormControl, Validators, AbstractControl, ValidatorFn, ValidationErrors } from '@angular/forms';
import { MatButtonToggleChange } from '@angular/material';
import { DatePipe } from '@angular/common';

import { RegistrationService } from '../registration.service';
import { DataStorageService } from 'src/app/shared/data-storage.service';
import { DemoLabels } from './demographic.labels';
import { IdentityModel } from './identity.model';
import { AttributeModel } from './attribute.model';
import { RequestModel } from './request.model';
import { DemoIdentityModel } from './demo.identity.model';
import { UserModel } from './user.model';
import { SharedService } from 'src/app/shared/shared.service';

@Component({
  selector: 'app-demographic',
  templateUrl: './demographic.component.html',
  styleUrls: ['./demographic.component.css']
})
export class DemographicComponent implements OnInit {
  id: number;
  step = 0;
  demo = new DemoLabels(
    'Full Name',
    'Date Of Birth',
    'gender',
    'Address Line 1',
    'Address Line 2',
    'Address Line 3',
    'Region',
    'Province',
    'City',
    'Postal Code',
    'Local Administrative Authority',
    'Email Id',
    'Mobile Number',
    'CNE/PIN Number',
    'Age'
  );
  numberPattern = '^[0-9]+[0-9]*$';
  textPattern = '^[a-zA-Z ]*$';
  ageOrDobPref = '';
  showCalender: boolean;
  showDate = false;
  numberOfApplicants: number;
  userForm: FormGroup;
  numbers: number[];
  isDisabled = [];
  checked = true;
  maxDate = new Date(Date.now());
  preRegId = '';
  loginId = '';
  dataUploadComplete = true;
  uppermostLocationHierarchy;
  @ViewChild('dd') dd;
  @ViewChild('mm') mm;
  @ViewChild('yyyy') yyyy;
  @ViewChild('age') age;

  constructor(
    private router: Router,
    private route: ActivatedRoute,
    private regService: RegistrationService,
    private dataStorageService: DataStorageService,
    private sharedService: SharedService
  ) {}

  ngOnInit() {
    // this.minDate.setFullYear(this.maxDate.getFullYear() - 150);
    this.route.parent.params.subscribe((params: Params) => {
      this.loginId = params['id'];
    });
    this.route.params.subscribe((params: Params) => {
      this.numberOfApplicants = +params['id'];
      this.numbers = Array(this.numberOfApplicants)
        .fill(0)
        .map((x, i) => i);
      this.initForm();
    });
    this.isDisabled[0] = true;

    // this.uppermostLocationHierarchy = this.dataStorageService
    //   .getLocationMetadataHirearchy('country')
    //   .subscribe(response => {
    //     const countryHirearchy = response['response'];
    //     const uppermostLocationHierarchy = countryHirearchy.filter(element => element.name === 'INDIA');
    //     this.uppermostLocationHierarchy = uppermostLocationHierarchy;
    //   });
  }

  initForm() {
    let fullName = '';
    let gender = '';
    let addressLine1 = '';
    let addressLine2 = '';
    let addressLine3 = '';
    let region = '';
    let province = '';
    let city = '';
    let localAdministrativeAuthority = '';
    let email = '';
    let dob = '';
    let age = '';
    let postalCode = '';
    let mobilePhone = '';
    let pin = '';
    let date = '';
    let month = '';
    let year = '';

    if (this.regService.getUser(this.step) != null) {
      const user = this.regService.getUser(this.step);
      this.preRegId = user.preRegId;
      fullName = user.identity.FullName[0].value;
      gender = user.identity.gender[0].value;
      addressLine1 = user.identity.addressLine1[0].value;
      addressLine2 = user.identity.addressLine2[0].value;
      addressLine3 = user.identity.addressLine3[0].value;
      region = user.identity.region[0].value;
      province = user.identity.province[0].value;
      city = user.identity.city[0].value;
      localAdministrativeAuthority = user.identity.localAdministrativeAuthority[0].value;
      email = user.identity.emailId[0].value;
      date = user.identity.dateOfBirth[0].value.split('/')[0];
      month = user.identity.dateOfBirth[0].value.split('/')[1];
      year = user.identity.dateOfBirth[0].value.split('/')[2];
      dob = user.identity.dateOfBirth[0].value;
      age = user.identity.dateOfBirth[0].value;
      postalCode = user.identity.postalcode[0].value;
      mobilePhone = user.identity.mobileNumber[0].value;
      pin = user.identity.CNEOrPINNumber[0].value;
    }

    this.userForm = new FormGroup({
      fullName: new FormControl(fullName.trim(), [Validators.required, this.noWhitespaceValidator]),
      gender: new FormControl(gender, Validators.required),
      addressLine1: new FormControl(addressLine1, [Validators.required, this.noWhitespaceValidator]),
      addressLine2: new FormControl(addressLine2),
      addressLine3: new FormControl(addressLine3),
      region: new FormControl(region, Validators.required),
      province: new FormControl(province, Validators.required),
      city: new FormControl(city, Validators.required),
      localAdministrativeAuthority: new FormControl(localAdministrativeAuthority, Validators.required),
      email: new FormControl(email, Validators.email),
      age: new FormControl(age, [
        Validators.required,
        Validators.max(150),
        Validators.min(1),
        Validators.pattern(this.numberPattern)
      ]),
      dob: new FormControl(dob),
      date: new FormControl(date, [
        Validators.required,
        Validators.maxLength(2),
        Validators.minLength(2),
        Validators.pattern(this.numberPattern)
      ]),
      month: new FormControl(month, [
        Validators.required,
        Validators.maxLength(2),
        Validators.minLength(2),
        Validators.pattern(this.numberPattern)
      ]),
      year: new FormControl(year, [
        Validators.required,
        Validators.maxLength(4),
        Validators.minLength(4),
        Validators.min(this.maxDate.getFullYear() - 150),
        Validators.pattern(this.numberPattern)
      ]),
      postalCode: new FormControl(postalCode, [
        Validators.required,
        Validators.maxLength(5),
        Validators.minLength(5),
        Validators.pattern(this.numberPattern)
      ]),
      mobilePhone: new FormControl(mobilePhone, [
        Validators.maxLength(9),
        Validators.minLength(9),
        Validators.pattern(this.numberPattern)
      ]),
      pin: new FormControl(pin, [Validators.maxLength(30), Validators.pattern(this.numberPattern)])
    });
    // this.userForm.setValidators([this.oneOfControlRequired(this.userForm.get('dob'), this.userForm.get('age'))]);
  }

  onBack() {}

  onSubmit() {
    // console.log(this.uppermostLocationHierarchy[0].code);
    // this.dataStorageService.getLocationList('BLR', 'ENG');

    let preId = '';
    const identity = this.createIdentityJSON();
    this.dataUploadComplete = false;
    return new Promise((resolve, reject) => {
      this.dataStorageService.addUser(this.createRequestJSON(this.preRegId)).subscribe(
        response => {
          if (this.regService.getUser(this.step) != null) {
            this.regService.updateUser(
              this.step,
              new UserModel(this.preRegId, identity, this.regService.getUserFiles(this.step))
            );
            this.sharedService.updateNameList(this.step, {
              fullName: this.userForm.controls.fullName.value,
              preRegId: this.preRegId
            });
          } else {
            preId = response['response'][0].prId;
            this.regService.addUser(new UserModel(preId, identity, []));
            this.sharedService.addNameList({
              fullName: this.userForm.controls.fullName.value,
              preRegId: preId
            });
          }
        },
        error => console.log(error),
        () => {
          this.isDisabled[this.step] = true;
          this.step++;
          this.checked = true;
          this.dataUploadComplete = true;
          if (this.step === this.numberOfApplicants) {
            // this.router.navigate(['../../file-upload'], { relativeTo: this.route });
          }
          return resolve(true);
        }
      );
    });
  }

  onGenderChange(gender: MatButtonToggleChange) {
    this.userForm.controls.gender.patchValue(gender.value);
  }

  onAgeChange() {
    const age = this.age.nativeElement.value;
    const now = new Date();
    const calulatedYear = now.getFullYear() - age;
    this.userForm.controls.date.patchValue('01');
    this.userForm.controls.month.patchValue('01');
    this.userForm.controls.year.patchValue(calulatedYear);
    this.userForm.controls['dob'].setErrors(null);
  }

  onDOBChange() {
    const date = this.dd.nativeElement.value;
    const month = this.mm.nativeElement.value;
    const year = this.yyyy.nativeElement.value;
    if (date !== '' && month !== '' && year !== '') {
      const newDate = month + '/' + date + '/' + year;
      const dateform = new Date(newDate);
      const _month = dateform.getMonth() + 1;
      if (dateform.toDateString() !== 'Invalid Date' && (+month === _month || month === '0' + _month)) {
        console.log('inside valid');
        const pipe = new DatePipe('en-US');
        const myFormattedDate = pipe.transform(dateform, 'dd/MM/yyyy');
        this.userForm.controls.dob.patchValue(myFormattedDate);
        this.userForm.controls.age.patchValue(this.calculateAge(dateform));
        console.log(myFormattedDate);
      } else {
        this.userForm.controls['dob'].markAsTouched();
        this.userForm.controls['dob'].setErrors({ incorrect: true });
        this.userForm.controls.age.patchValue('');
        console.log('invalid date');
      }
    }
  }

  private calculateAge(bDay) {
    const now = new Date();
    const born = new Date(bDay);
    const years = Math.floor((now.getTime() - born.getTime()) / (365.25 * 24 * 60 * 60 * 1000));
    if (years > 150) {
      this.userForm.controls['dob'].markAsTouched();
      this.userForm.controls['dob'].setErrors({ incorrect: true });
      this.userForm.controls['year'].setErrors(null);
      return '';
    } else {
      this.userForm.controls['dob'].markAsUntouched();
      this.userForm.controls['dob'].setErrors(null);
      this.userForm.controls['year'].setErrors(null);
      return years;
    }
  }

  // private oneOfControlRequired(...controls: AbstractControl[]): ValidatorFn {
  //   return (control: AbstractControl): ValidationErrors | null => {
  //     for (const aControl of controls) {
  //       if (!Validators.required(aControl)) {
  //         return null;
  //       }
  //     }
  //     return { oneOfRequired: true };
  //   };
  // }

  private noWhitespaceValidator(control: FormControl) {
    const isWhitespace = (control.value || '').trim().length === 0;
    const isValid = !isWhitespace;
    return isValid ? null : { whitespace: true };
  }

  private createIdentityJSON() {
    const identity = new IdentityModel(
      [new AttributeModel('en', this.demo.fullName, this.userForm.controls.fullName.value)],
      [new AttributeModel('en', this.demo.dateOfBirth, this.userForm.controls.dob.value)],
      [new AttributeModel('en', this.demo.gender, this.userForm.controls.gender.value)],
      [new AttributeModel('en', this.demo.addressLine1, this.userForm.controls.addressLine1.value)],
      [new AttributeModel('en', this.demo.addressLine2, this.userForm.controls.addressLine2.value)],
      [new AttributeModel('en', this.demo.addressLine3, this.userForm.controls.addressLine3.value)],
      [new AttributeModel('en', this.demo.region, this.userForm.controls.region.value)],
      [new AttributeModel('en', this.demo.province, this.userForm.controls.province.value)],
      [new AttributeModel('en', this.demo.city, this.userForm.controls.city.value)],
      [new AttributeModel('en', this.demo.postalCode, this.userForm.controls.postalCode.value)],
      [
        new AttributeModel(
          'en',
          this.demo.localAdministrativeAuthority,
          this.userForm.controls.localAdministrativeAuthority.value
        )
      ],
      [new AttributeModel('en', this.demo.emailId, this.userForm.controls.email.value)],
      [new AttributeModel('en', this.demo.mobileNumber, this.userForm.controls.mobilePhone.value)],
      [new AttributeModel('en', this.demo.CNEOrPINNumber, this.userForm.controls.pin.value)]
      // [new AttributeModel('en', this.demo.age, this.userForm.controls.age.value)]
    );
    return identity;
  }

  private createRequestJSON(id: string) {
    const identity = this.createIdentityJSON();
    const req: RequestModel = {
      preRegistrationId: id,
      createdBy: this.loginId,
      createdDateTime: '',
      updatedBy: '',
      updatedDateTime: '',
      statusCode: 'Pending_Appointment',
      langCode: 'en',
      demographicDetails: new DemoIdentityModel(identity)
    };
    return req;
  }
}
