export interface OtpSendModel {
    appId: string ;
    context: string ;
    otpChannel: string[];
    templateVariables: any;
    userId: string;
    useridtype: string;
}
