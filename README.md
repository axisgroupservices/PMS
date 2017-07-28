1. Copy this project and paste.
2. Copy the location and cd to it.
3. Change groupId, artifactId and finalName on pom.xml. Customize your way.
4. Give new Git repository name on createRemoteRespository.sh file.
5. run chmod +x CreateRemoteRepository.sh
6. ./CreateRemoteRepository.sh
7. git remote -v : check if the remote repository is set.


//Git Hot fix policies.
1. Checkout to new branch call hot_fix_id_number
2. Commit hot_fix without push.
3. Checkout to develop branch and use this command.
git merge hot_fix
4. Push develop.

Check if two branch is in sync: git diff develop hot_fix;


//GetLogin Token
curl -v https://api.sandbox.paypal.com/v1/oauth2/token \
   -H "Accept: application/json" \
   -H "Accept-Language: en_US" \
   -u "AepMzCmhcr1xPK_QqAHL9l0pICYQtmFh4VdKeD9IWo7dDFv9Qiy-cMIup14W2hFmX49B_QND2xMLqyoT
:EBJmps4QAiLqUDhg-GpJaLIHDJ47LQSBD9cLHThSVdfrorHvvPDU0GltQRiB12lgSUphPnVMrOOMWQT2" \
   -d "grant_type=client_credentials"
   
   
   
   