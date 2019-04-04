package io.alauda.ml

def getModelVersionContextPath(orig, version){
    /*
    def spliter = "/"
    def arr = orig.split(spliter)

    if (arr.length<1){
        return orig
    }
    arr[arr.length-1] = version
    // return "".join("/",arr)
    def result=""
    arr.eachWithIndex { vv,index->
        result+=vv
        if (index!=arr.size()-1){
            result+=spliter
        }
    }
    return result
    */

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
println getModelVersionContextPath("docker/half_plus_three/00000123","13")