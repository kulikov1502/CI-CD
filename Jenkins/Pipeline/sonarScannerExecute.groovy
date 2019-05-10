import com.bpmonline.jenkins.ConfigurationHelper
import com.bpmonline.jenkins.Utils
import groovy.transform.Field

@Field String STEP_NAME = 'sonarScannerExecute'
@Field Set PARAMETER_KEYS = [
    'name',
    'containerName',
    'command',
    'returnStdout',
    'isStage'
]

void call(Map parameters = [:]) {
    handleStepErrors(stepName: STEP_NAME, stepParameters: parameters) {
        Map config = getConfiguration(parameters)
        Utils.stepWrap(this, config, this.&execute)
    }
}

private def execute(Map config) {
    String shScript = 'sonar-scanner'
    if (config.command) {
        shScript += " ${config.command}"
    }
    return shContainer(containerName: config.containerName, script: shScript, returnStdout: config.returnStdout)
}

private Map getConfiguration(Map parameters) {
    return ConfigurationHelper
        .loadStepDefaults(this)
        .mixin(parameters, PARAMETER_KEYS)
        .withMandatoryProperty('containerName')
        .addIfEmpty('returnStdout', false)
        .use()
}
