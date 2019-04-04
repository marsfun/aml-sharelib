#!/usr/bin/env groovy
import io.alauda.ml.Util

def buildImage(Map runtime,Map source,Map image,Map build){
    def project = runtime.project
    def kind = runtime.meta_kind
    def instance_name = runtime.meta_instance_name
    def start_state = runtime.start_state
    def err_state = runtime.error_state

    try{
        timeout(time: runtime.timeout_value, unit: runtime.timeout_unit){
            alaudaDevops.withCluster() {
            // 指定namespace
                alaudaDevops.withProject(project) {
                    def isFound = alaudaDevops.selector(kind, instance_name).exists()
                    echo "检查 ${kind}/${instance_name} 实例是否存在? ${isFound}"
                    if (!isFound){
                        error "in catch, internal error: ${kind}/${instance_name} not found"
                    }
                    //
                    def wantedobject = alaudaDevops.selector(kind,instance_name).object()
                    wantedobject.status.state = start_state
                    alaudaDevops.apply(wantedobject)
                }
            }

            if (source.repo_type != "SVN"){
                env.BRANCH = source.branch
                def scmVars = checkout([
                    $class: 'GitSCM',
                    branches: [[name: source.branch]],

                    extensions: [[
                    $class: 'SubmoduleOption',
                    recursiveSubmodules:
                    true,
                    reference: '',
                    ],[
                    $class: 'RelativeTargetDirectory',
                        relativeTargetDir: source.relative_directory
                    ]],
                    userRemoteConfigs:
                    [[
                    credentialsId: source.credential_id,
                    url: source.code_repo

                    ]]
                ])
                env.GIT_COMMIT = scmVars.GIT_COMMIT
                env.GIT_BRANCH = scmVars.GIT_LOCAL_BRANCH
            }
            if (source.repo_type == "SVN") {
                if (RELATIVE_DIRECTORY == ''){
                    env.RELATIVE_DIRECTORY = "."
                }
                def scmVars = checkout([
                    $class: "SubversionSCM",
                    additionalCredentials:
                    [],
                    excludedCommitMessages: "",
                    excludedRegions: "",
                    excludedRevprop:
                    "",
                    excludedUsers: "",
                    filterChangelog: false,
                    ignoreDirPropChanges:
                    false,
                    includedRegions: "",
                    locations: [[
                        credentialsId:
                    "${CREDENTIAL_ID}", depthOption: "infinity", ignoreExternalsOption: true,
                    local: "${RELATIVE_DIRECTORY}", remote: "${CODE_REPO}"
                    ]],
                    quietOperation:
                    true, workspaceUpdater: [$class: "UpdateUpdater"]
                ])
                SVN_REVISION = scmVars.SVN_REVISION
                env.SVN_REVISION = SVN_REVISION
                env.CODE_COMMIT = SVN_REVISION
            }
            // dir(source.relative_directory) {
                def util = new Util()
                (model_name,context)=util.getModelNameAndContext(source.model_path)
             
                def foundModelPath = sh (
                    script: """#!/usr/bin/env bash
                    changeModelVersion()    
                    {
                        context=\${1}
                        for f in `ls \${context}`; do
                            if [ -d "\${context}/\${f}" ]; then
                                hasWanted "\${context}/\${f}" 
                                changeModelVersion "\${context}/\${f}"
                            fi
                        done 
                    }
                    hasWanted()
                    {
                        pbfile=0 
                        varsfolder=0
                        folder=""
                        for ff in `ls \${1}`; do
                            if [[ -d "\${1}/\${ff}" && "\${ff}" = "variables" ]]; then
                                varsfolder=1
                            fi
                            if [ -f "\${1}/\${ff}" ]; then
                                FILE="\${1}/\${ff}"
                                extension=\$(echo \${FILE} | cut -d . -f2)
                                if [[ "pb"x = "\${extension}"x ]]; then
                                    pbfile=1
                                fi
                            fi
                            if [ \${pbfile} -eq 1 -a \${varsfolder} -eq 1 ] ; then 
                                echo \${1}
                            fi
                        done
                    }
                    out=\$(changeModelVersion """ +context+"/"+model_name+ ")\n"+"echo \$out\n"
                    ,returnStdout: true).trim()
                println("foundModelPath ="+foundModelPath)    
                if (foundModelPath==""){
                    error "model files not found"
                }
                def afterPath = util.getModelVersionContextPath(foundModelPath,runtime.model_version)
                println("change path ="+afterPath)
                if (foundModelPath!=afterPath){
                    sh """
                    rm -rf  $afterPath
                    echo "cleanup exists $afterPath"
                    
                    mv $foundModelPath $afterPath
                    echo "change model version from $foundModelPath to $afterPath"
                    """
                }
                
                retry(build.retry_count) {
                    def repoandtag = image.repo+':'+image.tag
                    if (image.credentialId != '') {
                        withCredentials([usernamePassword(credentialsId: image.credentialId, passwordVariable: 'PASSWD', usernameVariable: 'USER')]) {
                            sh "docker login ${image.repo} -u ${USER} -p ${PASSWD}"
                        }
                    }
                    def content= 'FROM _BASE_IMAGE_\n'+
                        'ARG MODEL=chicago-taxi\n'+
                        'ADD ${MODEL} ${MODEL_BASE_PATH}/${MODEL}\n'+
                        'ENV MODEL_NAME=${MODEL}'
                    content = content.replaceAll('_BASE_IMAGE_',image.baseImage)
                    writeFile file:build.dockerfile_path, text: content
                    sh """
                        docker build -t ${repoandtag} -f ${build.dockerfile_path} ${build.arguments} ${context}
                        docker push ${repoandtag}
                    """
                    if (image.credentialId != '') {
                        sh "docker logout ${repoandtag}"
                    }
                }
            // }
        }
    }catch(org.jenkinsci.plugins.workflow.steps.FlowInterruptedException e){
        // echo "111 FlowInterruptedException"
        // def cause = e.getCauses().get(0)
        // if (cause instanceof org.jenkinsci.plugins.workflow.steps.TimeoutStepExecution.ExceededTimeout) {
        //     echo "222 ExceededTimeout"
        // }
        alaudaDevops.withCluster() {
            alaudaDevops.withProject(project) {
                def isFound = alaudaDevops.selector(kind, instance_name).exists()
                echo "检查 ${kind}/${instance_name} 实例是否存在? ${isFound}"
                if (!isFound){
                    error "in catch, internal error: ${kind}/${instance_name} not found"
                }
                //
                def wantedobject = alaudaDevops.selector(kind,instance_name).object()
                wantedobject.status.state = err_state
                alaudaDevops.apply(wantedobject)
            }
        }

        throw e
    }
}

def deployModelService(Map modelservice){
    
    def kind = modelservice.meta_kind
    def instance_name = modelservice.meta_instance_name
    def start_state = modelservice.start_state
    def err_state = modelservice.error_state
    def _deploy_timeout_ = modelservice.deploy_timeout
    def _deploy_timeout_unit_ = modelservice.deploy_timeout_unit
    try{
        alaudaDevops.withCluster() {
            alaudaDevops.withProject(modelservice.project) {
                def isFound = alaudaDevops.selector(kind, instance_name).exists()
                echo "检查 ${kind}/${instance_name} 实例是否存在? ${isFound}"
                
                if (!isFound){
                    error "deploy internal error: ${kind}/${instance_name} not found"
                }
                def wantedcrd = alaudaDevops.selector(kind,instance_name)
                wantedobject =  wantedcrd.object()
                wantedobject.status.state = start_state
                alaudaDevops.apply(wantedobject)
                //watch
                timeout(time: _deploy_timeout_, unit: _deploy_timeout_unit_){
                    wantedcrd.watch {
                        echo "开始watch 资源 ${kind}/${instance_name}"
                        if ( it.count() == 0 ) {
                            echo "goto next watch loop [cause: ${instance_name} not found."
                            return false
                        }
                        def allDone = true
                        def current_state =''
                        it.withEach {
                            current_state = it.object().status.state
                            if ( current_state.equals(start_state) ) {
                                echo "state=${current_state}, 没有达到预期状态 [!${start_state}], go to next watch loop"
                                allDone = false
                            }
                        }
                        if (allDone){
                            echo "current_state=${current_state}, OK 达到预期状态 [!${start_state}], exit watch loop"
                        }
                        return allDone
                    }
                }
            }
        }
    }catch (java.lang.InterruptedException e) {
        alaudaDevops.withCluster() {
            alaudaDevops.withProject(modelservice.project) {
                def isFound = alaudaDevops.selector(kind, instance_name).exists()
                echo "检查 ${kind}/${instance_name} 实例是否存在? ${isFound}"
                if (!isFound){
                    error "deploy in catch, internal error: ${kind}/${instance_name} not found"
                }
                //
                def wantedobject = alaudaDevops.selector(kind,instance_name).object()
                wantedobject.status.state = err_state
                alaudaDevops.apply(wantedobject)
            }
        }
        throw e
    }
}