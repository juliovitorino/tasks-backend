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
                dir('api-test') {
                    git credentialsId: 'GitHub_login', url: 'https://github.com/juliovitorino/tasks-api-test.git'
                    sh 'mvn test'
                }
            }
        }
        stage ('Build Frontend') {
            steps {
                dir('frontend') {
                    git credentialsId: 'GitHub_login', url: 'https://github.com/juliovitorino/tasks-frontend.git'
                    sh 'mvn clean package'
                }
            }
        }
        stage ('Deploy Frontend') {
            steps {
                dir('frontend') {
                    deploy adapters: [tomcat8(credentialsId: 'TomcatAdminLogin', path: '', url: 'http://localhost:8001/')], contextPath: 'tasks', war: 'target/tasks.war'
                }
                
            }
        }        
        stage ('Deploy Produção') {
            steps {
                sh 'docker-compose build'
                sh 'docker-compose up -d'
                mail bcc: '', body: 'Deploy Realizado com sucesso da build_$BUILD_NUMBER}, cc: '', from: '', replyTo: '', subject: 'Deploy em Produção realizado build_$BUILD_NUMBER', to: 'test@mailhog.local'
            }
        }        
    }

    post  {
        always {
            junit allowEmptyResults: true, testResults: 'target/surefire-reports/*.xml, api-test/target/surefire-reports/*.xml'
        }
        unsuccessful {
            emailext attachLog: true, body: 'See the log attached', subject: 'Build $BUILD_NUMBER has failed', to: 'test\@mailhog\.local'
        }

        fixed {
            emailext attachLog: true, body: 'See the log attached', subject: 'Build $BUILD_NUMBER is fine!', to: 'test\@mailhog\.local'
        }

    }
}
