import com.bpmonline.jenkins.ConfigurationHelper
import com.bpmonline.jenkins.Utils
import groovy.transform.Field

@Field String STEP_NAME = 'dotnetSonarScannerStart'
@Field Set PARAMETER_KEYS = [
    'containerName',
    'isStage',
    'projectKey',
    'projectName',
    'host',
    'language',
    'exclusions',
    'reportsPaths'
]

void call(Map parameters = [:]) {
    handleStepErrors(stepName: STEP_NAME, stepParameters: parameters) {
        Map config = getConfiguration(parameters)
        Utils.stepWrap(this, config, this.&execute)
    }
}

private void execute(Map config) {
    String command = buildCommand(config)
    dotnetExecute(command: command, containerName: config.containerName)
}

private String buildCommand(Map config) {
    def command = "sonarscanner begin"
    command += " /k:\"${config.projectKey}\""    
    if (config.projectName) {
        command += " /n:\"${config.projectName}\""
    }
    command += " /d:sonar.host.url=\"${config.host}\""
    command += " /d:sonar.language=\"${config.language}\""
    if (config.sonarExclusions) {
        command += " /d:sonar.exclusions=\"${config.exclusions}\""
    }
    command += " /d:sonar.cs.opencover.reportsPaths=\"${config.reportsPaths}\""
    return command
}

private Map getConfiguration(Map parameters) {
    return ConfigurationHelper
        .loadStepDefaults(this)
        .mixin(parameters, PARAMETER_KEYS)
        .withMandatoryProperty('projectKey')
        .withMandatoryProperty('host')
        .use()
}
