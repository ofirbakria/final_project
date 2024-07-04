import groovy.json.JsonBuilder
import groovy.json.JsonSlurper
import groovy.json.JsonSlurperClassic
import groovy.transform.Field

@Field JOB = [:]

JOB.trigg_by = currentBuild.getBuildCauses('hudson.model.Cause$UserIdCause').userName

PROP = [:]

PROP['git_cred'] = 'github_ssh2'
PROP['branch'] = 'oferbakria'
PROP['proj_url'] = 'https://github.com/AlexeyMihaylovDev/atech-devops-nov-2023'

PROP['dockerhub_cred'] = 'dockerhub_cred' // Jenkins credential ID for DockerHub
PROP['docker_image1'] = 'oferbakria/poly'
PROP['docker_image2'] = 'oferbakria/yolo'
PROP['docker_tag'] = '1.7'
PROP['aws_cli_cred'] = 'aws_cred'

PROP['project_folder_name'] = 'final'
PROP['cluster_region'] = 'us-east-1'
PROP['cluster_name'] = 'Atech'
PROP['helm_sh'] = 'helm install ofertest -f values.yaml -n oferbakria .'


pipeline {

    agent { label 'jenkins_ec2' } // Use the label of your EC2 agent

    // triggers {
    //     GenericTrigger(
    //             genericVariables: [
    //                     [key: 'refsb', value: '$.ref'],
    //                     [key: 'pusher', value: '$.pusher.name'],
    //                     [key: 'change_files', value: '$.commits[0].modified[0]'],
    //                     // [key: 'type', value: '$.changes[0].type'],
    //             ],
    //             token: "123456",
    //             tokenCredentialId: '',
    //             printContributedVariables: true,
    //             printPostContent: false,
    //             silentResponse: false,
    //             regexpFilterText: '$refsb $change_files',
    //             regexpFilterExpression: '^(refs/heads/main)'        
    //     )
    // }


    stages {

    // stage('Hello'){
    //     steps {
    //         script {
    //             println("=====================================${STAGE_NAME}=====================================")
    //             println("Hello ${PROP.trigg_by}")
    //         }
    //     }
    // }




    stage('Git clone') {
        steps {
            script {
                println("=====================================${STAGE_NAME}=====================================")
                println("Cloning from branch ${PROP.branch} and using credentials ${PROP.git_cred}")

                // Enable sparse checkout
                checkout([
                    $class: 'GitSCM', 
                    branches: [[name: "*/${PROP.branch}"]],
                    doGenerateSubmoduleConfigurations: false, 
                    extensions: [
                        [$class: 'SparseCheckoutPaths', sparseCheckoutPaths: [[path: PROP.project_folder_name]]]
                    ], 
                    submoduleCfg: [], 
                    userRemoteConfigs: [[
                        credentialsId: PROP.git_cred, 
                        url: PROP.proj_url
                    ]]
                ])
            }
        }
        }




        stage('delete .git golder'){
            steps {
                script {
                    println("=====================================${STAGE_NAME}=====================================")
                    println("Deleting.git folder")
                    sh 'rm -rf .git'    
                }
            }
        }


        stage('Commit and Push'){
            steps {
                script {
                    println("=====================================${STAGE_NAME}=====================================")
                    println("Committing and pushing changes")
                    sshagent([PROP.git_cred]){
                        sh """
                            git init
                            git branch -M main
                            git remote add origin git@github.com:ofirbakria/forArgo.git
                            git add .
                            git commit -m "Commit at ${System.currentTimeMillis()}"

                            git pull origin main --rebase
                            git push --set-upstream origin main
                        """
                    }
                }
            }
        }




    }

    
}