#!groovy

/*
 * Triggers a rebuild of the lastBuild of a remote JCK _rerun job on
 * ci.eclipse.org/temurin-compliance, re-using that build's own parameters.
 *
 * This is necessary because the TARGET/CUSTOM_TARGET for _rerun jobs are set
 * dynamically by the parent job (i.e. they contain only the failed test cases
 * from the parent run) and are not known to the triggering pipeline.
 *
 * Parameters:
 *   REMOTE_JOB_NAME        - e.g. Test_openjdk21_hs_dev.jck_x86-64_alpine-linux_rerun
 *   TEMURIN_COMPLIANCE_CREDENTIAL_ID - Jenkins credential ID (Username/Password or Secret Text)
 *                            for ci.eclipse.org/temurin-compliance. Defaults to
 *                            'temurin-compliance-token'.
 */

def remoteBase = 'https://ci.eclipse.org/temurin-compliance'
def jobName    = params.REMOTE_JOB_NAME?.trim()
def credId     = params.TEMURIN_COMPLIANCE_CREDENTIAL_ID ?: 'temurin-compliance-token'

if (!jobName) {
    error "REMOTE_JOB_NAME must be set"
}

def rebuildUrl = "${remoteBase}/job/${jobName}/lastBuild/rebuild/configSubmit"

node("worker || (ci.role.test&&hw.arch.x86&&sw.os.linux)") {
    stage("Rebuild ${jobName}") {
        echo "POSTing rebuild request to: ${rebuildUrl}"

        withCredentials([usernamePassword(
            credentialsId: credId,
            usernameVariable: 'REMOTE_USER',
            passwordVariable: 'REMOTE_TOKEN'
        )]) {
            // Fetch a crumb first (Jenkins CSRF protection)
            def crumbJson = httpRequest(
                url: "${remoteBase}/crumbIssuer/api/json",
                authentication: credId,
                validResponseCodes: '200'
            )
            def crumb = readJSON(text: crumbJson.content)

            // POST to the rebuild endpoint — no body needed; the Rebuild plugin
            // copies parameters from the referenced build automatically.
            def response = httpRequest(
                url: rebuildUrl,
                httpMode: 'POST',
                customHeaders: [[name: crumb.crumbRequestField, value: crumb.crumb]],
                authentication: credId,
                validResponseCodes: '200:302',
                ignoreSslErrors: false
            )

            echo "Rebuild response status: ${response.status}"
            if (response.status in [200, 201, 302]) {
                echo "Rebuild of ${jobName} triggered successfully."
                currentBuild.description = "Triggered rebuild of <a href='${remoteBase}/job/${jobName}/' target='_blank'>${jobName}</a>"
            } else {
                error "Unexpected response ${response.status} from rebuild endpoint"
            }
        }
    }
}
