import com.bpmonline.jenkins.ConfigurationHelper
import com.bpmonline.jenkins.Utils
import groovy.transform.Field

@Field String STEP_NAME = 'dotnetUnitTests'
@Field Set PARAMETER_KEYS = [
    'configuration',
    'publishResults',
    'testsCategory',
    'name',
    'testsResultFolder',
    'containerName',
    'isStage',
    'vsTestFalse',
    'projectsMask',
    'projectsPathTests',
    'coverage',
    'coverageFormat'
]

void call(Map parameters = [:]) {
    handleStepErrors(stepName: STEP_NAME, stepParameters: parameters) {
        Map config = getConfiguration(parameters)
        Utils.stepWrap(this, config, this.&execute)
    }
}

private void execute(Map config) {
    String command = buildCommand(config)
    try {
        dotnetExecute(command: command, containerName: config.containerName)
    } finally {
        boolean isNeedToPublishResults = (config.publishResults && fileExists(config.testsResultFolder))
        if (isNeedToPublishResults) {
            publishMSTestResults(testsResultFolder: config.testsResultFolder, containerName: config.containerName)
        }
    }
}

private String buildCommandTest(Map config) {
    final String loggerType = 'trx'

    def command = "test"
    command += " ${config.projectsPathTests}"
    command += " --logger \"${loggerType}\""
    command += " --results-directory ${env.WORKSPACE}/${config.testsResultFolder}"
    command += " /p:CollectCoverage=${config.coverage}"
    if (config.coverageFormat) {
        command += " /p:CoverletOutputFormat=${config.coverageFormat}"
    } else {
        command += " /p:CoverletOutputFormat=opencover"
    }
    return command
}

private String buildCommandVSTest(Map config) {
    final String testLibraryMask = '*.Tests.dll'
    final String loggerType = 'trx'

    def command = "vstest"
    if (config.projectsMask) {
        command += " ${config.projectsMask}"
    } else {
        command += " src/*.Tests/bin/${config.configuration}/**/${testLibraryMask}"
    }
    command += " --logger:\"${loggerType}\""
    command += " --ResultsDirectory:${config.testsResultFolder}"
    if (config.testsCategory) {
        command += " --TestCaseFilter:TestCategory=${config.testsCategory}"
    }
    return command
}

private String buildCommand(Map config) {
    if(config.vsTestFalse) {
        return buildCommandTest(config)
    } else {
        return buildCommandVSTest(config)
    }
}

private Map getConfiguration(Map parameters) {
    return ConfigurationHelper
        .loadStepDefaults(this)
        .mixin(parameters, PARAMETER_KEYS)
        .withMandatoryProperty('configuration')
        .addIfEmpty('testsResultFolder', Utils.getRandomName('tests-result'))
        .use()
}
