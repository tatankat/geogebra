@NonCPS
def getChangelog() {
    def changeLogSets = currentBuild.changeSets
    def lines = []
    for (int i = 0; i < changeLogSets.size(); i++) {
        def entries = changeLogSets[i].items
        for (int j = 0; j < entries.length; j++) {
            def entry = entries[j]
            lines << "${entry.commitId},${entry.author.toString()},${new Date(entry.timestamp)},${entry.msg}"
        }
    }
    return lines.join("\n").toString()
}

def isGiac = env.BRANCH_NAME.matches("dependabot.*giac.*")
def nodeLabel = isGiac ? "Ubuntu" : "posix"

def s3uploadDefault = { dir, pattern, encoding ->
    withAWS (region:'eu-central-1', credentials:'aws-credentials') {
        if (!pattern.contains(".zip")) {
            s3Upload(bucket: 'apps-builds', workingDir: dir, path: "geogebra/branches/${env.GIT_BRANCH}/${env.BUILD_NUMBER}/",
               includePathPattern: pattern, acl: 'PublicRead', contentEncoding: encoding)
        }
        s3Upload(bucket: 'apps-builds', workingDir: dir, path: "geogebra/branches/${env.GIT_BRANCH}/latest/",
            includePathPattern: pattern, acl: 'PublicRead', contentEncoding: encoding)
    }
}

pipeline {
    options {
        gitLabConnection('git.geogebra.org')
        buildDiscarder(logRotator(numToKeepStr: '30'))
    }
    agent {label nodeLabel}
    stages {
        stage('cancel prev builds') {
            when {
                expression { return env.BRANCH_NAME != 'master' && env.BRANCH_NAME != 'dev' }
            }
            steps {
                milestone label: '', ordinal:  Integer.parseInt(env.BUILD_ID) - 1
                milestone label: '', ordinal:  Integer.parseInt(env.BUILD_ID)
            }
        }
        stage('build') {
            steps {
                updateGitlabCommitStatus name: 'build', state: 'pending'
                writeFile file: 'changes.csv', text: getChangelog()
                sh label: 'build web', script: './gradlew :web:prepareS3Upload :web:createDraftBundleZip :web:mergeDeploy -Pgdraft=true'
            }
        }
        stage('tests and reports') {
            when {
               expression {return !isGiac}
            }
            steps {
                sh label: 'test', script: "./gradlew :common-jre:test :desktop:test :common-jre:jacocoTestReport :web:test"
                sh label: 'static analysis', script: './gradlew pmdMain spotbugsMain -x common:spotbugsMain  -x renderer-base:spotbugsMain --max-workers=1'
                sh label: 'spotbugs common', script: './gradlew :common:spotbugsMain'
                sh label: 'code style', script: './gradlew :web:cpdCheck checkStyleMain checkStyleTest'
                junit '**/build/test-results/test/*.xml'
                recordIssues tools: [
                    cpd(pattern: '**/build/reports/cpd/cpdCheck.xml')
                ]
                recordIssues qualityGates: [[threshold: 1, type: 'TOTAL', unstable: true]], tools: [
                    spotBugs(pattern: '**/build/reports/spotbugs/*.xml', useRankAsPriority: true), 
                    pmdParser(pattern: '**/build/reports/pmd/main.xml'),
                    checkStyle(pattern: '**/build/reports/checkstyle/*.xml')
                ]
                publishCoverage adapters: [jacocoAdapter('**/build/reports/jacoco/test/*.xml')],
                    sourceFileResolver: sourceFiles('NEVER_STORE')

            }
        }
        stage('giac test') {
            when {
                expression {return isGiac}
            }
            parallel {
                stage('mac') {
                    agent {label 'mac'}
                    steps {
                        sh label: 'test', script: "./gradlew :desktop:test"
                        junit '**/build/test-results/test/*.xml'
                    }
                }
                stage('linux') {
                    agent {label 'Ubuntu'}
                    steps {
                        sh label: 'test', script: "./gradlew :desktop:test"
                        junit '**/build/test-results/test/*.xml'
                    }
                }
                stage('windows') {
                    agent {label 'winbuild'}
                    steps {
                        bat label: 'test', script: ".\\gradlew.bat :desktop:test"
                        junit '**/build/test-results/test/*.xml'
                    }
                }
            }
        }
        stage('archive') {
            steps {
                script {
                    withAWS (region:'eu-central-1', credentials:'aws-credentials') {
                       s3Delete(bucket: 'apps-builds', path: "geogebra/branches/${env.GIT_BRANCH}/latest/")
                    }
                    s3uploadDefault(".", "changes.csv", "")
                    s3uploadDefault("web/build/s3", "webSimple/**", "gzip")
                    s3uploadDefault("web/build/s3", "web3d/**", "gzip")
                    s3uploadDefault("web/war", "**/*.html", "")
                    s3uploadDefault("web/war", "**/deployggb.js", "")
                    s3uploadDefault("web/war", "*.zip", "")
                    s3uploadDefault("web/war", "geogebra-live.js", "")
                    s3uploadDefault("web/war", "platform.js", "")
                    s3uploadDefault("web/war", "css/**", "")
                }
            }
        }
    }
    post {
        unsuccessful {
            updateGitlabCommitStatus name: 'build', state: 'failed'
        }
        success {
            updateGitlabCommitStatus name: 'build', state: 'success'
        }
        always {
           cleanAndNotify()
        }
    }
}
