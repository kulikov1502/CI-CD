import com.bpmonline.jenkins.ConfigurationHelper
import com.bpmonline.jenkins.Utils
import groovy.transform.Field
import groovy.json.JsonSlurper

@Field String STEP_NAME = 'qualityGatesValidation'
@Field Set PARAMETER_KEYS = [
    'containerName',
    'isStage',
    'projectKey',
    'host'
]

void call(Map parameters = [:]) {
    handleStepErrors(stepName: STEP_NAME, stepParameters: parameters) {
        Map config = getConfiguration(parameters)
        Utils.stepWrap(this, config, this.&execute)
    }
}

private void execute(Map config) {
    String sonarQubeTaskURL = null;
    String reportURL = null;
    def list = currentBuild.rawBuild.getLog(100)
    String taskLinePrefix = "More about the report processing at "
    String reportLinePrefix = "ANALYSIS SUCCESSFUL, you can browse "
    for (int i=list.size()-1; i>0; i--) {
        String line = list[i]
        if (line.contains(taskLinePrefix)) {
            sonarQubeTaskURLLine = line.substring(taskLinePrefix.length())
            def sonarQubeTaskURLFinder = (sonarQubeTaskURLLine =~ 'report processing at (.*)')
            sonarQubeTaskURL = sonarQubeTaskURLFinder[0][1]
        }
        if (line.contains(reportLinePrefix)) {
            reportURLLine = line.substring(reportLinePrefix.length())
            def reportURLFinder = (reportURLLine =~ 'you can browse (.*)')
            reportURL = reportURLFinder[0][1]
        }
    }
    if (sonarQubeTaskURL == null) {
        println "sonarQubeTaskURL url not found"
    }
    println "SonarQube analysis report can be found at: " + reportURL
    String status = "PENDING"
    while (status == "PENDING" || status == "IN_PROGRESS") {
        sleep 1
        status = getSonarQubeTaskStatus(sonarQubeTaskURL)
    }
    if (status == "FAILED" || status == "CANCELED") {
        println "SonarQube analysis failed, please see to the SonarQube analysis: ${reportURL}"
    }
    qualityGateUrl = "${config.host}/api/qualitygates/project_status?projectKey=${config.projectKey}"
    def get = new URL(qualityGateUrl).openConnection()
    def getRC = get.getResponseCode()
    println(getRC)
    if(getRC.equals(200)) {
        def getResponse = get.getInputStream().getText()
        def jsonSlurper = new JsonSlurper()
        def object = jsonSlurper.parseText(getResponse)
        assert object instanceof Map
        println("Project status:" + object.projectStatus.status + ", detailed project information: ${config.host}/dashboard?id=${config.projectKey}")
        assert object.projectStatus.status == 'OK' : "Quality gate validation failed. Details: ${config.host}/dashboard?id=${config.projectKey}"
    }
}

def getSonarQubeTaskStatus(String statusUrl) {
    println "Requesting SonarQube status at " + statusUrl
    def getStatusTask = new URL(statusUrl).openConnection()
    def getStatusTaskRC = getStatusTask.getResponseCode()
    println(getStatusTaskRC)
    if(getStatusTaskRC.equals(200)) {
        def getStatusTaskResponse = getStatusTask.getInputStream().getText()
        def jsonSlurper = new JsonSlurper()
        def object = jsonSlurper.parseText(getStatusTaskResponse)
        assert object instanceof Map
        println("SonarQube task status is " + object.task.status)
        return object.task.status
    }
}

private Map getConfiguration(Map parameters) {
    return ConfigurationHelper
        .loadStepDefaults(this)
        .mixin(parameters, PARAMETER_KEYS)
        .withMandatoryProperty('projectKey')
        .withMandatoryProperty('host')
        .use()
}
