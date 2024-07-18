import groovy.json.JsonBuilder
import groovy.json.JsonSlurper
import groovy.json.JsonSlurperClassic
import groovy.transform.Field

@Field JOB = [:]

JOB.trigg_by = currentBuild.getBuildCauses('hudson.model.Cause$UserIdCause').userName

PROP = [:]

PROP['git_cred'] = 'github_ssh2'
PROP['branch'] = 'main'
PROP['proj_url'] = 'https://github.com/ofirbakria/final_project'

PROP['dockerhub_cred'] = 'dockerhub_cred' // Jenkins credential ID for DockerHub
PROP['docker_image1'] = 'oferbakria/poly'
PROP['docker_image2'] = 'oferbakria/yolo'
PROP['docker_tag'] = '2.2'
PROP['aws_cli_cred'] = 'aws_cred'

PROP['project_folder_name'] = 'final'
PROP['cluster_region'] = 'us-east-1'
PROP['cluster_name'] = 'Atech'
// PROP['helm_sh'] = 'helm install ofertest -f values.yaml -n oferbakria .'


pipeline {

    agent { label 'jenkins_ec2' } // Use the label of your EC2 agent

    triggers {
        GenericTrigger(
                genericVariables: [
                        [key: 'refsb', value: '$.ref'],
                        [key: 'pusher', value: '$.pusher.name'],
                        [key: 'change_files', value: '$.commits[0].modified[0]'],
                        // [key: 'type', value: '$.changes[0].type'],
                ],
                token: "123456",
                tokenCredentialId: '',
                printContributedVariables: true,
                printPostContent: false,
                silentResponse: false,
                regexpFilterText: '$refsb $change_files',
                regexpFilterExpression: '^(refs/heads/main)'        
        )
    }


    stages {

        
    stage('SCM') {
        checkout scm
    }
    stage('SonarQube Analysis') {
        def scannerHome = tool 'SonarScanner';
        withSonarQubeEnv() {
        sh "${scannerHome}/bin/sonar-scanner"
        }
    }

    stage('Hello'){
        steps {
            script {
                println("=====================================${STAGE_NAME}=====================================")
                println("Hello ${pusher}")
            }
        }
    }


    // stage('Git clone') {
    //     steps {
    //         script {
    //             println("=====================================${STAGE_NAME}=====================================")
    //             println("Cloning from branch ${PROP.branch} and using credentials ${PROP.git_cred}")

    //             // Enable sparse checkout
    //             checkout([
    //                 $class: 'GitSCM', 
    //                 branches: [[name: "*/${PROP.branch}"]],
    //                 doGenerateSubmoduleConfigurations: false, 
    //                 extensions: [
    //                     [$class: 'SparseCheckoutPaths', sparseCheckoutPaths: [[path: PROP.project_folder_name]]]
    //                 ], 
    //                 submoduleCfg: [], 
    //                 userRemoteConfigs: [[
    //                     credentialsId: PROP.git_cred, 
    //                     url: PROP.proj_url
    //                 ]]
    //             ])
    //         }
    //     }
    //     }


    //     stage('Copy PEM File to Node') {
    //         steps {
    //             script {
    //                 // Use the withCredentials block to securely retrieve the file credential
    //                 withCredentials([file(credentialsId: 'alb2_cert', variable: 'CREDENTIAL_FILE')]) {
    //                     def filePath = env.CREDENTIAL_FILE
    //                     def destinationDir = '/home/ubuntu/workspace/final/final/aws_project/polybot'  // Replace with your actual destination directory

    //                     // Copy the file to the node using Jenkins pipeline sh step
    //                     sh "cp ${filePath} ${destinationDir}"
    //                     sh "sudo chmod 666 ${destinationDir}/YOURPUBLIC.pem"
    //                 }
    //             }
    //         }
    //     }


    // stage('Create Docker Image') {
    //     steps {
    //          script {
    //          println("=====================================${STAGE_NAME}=====================================")
    //             sh "docker build -t ${PROP.docker_image1}:${PROP.docker_tag} ./final/aws_project/polybot"
    //             sh "docker build -t ${PROP.docker_image2}:${PROP.docker_tag} ./final/aws_project/yolo5"
    //          }
    //     }
    // }

    // stage('Push Docker Images') {
    //     steps {
    //         script {
    //             println("=====================================${STAGE_NAME}=====================================")
    //             withCredentials([usernamePassword(credentialsId: PROP.dockerhub_cred, passwordVariable: 'DOCKERHUB_PASS', usernameVariable: 'DOCKERHUB_USER')]) {

    //                 sh """
    //                     echo $DOCKERHUB_PASS | docker login -u $DOCKERHUB_USER --password-stdin
    //                     docker push ${PROP.docker_image1}:${PROP.docker_tag}
    //                     docker push ${PROP.docker_image2}:${PROP.docker_tag}

    //                     docker rmi -f ${PROP.docker_image1}:${PROP.docker_tag}
    //                     docker rmi -f ${PROP.docker_image2}:${PROP.docker_tag}
    //                 """
    //             }
    //         }
    //     }
    // }



    // stage('Read helm chart values'){
    //     steps{
    //         script{  
    //                 dir("${WORKSPACE}/${PROP.project_folder_name}")  {
    //                 sh 'ls -l'    
    //             def chartValues = readFile (file: 'values.yaml')  
    //             JOB.buildChartValues = chartValues
    //                 }
    //             println(JOB.buildChartValues)
                            
    //         }
            
    //     }
    // }



    // stage('Input params '){
    //     when{
    //         expression { !JOB.trigg_by.isEmpty()}
    //     }
    //     steps{
    //         script{
    //             def userInput = input (id: 'userInput', message: 'Please provide the following parameters', parameters: [
    //                 [$class: 'WHideParameterDefinition', defaultValue: JOB.buildChartValues, description: '', name: 'chartValues'],
    //                 choice(name: 'CHOICE_HELM', choices: ['install','upgrade','uninstall'], description: 'Choose one'),
    //                 [$class: 'DynamicReferenceParameter', choiceType: 'ET_FORMATTED_HTML', description: "",
    //                         name: 'ChartValues', omitValueField: true, referencedParameters: 'chartValues',
    //                         script: [$class: 'GroovyScript', fallbackScript: [classpath: [], sandbox: true,
    //                                                                         script: 'return [\'error\']'], script: [classpath: [], sandbox: true, script: '''
    //                         def jsonValue = "${chartValues}".replaceAll('"', '\\\\"') 
    //                         return "<textarea name=\\"value\\" rows='27' cols='100'>${jsonValue}</textarea>"
    //                     ''']]],

    //             ])
    //             JOB.params = userInput['ChartValues']
    //             sh 'rm -f values.yaml' //delete old values.yaml

    //         }
    //     }
    // }



    //     stage('Helm update values by user input'){
    //         when{
    //             expression { !JOB.trigg_by.isEmpty()}
    //         }
    //         steps{
    //             script{
    //                  dir("${WORKSPACE}/${PROP.project_folder_name}")  {
    //                     writeFile file: 'values.yaml', text: JOB.params
    //                 }
    //             }
    //         }
    //     }


    //     stage('Login to EKS') {
    //         steps {
    //             withCredentials([[$class: 'AmazonWebServicesCredentialsBinding', credentialsId: PROP['aws_cli_cred']]]) {
    //                 script {
    //                     sh "aws eks --region ${PROP.cluster_region} update-kubeconfig --name ${PROP.cluster_name}"
    //                 }
    //             }
    //         }
    //     }




        // stage('Trigger Argocd') {
        //     steps {
        //         build job: 'argocd', wait: true
        //     }
        // }





    //     stage('Helm install'){
    //         steps{
    //             script{
    //                  dir("${WORKSPACE}/final")  {
    //                     sh "${PROP.helm_sh}"
    //                 }
    //             }
    //         }
    //     }





    }

    
}
// install helm and kubectl add correct aws cred