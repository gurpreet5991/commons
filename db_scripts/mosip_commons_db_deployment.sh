### -- ---------------------------------------------------------------------------------------------------------
### -- Script Name		: MOSIP ALL DB Artifacts deployment script
### -- Deploy Module 	: MOSIP Common Module
### -- Purpose    		: To deploy MOSIP Common Module Database DB Artifacts.       
### -- Create By   		: Sadanandegowda DM
### -- Created Date		: 07-Jan-2020
### -- 
### -- Modified Date        Modified By         Comments / Remarks
### -- -----------------------------------------------------------------------------------------------------------

#! bin/bash
echo "`date` : You logged on to DB deplyment server as : `whoami`"
echo "`date` : MOSIP Database objects deployment started...."

echo "=============================================================================================================="
bash ./mosip_kernel/mosip_kernel_db_deploy.sh ./mosip_kernel/mosip_kernel_deploy.properties
echo "=============================================================================================================="

echo "=============================================================================================================="
bash ./mosip_idrepo/mosip_idrepo_db_deploy.sh ./mosip_idrepo/mosip_idrepo_deploy.properties
echo "=============================================================================================================="

echo "=============================================================================================================="
bash ./mosip_regprc/mosip_regprc_db_deploy.sh ./mosip_regprc/mosip_regprc_deploy.properties
echo "=============================================================================================================="

echo "=============================================================================================================="
bash ./mosip_iam/mosip_iam_db_deploy.sh ./mosip_iam/mosip_iam_deploy.properties
echo "=============================================================================================================="

echo "=============================================================================================================="
bash ./mosip_audit/mosip_audit_db_deploy.sh ./mosip_audit/mosip_audit_deploy.properties
echo "=============================================================================================================="

echo "`date` : MOSIP DB Deployment for core databases is completed, Please check the logs at respective logs directory for more information"
 
