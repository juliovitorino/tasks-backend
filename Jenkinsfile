pipeline {
    agent any
    stages {
        stage ('Build Backend') {
            steps {
                sh 'mvn clean package -DskipTests=true'
            }
        }
        stage ('Testes Unitários') {
            steps {
                sh 'mvn test'
            }
        }
        stage ('Sonar Analysis') {
            environment {
                scannerHome = tool 'SONAR_SCANNER'
            }
            steps {
                withSonarQubeEnv('SONAR_LOCAL') {
                    sh "${scannerHome}/bin/sonar-scanner -e -Dsonar.projectKey=DeployBack -Dsonar.host.url=http://localhost:9000 -Dsonar.login=e6e3a716c0d3354054d23bd64e1cbfea70175a90 -Dsonar.java.binaries=target -Dsonar.coverage.exclusions=**/.mvn,**/src/test/**,**model/**,**Application.java"
                }
            }
        }
        stage ('Quality Gate Hook') {
            steps {
                sleep(10)
                timeout(time: 1, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: true
                }
            }
        }
        stage ('Deploy Backend') {
            steps {
                deploy adapters: [tomcat8(credentialsId: 'TomcatAdminLogin', path: '', url: 'http://localhost:8001/')], contextPath: 'tasks-backend', war: 'target/tasks-backend.war'
            }
        }
        stage ('API Tests') {
            steps {
                git credentialsId: 'GitHub_login', url: 'https://github.com/juliovitorino/tasks-api-test.git'
                sh 'mvn test'
            }
        }

    }
}
