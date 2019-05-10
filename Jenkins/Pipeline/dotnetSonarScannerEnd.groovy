import com.bpmonline.jenkins.ConfigurationHelper
import com.bpmonline.jenkins.Utils
import groovy.transform.Field

@Field String STEP_NAME = 'dotnetSonarScannerEnd'
@Field Set PARAMETER_KEYS = [
    'containerName',
    'isStage'
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
    def command = "sonarscanner end"
    return command
}

private Map getConfiguration(Map parameters) {
    return ConfigurationHelper
        .loadStepDefaults(this)
        .mixin(parameters, PARAMETER_KEYS)
        .use()
}
