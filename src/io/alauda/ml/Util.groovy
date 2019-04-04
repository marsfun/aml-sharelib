package io.alauda.ml

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