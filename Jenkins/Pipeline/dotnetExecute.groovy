import com.bpmonline.jenkins.ConfigurationHelper
import com.bpmonline.jenkins.Utils
import groovy.transform.Field

@Field String STEP_NAME = 'dotnetExecute'
@Field Set PARAMETER_KEYS = [
    'containerName',
    'command',
    'returnStdout',
    'name',
    'isStage'
]

String call(Map parameters = [:]) {
    handleStepErrors(stepName: STEP_NAME, stepParameters: parameters) {
        Map config = getConfiguration(parameters)
        Utils.stepWrap(this, config, this.&execute)
    }
}

private def execute(Map config) {
    final String shScript = "dotnet ${config.command}"
    return shContainer(containerName: config.containerName, script: shScript, returnStdout: config.returnStdout)
}

private Map getConfiguration(Map parameters) {
    return ConfigurationHelper
        .loadStepDefaults(this)
        .mixin(parameters, PARAMETER_KEYS)
        .withMandatoryProperty('containerName')
        .withMandatoryProperty('command')
        .addIfEmpty('returnStdout', false)
        .use()
}
