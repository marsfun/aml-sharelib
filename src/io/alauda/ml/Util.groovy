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
    context=${1}
    version=${2}
    # echo "change2 context=${context} version=${version}"
    for f in `ls ${context}`; do
        if [ -d "${context}/${f}" ]; then
            # echo "现在进入" ${context}"/"${f} "检查是不是这个文件夹"
            # echo "1 context=${context} version=${version}"
            hasWanted "${context}/${f}" ${version}
            # echo "2 context=${context} version=${version}"
            # echo ${f}
            change2 "${context}/${f}" ${version}
        fi
    done 
}
hasWanted()
{
    # echo "在目录" ${1} "中检查"
    pbfile=0 
    varsfolder=0
    folder=""
    for ff in `ls ${1}`; do
        # echo "检查到" ${ff}
        if [[ -d "${1}/${ff}" && "${ff}" = "variables" ]]; then
            varsfolder=1
        fi
        if [ -f "${1}/${ff}" ]; then
            FILE="${1}/${ff}"
            extension=$(echo ${FILE} | cut -d . -f2)
            if [[ "pb"x = "${extension}"x ]]; then
                pbfile=1
            fi
        fi
        # echo pbfile=${pbfile} varsfolder=${varsfolder}
        if [ ${pbfile} -eq 1 -a ${varsfolder} -eq 1 ] ; then 
            echo ${1}
            # parent=${1%\/*}
            # "mv" ${1} ${parent}"/"${2}
        fi
    done
}
nnn=$(changeVersionByUser ${1} ${2})
echo $nnn
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