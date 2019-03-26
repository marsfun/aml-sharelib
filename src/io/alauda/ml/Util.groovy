package io.alauda.ml

import groovy.io.FileType
import org.codehaus.groovy.util.StringUtil

def changeVersionByUser(String path ){
    println path
    def baseDir = new File(path)
    def guessDirList =[]

    baseDir.eachDir{
        println it.name
        def hasPbfile,hasVars
        for (file in it.listFiles()){
            if (file.name.endsWith(".pb")){
                hasPbfile = true
            }
            if (file.isDirectory()&& file.name == "variables"){
                hasVars = true
            }
            println file.name
        }
        if (hasPbfile && hasVars){
            guessDirList.add(it.name)
        }
    }
    if (guessDirList.size() ==1) {
        return guessDirList.get(0)
    }else{
        return ""
    }
};

def getChangeVersionShellScript(){
 return """
 #!/usr/bin/env bash

changeVersionByUser()
{
    context=”\${1}”
    version="\${2}"
   
    for f in `ls "\${context}"`; do
        if [ -d "\${context}/\${f}" ]; then
            hasWanted "\${context}/\${f}" \${version}
            changeVersionByUser "\${context}/\${f}" "\${version}"
        fi
    done 
}
hasWanted()
{
    pbfile=0 
    varsfolder=0
    folder=""
    for ff in `ls "\${1}"`; do
        if [[ -d "\${1}/\${ff}" && "\${ff}"x = "variables"x ]]; then
            varsfolder=1
        fi
        if [ -f "\${1}/\${ff}" ]; then
            FILE="\${1}/\${ff}"
            extension=\$(echo "\${FILE}" | cut -d . -f2)
            if [[ "pb"x = "\${extension}"x ]]; then
                pbfile=1
            fi
        fi
        if [ \${pbfile} -eq 1 -a \${varsfolder} -eq 1 ] ; then 
            echo \${1}
        fi
    done
}
nnn=\$(changeVersionByUser \${1} \${2})
echo \$nnn
"""   
}

def getModelVersionContextPath( orig, version){
    def arr = orig.split("/")

    if (arr.length<1){
        return orig
    }
    arr[arr.length-1] = version
    return "".join("/",arr)
}