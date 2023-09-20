def MS_GROUP_ID
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
        stage("Publish Backend to Nexus Repository Manager") {
            environment {
                NEXUS_VERSION = "nexus3"
                NEXUS_PROTOCOL = "http"
                NEXUS_URL = "locahost:8081"
                NEXUS_REPOSITORY = "ms-maven-snapshots"
                NEXUS_CREDENTIAL_ID = "nexus-user-credential"
            }   
            steps {
                script {
                    pom = readMavenPom file: "pom.xml";
                    filesByGlob = findFiles(glob: "target/*.${pom.packaging}");
                    echo "${filesByGlob[0].name} ${filesByGlob[0].path} ${filesByGlob[0].directory} ${filesByGlob[0].length} ${filesByGlob[0].lastModified}"
                    artifactPath = filesByGlob[0].path;
                    artifactExists = fileExists artifactPath;
                    if(artifactExists) {
                        echo "*** File: ${artifactPath}, group: ${pom.groupId}, packaging: ${pom.packaging}, version ${pom.version}";
                        MS_GROUP_ID = ${pom.groupId}
                    } else {
                        error "*** File: ${artifactPath}, could not be found";
                    }
                }
                nexusArtifactUploader credentialsId: 'nexus_user_credential', groupId: 'br.ce.wcaquino', nexusUrl: 'locahost:8081', nexusVersion: 'nexus2', protocol: 'http', repository: 'ms-maven-repository', version: '1.0.0-SNAPSHOT'
            }
        }             
        stage ('Deploy Produção') {
            steps {
                sh 'docker-compose build'
                sh 'docker-compose up -d'
            }
        }        
    }

    post  {
        always {
            junit allowEmptyResults: true, testResults: 'target/surefire-reports/*.xml, api-test/target/surefire-reports/*.xml'
            archiveArtifacts artifacts: 'target/tasks-backend.war, frontend/target/tasks.war', followSymlinks: false, onlyIfSuccessful: true
        }
        unsuccessful {
            emailext attachLog: true, body: 'See the log attached', subject: 'Build $BUILD_NUMBER has failed', to: 'test@mailhog.local'
        }

        fixed {
            emailext attachLog: true, body: 'See the log attached', subject: 'Build $BUILD_NUMBER is fine!', to: 'test@mailhog.local'
        }

    }
}
