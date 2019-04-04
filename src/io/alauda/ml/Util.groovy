package io.alauda.ml
import groovy.json.JsonSlurper

def getModelVersionContextPath(orig, version){
    def pos = orig.lastIndexOf("/")
    if(pos==-1){
        return orig
    }
    return orig.substring(0,pos)+"/"+version
}
// return (modelname , context)
def getModelNameAndContext(relativePath){
    def pos = relativePath.lastIndexOf("/")
    if(pos==-1){
        return [relativePath,"."]
    }
    return [relativePath.substring(pos+1),relativePath.substring(0,pos)]
}

def checkAndExpandImageMap(imageMap){
    if (imageMap.baseImageTag==''){
        imageMap.baseImageTag= 'latest'
    }
    imageMap.baseImage = imageMap.baseImageRepo +':'+imageMap.baseImageTag
    /*
    value: "{\"credentialId\":\"aml-fy-fyalaudaorg\",\"repositoryPath\":\"index.alauda.cn/alaudaorg/testcodemix\",\"type\":\"input\",\"tag\":\"create1\",\"secretNamespace\":\"aml-fy\"}"
    */
    def slurper = new JsonSlurper()
    def outImageRepositoryObject = slurper.parseText(imageMap.outImageRepositoryObjectStr)
    imageMap.outImageRepo = outImageRepositoryObject.repositoryPath
    imageMap.outImageTag = outImageRepositoryObject.tag
    imageMap.outImageRepoTag = outImageRepositoryObject.repositoryPath+':'+outImageRepositoryObject.tag

    imageMap.credentialId = outImageRepositoryObject.credentialId
    imageMap.type = outImageRepositoryObject.type
}

image = Map[
    'outImageRepositoryObjectStr':"{\"credentialId\":\"aml-fy-fyalaudaorg\",\"repositoryPath\":\"index.alauda.cn/alaudaorg/testcodemix\",\"type\":\"input\",\"tag\":\"create1\",\"secretNamespace\":\"aml-fy\"}",
]
def nmap = checkAndExpandImageMap(image)
println(nmap.outImageRepo)
println(nmap.outImageRepoTag)