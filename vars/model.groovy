
def buildImage(Map runtime,Map source,Map image,Map build){

    timeout(time: runtime.timeout_value, unit: runtime.timeout_unit){
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
        echo "seesee model folder2"
        // sh '''
        //     ls -l ${source.relative_directory}
        // '''

    }
    dir(source.relative_directory) {
        retry(build.retry_count) {
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
                docker build -t ${image.repoandtag} -f ${build.dockerfile_path} ${build.arguments} ${build.context}
                docker push ${image.repoandtag}
            """
            if (image.credentialId != '') {
                sh "docker logout ${image.repoandtag}"
            }
        }
    }


}