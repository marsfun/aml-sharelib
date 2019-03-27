package io.alauda.ml

def getModelVersionContextPath(orig, version){
    def spliter = "/"
    def arr = orig.split(spliter)

    if (arr.length<1){
        return orig
    }
    arr[arr.length-1] = version
    for(String in arr){

    }
    // return "".join("/",arr)
   
    def result=""
    arr.eachWithIndex { vv,index->
        result+=vv
        if (index!=arr.size()-1){
            result+=spliter
        }
    }
    return result
}