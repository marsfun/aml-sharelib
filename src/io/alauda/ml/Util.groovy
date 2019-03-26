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
